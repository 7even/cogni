(ns cogni.views
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [cogni.events :as events]
            [cogni.subs :as subs]))

(defn purchase-row [purchase]
  (letfn [(on-click [e]
            (do
              (.preventDefault e)
              (rf/dispatch [::events/retract-purchase purchase])))]
    [:li
     purchase
     " "
     [:a {:href "#"
          :on-click on-click}
      "x"]]))

(defn new-purchase-input []
  [:input {:type :text
           :value @(rf/subscribe [::subs/new-purchase])
           :on-change #(rf/dispatch [::events/change-new-purchase
                                     (-> % .-target .-value)])}])

(defn add-purchase-button []
  [:button
   {:on-click #(rf/dispatch [::events/add-purchase])
    :disabled @(rf/subscribe [::subs/new-purchase-invalid?])}
   "Add"])

(defn purchases-list []
  (let [purchases @(rf/subscribe [::subs/purchases])
        duplication-error @(rf/subscribe [::subs/duplication-error])]
    [:div
     [:h2 "Purchases"]
     [:ul
      (for [purchase purchases]
        ^{:key purchase} [purchase-row purchase])]
     [new-purchase-input]
     [add-purchase-button]
     (when (some? duplication-error)
       [:div {:style {:color "red"}} duplication-error])]))

(defn ui []
  (let [loading? @(rf/subscribe [::subs/loading?])
        loading-error @(rf/subscribe [::subs/loading-error])]
   (cond
     loading?              [:i "Loading..."]
     (some? loading-error) [:i {:style {:color "red"}} loading-error]
     :else                 purchases-list)))
