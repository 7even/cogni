(ns cogni.core
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [cogni.events :as events]
            [cogni.subs :as subs]))

(defn ui []
  (cond
    @(rf/subscribe [::subs/loading?])
    [:i "Loading..."]
    (some? @(rf/subscribe [::subs/loading-error]))
    [:i
     {:style {:color "red"}}
     @(rf/subscribe [::subs/loading-error])]
    :else
    [:div
     [:h2 "Purchases"]
     [:ul
      (for [purchase @(rf/subscribe [::subs/purchases])]
        [:li
         {:key purchase}
         purchase
         " "
         [:a {:href "#"
              :on-click (fn [e]
                          (do
                            (.preventDefault e)
                            (rf/dispatch [::events/retract-purchase purchase])))}
          "x"]])]
     [:input {:type :text
              :value @(rf/subscribe [::subs/new-purchase])
              :on-change #(rf/dispatch [::events/change-new-purchase
                                        (-> % .-target .-value)])}]
     [:button
      {:on-click #(rf/dispatch [::events/add-purchase])
       :disabled @(rf/subscribe [::subs/new-purchase-invalid?])}
      "Add"]
     (when (some? @(rf/subscribe [::subs/duplication-error]))
       [:div {:style {:color "red"}}
        @(rf/subscribe [::subs/duplication-error])])]))

(defn render []
  (ra/render [ui]
             (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::events/load-purchases])
  (render))
