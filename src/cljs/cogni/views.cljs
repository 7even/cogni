(ns cogni.views
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [cogni.events :as events]
            [cogni.subs :as subs]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [clojure.string :as str]))

(defn- cs [& names]
  (str/join " " (filter identity names)))

(defn- grid-row [contents]
  [:div.row
   [:div.col
    contents]])

(defn purchase-row [purchase]
  (letfn [(on-click [e]
            (.preventDefault e)
            (rf/dispatch [::events/retract-purchase purchase]))]
    [:li.list-group-item
     purchase
     " "
     (when-not @(rf/subscribe [::subs/viewing-snapshot?])
       [:button.close {:href "#"
                       :on-click on-click}
        [:span "×"]])]))

(defn new-purchase-input []
  [:input.form-control {:type :text
                        :value @(rf/subscribe [::subs/new-purchase])
                        :disabled @(rf/subscribe [::subs/viewing-snapshot?])
                        :on-change #(rf/dispatch [::events/change-new-purchase
                                                  (.. % -target -value)])
                        :on-key-down #(when (and (not @(rf/subscribe [::subs/cant-add-purchase?]))
                                                 (= (.-keyCode %) 13))
                                        (rf/dispatch [::events/add-purchase]))}])

(defn add-purchase-button []
  [:div.input-group-append
   [:button.btn.btn-primary
    {:on-click #(rf/dispatch [::events/add-purchase])
     :disabled @(rf/subscribe [::subs/cant-add-purchase?])}
    "Add"]])

(defn purchases-list []
  (let [purchases (if @(rf/subscribe [::subs/viewing-snapshot?])
                    (mapv :name @(rf/subscribe [::subs/current-snapshot]))
                    @(rf/subscribe [::subs/purchases]))
        duplication-error @(rf/subscribe [::subs/duplication-error])]
    [:div.col-sm-auto
     (grid-row [:h3 {:style {:margin-top "10px"}} "Purchases"])
     (grid-row [:ul.list-group
                (for [purchase purchases]
                  ^{:key purchase} [purchase-row purchase])])
     (grid-row [:div.input-group {:style {:margin-top "10px"}}
                [new-purchase-input]
                [add-purchase-button]])
     (when (some? duplication-error)
       [:div {:style {:color "red"}} duplication-error])]))

(defn history-item-row [{:keys [t happened-at]}]
  (let [active? (= t @(rf/subscribe [::subs/current-t]))]
    [:a.list-group-item.list-group-item-action
     {:href "#"
      :class (cs (when active?
                   "active")
                 (when @(rf/subscribe [::subs/snapshot-loading?])
                   "disabled"))
      :on-click (fn [e]
                  (.preventDefault e)
                  (if active?
                    (rf/dispatch [::events/switch-to-current])
                    (rf/dispatch [::events/switch-to-snapshot t])))}
     (f/unparse (f/formatters :mysql)
                (t/to-default-time-zone happened-at))]))

(defn history []
  (let [items @(rf/subscribe [::subs/history])]
    [:div.col-sm-auto
     (grid-row [:h3 {:style {:margin-top "10px"}} "Changes history"])
     (grid-row [:div.list-group
                (for [item (take 20 items)]
                  ^{:key (:t item)} [history-item-row item])])]))

(defn ui []
  (let [loading? @(rf/subscribe [::subs/loading?])
        loading-error @(rf/subscribe [::subs/loading-error])]
    [:div.container
     (cond
       loading?              (grid-row [:i "Loading..."])
       (some? loading-error) (grid-row [:i {:style {:color "red"}} loading-error])
       :else                 [:div.row
                              (purchases-list)
                              (history)])]))
