(ns cogni.app
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]))

(rf/reg-event-db :initialize-db
                 (fn [_ _]
                   {:purchases ["Bread"
                                "Milk"
                                "Beer"]}))

(rf/reg-sub :purchases
            (fn [db]
              (:purchases db)))

(defn ui []
  [:div
   [:h2 "Purchases"]
   [:ul
    (for [purchase @(rf/subscribe [:purchases])]
      [:li purchase])]])

(defn render []
  (ra/render [ui] (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (render))
