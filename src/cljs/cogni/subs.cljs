(ns cogni.subs
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-sub ::purchases
            (fn [db]
              (:purchases db)))

(rf/reg-sub ::history
            (fn [db]
              (:history db)))

(rf/reg-sub ::t
            (fn [db]
              (:current-t db)))

(rf/reg-sub ::viewing-snapshot?
            (fn [db]
              (some? (:current-t db))))

(rf/reg-sub ::new-purchase
            (fn [db]
              (:new-purchase db)))

(rf/reg-sub ::loading?
            (fn [db]
              (:loading? db)))

(rf/reg-sub ::loading-error
            (fn [db]
              (:loading-error db)))

(rf/reg-sub ::duplication-error
            (fn []
              [(rf/subscribe [::purchases])
               (rf/subscribe [::new-purchase])])
            (fn [[purchases new-purchase]]
              (when (seq (filter #(= new-purchase %) purchases))
                "This purchase is already on the list")))

(rf/reg-sub ::cant-add-purchase?
            (fn []
              [(rf/subscribe [::duplication-error])
               (rf/subscribe [::new-purchase])
               (rf/subscribe [::viewing-snapshot?])])
            (fn [[duplication-error new-purchase viewing-snapshot?]]
              (or (some? duplication-error)
                  (str/blank? new-purchase)
                  viewing-snapshot?)))
