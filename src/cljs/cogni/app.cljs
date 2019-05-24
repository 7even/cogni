(ns cogni.app
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.edn :as ajax]))

(rf/reg-event-db :initialize-db
                 (fn [_ _]
                   {:purchases []}))

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
                   (assoc db :purchases (map :name response))))

(rf/reg-event-db :purchases-failed-to-load
                 (fn [db [_ response]]
                   (println "Failed to load purchases:" response)
                   db))

(rf/reg-sub :purchases
            (fn [db]
              (:purchases db)))

(defn ui []
  [:div
   [:h2 "Purchases"]
   [:ul
    (for [purchase @(rf/subscribe [:purchases])]
      [:li {:key purchase} purchase])]])

(defn render []
  (ra/render [ui] (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:load-purchases])
  (render))
