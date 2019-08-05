(ns cogni.events
  (:require [re-frame.core :as rf]
            [ajax.edn :as ajax]
            [cljs.pprint :refer [pprint]]
            [day8.re-frame.http-fx]
            [wscljs.client :as ws]
            [wscljs.format :as fmt]
            [cljs-time.coerce :as c]))

(defonce ws-connection (atom nil))

(defn handle-server-message [{:keys [type data]}]
  (case type
    :state (rf/dispatch [::state-loaded data])
    :transaction (rf/dispatch [::new-transaction data])
    (println "Server sent a message with unexpected type:" type)))

(rf/reg-event-fx ::initialize-ws
                 (fn [_ _]
                   (println "Initializing WS connection")
                   (reset! ws-connection
                           (ws/create (str "ws://" (.. js/window -location -host) "/ws")
                                      {:on-open #(println "Connected")
                                       :on-close #(println "Disconnected")
                                       :on-message (fn [e]
                                                     (let [data (cljs.reader/read-string (.-data e))]
                                                       (println "Message from server:")
                                                       (pprint data)
                                                       (handle-server-message data)))}))
                   {}))

(rf/reg-fx :send-to-ws
           (fn [payload]
             (ws/send @ws-connection payload fmt/edn)))

(rf/reg-event-db ::initialize-db
                 (fn [_ _]
                   {:purchases []
                    :history []
                    :new-purchase ""
                    :loading? true
                    :loading-error nil
                    :duplication-error nil}))

(rf/reg-event-db ::state-loaded
                 (fn [db [_ {:keys [purchases history]}]]
                   (-> db
                       (assoc :purchases (mapv :name purchases))
                       (assoc :history (map #(update % :when c/from-date)
                                            history))
                       (assoc :loading? false))))

(defn- handle-change [db [_ name added?]]
  (if added?
    (cond-> db
      true (update :purchases conj name)
      (= name (:new-purchase db)) (assoc :new-purchase ""))
    (update db :purchases (fn [purchases]
                            (->> purchases
                                 (remove (partial = name))
                                 vec)))))

(rf/reg-event-db ::new-transaction
                 (fn [db [_ {:keys [t when changes]}]]
                   (-> (reduce handle-change db changes)
                       (update :history conj {:t t
                                              :when (c/from-date when)}))))

(rf/reg-event-db ::change-new-purchase
                 (fn [db [_ new-purchase]]
                   (assoc db :new-purchase new-purchase)))

(rf/reg-event-fx ::add-purchase
                 (fn [{db :db} _]
                   {:send-to-ws {:event :add-purchase
                                 :data {:name (:new-purchase db)}}}))

(rf/reg-event-fx ::retract-purchase
                 (fn [{db :db} [_ purchase-name]]
                   {:send-to-ws {:event :retract-purchase
                                 :data {:name purchase-name}}}))
