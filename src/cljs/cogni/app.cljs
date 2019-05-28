(ns cogni.app
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.edn :as ajax]))

(rf/reg-event-db :initialize-db
                 (fn [_ _]
                   {:purchases []
                    :new-purchase ""
                    :loading? true
                    :loading-error nil
                    :duplication-error nil}))

(rf/reg-event-fx :load-purchases
                 (fn [{db :db} _]
                   {:http-xhrio {:method :get
                                 :uri "http://localhost:8890/purchases"
                                 :format (ajax/edn-request-format)
                                 :response-format (ajax/edn-response-format)
                                 :on-success [:purchases-loaded]
                                 :on-failure [:purchases-failed-to-load]}}))

(rf/reg-event-db :purchases-loaded
                 (fn [db [_ response]]
                   (-> db
                       (assoc :purchases (mapv :name response))
                       (assoc :loading? false))))

(rf/reg-event-db :purchases-failed-to-load
                 (fn [db [_ response]]
                   (-> db
                       (assoc :loading-error (str "Failed to load purchases: " response))
                       (assoc :loading? false))))

(rf/reg-event-db :change-new-purchase
                 (fn [db [_ new-purchase]]
                   (assoc db :new-purchase new-purchase)))

(rf/reg-event-fx :add-purchase
                 (fn [{db :db} _]
                   {:http-xhrio {:method :post
                                 :uri "http://localhost:8890/purchases"
                                 :params {:name (:new-purchase db)}
                                 :format (ajax/edn-request-format)
                                 :response-format (ajax/edn-response-format)
                                 :on-success [:purchase-added]
                                 :on-failure [:purchase-failed-to-add]}}))

(rf/reg-event-db :purchase-added
                 (fn [db _]
                   (-> db
                       (update :purchases conj (:new-purchase db))
                       (assoc :new-purchase ""))))

(rf/reg-sub :purchases
            (fn [db]
              (:purchases db)))

(rf/reg-sub :new-purchase
            (fn [db]
              (:new-purchase db)))

(rf/reg-sub :loading?
            (fn [db]
              (:loading? db)))

(rf/reg-sub :loading-error
            (fn [db]
              (:loading-error db)))

(rf/reg-sub :duplication-error
            (fn []
              [(rf/subscribe [:purchases])
               (rf/subscribe [:new-purchase])])
            (fn [[purchases new-purchase]]
              (when (seq (filter #(= new-purchase %) purchases))
                "This purchase is already on the list")))

(defn ui []
  (cond
    @(rf/subscribe [:loading?])
    [:i "Loading..."]
    (some? @(rf/subscribe [:loading-error]))
    [:i
     {:style {:color "red"}}
     @(rf/subscribe [:loading-error])]
    :else
    [:div
     [:h2 "Purchases"]
     [:ul
      (for [purchase @(rf/subscribe [:purchases])]
        [:li {:key purchase} purchase])]
     [:input {:type :text
              :value @(rf/subscribe [:new-purchase])
              :on-change #(rf/dispatch [:change-new-purchase
                                        (-> % .-target .-value)])}]
     [:button {:on-click #(rf/dispatch [:add-purchase])} "Add"]
     (when (some? @(rf/subscribe [:duplication-error]))
       [:div {:style {:color "red"}}
        @(rf/subscribe [:duplication-error])])]))

(defn render []
  (ra/render [ui] (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:load-purchases])
  (render))
