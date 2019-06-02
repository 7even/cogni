(ns cogni.events
  (:require [re-frame.core :as rf]
            [ajax.edn :as ajax]
            [day8.re-frame.http-fx]))

(rf/reg-event-db ::initialize-db
                 (fn [_ _]
                   {:purchases []
                    :new-purchase ""
                    :loading? true
                    :loading-error nil
                    :duplication-error nil}))

(rf/reg-event-fx ::load-purchases
                 (fn [{db :db} _]
                   {:http-xhrio {:method :get
                                 :uri "http://localhost:8890/purchases"
                                 :format (ajax/edn-request-format)
                                 :response-format (ajax/edn-response-format)
                                 :on-success [::purchases-loaded]
                                 :on-failure [::purchases-failed-to-load]}}))

(rf/reg-event-db ::purchases-loaded
                 (fn [db [_ response]]
                   (-> db
                       (assoc :purchases (mapv :name response))
                       (assoc :loading? false))))

(rf/reg-event-db ::purchases-failed-to-load
                 (fn [db [_ response]]
                   (-> db
                       (assoc :loading-error (str "Failed to load purchases: " response))
                       (assoc :loading? false))))

(rf/reg-event-db ::change-new-purchase
                 (fn [db [_ new-purchase]]
                   (assoc db :new-purchase new-purchase)))

(rf/reg-event-fx ::add-purchase
                 (fn [{db :db} _]
                   {:http-xhrio {:method :post
                                 :uri "http://localhost:8890/purchases"
                                 :params {:name (:new-purchase db)}
                                 :format (ajax/edn-request-format)
                                 :response-format (ajax/edn-response-format)
                                 :on-success [::purchase-added]
                                 :on-failure [::purchase-failed-to-add]}}))

(rf/reg-event-db ::purchase-added
                 (fn [db _]
                   (-> db
                       (update :purchases conj (:new-purchase db))
                       (assoc :new-purchase ""))))

(rf/reg-event-fx ::retract-purchase
                 (fn [{db :db} [_ purchase-name]]
                   {:http-xhrio {:method :delete
                                 :uri (str "http://localhost:8890/purchases/" purchase-name)
                                 :format (ajax/edn-request-format)
                                 :response-format (ajax/edn-response-format)
                                 :on-success [::purchase-retracted purchase-name]
                                 :on-failure [::purchase-failed-to-retract]}}))

(rf/reg-event-db ::purchase-retracted
                 (fn [db [_ deleted-purchase]]
                   (update db :purchases (fn [purchases]
                                           (->> purchases
                                                (remove (partial = deleted-purchase))
                                                vec)))))
