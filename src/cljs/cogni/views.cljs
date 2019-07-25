(ns cogni.views
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [cogni.events :as events]
            [cogni.subs :as subs]))

(defn- grid-row [contents]
  [:div.row
   [:div.col-3
    contents]])

(defn purchase-row [purchase]
  (letfn [(on-click [e]
            (.preventDefault e)
            (rf/dispatch [::events/retract-purchase purchase])
            (rf/dispatch [::events/purchase-retracted purchase]))]
    [:li.list-group-item
     purchase
     " "
     [:button.close {:href "#"
                     :on-click on-click}
      [:span "×"]]]))

(defn new-purchase-input []
  [:input.form-control {:type :text
                        :value @(rf/subscribe [::subs/new-purchase])
                        :on-change #(rf/dispatch [::events/change-new-purchase
                                                  (.. % -target -value)])}])

(defn add-purchase-button []
  [:div.input-group-append
   [:button.btn.btn-primary
    {:on-click #(do (rf/dispatch [::events/add-purchase])
                    (rf/dispatch [::events/purchase-added]))
     :disabled @(rf/subscribe [::subs/new-purchase-invalid?])}
    "Add"]])

(defn purchases-list []
  (let [purchases @(rf/subscribe [::subs/purchases])
        duplication-error @(rf/subscribe [::subs/duplication-error])]
    [:div
     (grid-row [:h2 {:style {:margin-top "10px"}} "Purchases"])
     (grid-row [:ul.list-group
                (for [purchase purchases]
                  ^{:key purchase} [purchase-row purchase])])
     (grid-row [:div.input-group {:style {:margin-top "10px"}}
                [new-purchase-input]
                [add-purchase-button]])
     (when (some? duplication-error)
       [:div {:style {:color "red"}} duplication-error])]))

(defn ui []
  (let [loading? @(rf/subscribe [::subs/loading?])
        loading-error @(rf/subscribe [::subs/loading-error])]
    [:div.container
     (cond
       loading?              (grid-row [:i "Loading..."])
       (some? loading-error) (grid-row [:i {:style {:color "red"}} loading-error])
       :else                 (purchases-list))]))
