(ns cogni.app
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.edn :as ajax]))

(rf/reg-event-db :initialize-db
                 (fn [_ _]
                   {:purchases []
                    :loading? true
                    :error nil}))

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
                       (assoc :purchases (map :name response))
                       (assoc :loading? false))))

(rf/reg-event-db :purchases-failed-to-load
                 (fn [db [_ response]]
                   (-> db
                       (assoc :error (str "Failed to load purchases: " response))
                       (assoc :loading? false))))

(rf/reg-sub :purchases
            (fn [db]
              (:purchases db)))

(rf/reg-sub :loading?
            (fn [db]
              (:loading? db)))

(rf/reg-sub :error
            (fn [db]
              (:error db)))

(defn ui []
  (cond
    @(rf/subscribe [:loading?])
    [:i "Loading..."]
    (some? @(rf/subscribe [:error]))
    [:i
     {:style {:color "red"}}
     @(rf/subscribe [:error])]
    :else
    [:div
     [:h2 "Purchases"]
     [:ul
      (for [purchase @(rf/subscribe [:purchases])]
        [:li {:key purchase} purchase])]]))

(defn render []
  (ra/render [ui] (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:load-purchases])
  (render))
