(ns cogni.events
  (:require [re-frame.core :as rf]
            [ajax.edn :as ajax]
            [day8.re-frame.http-fx]
            [wscljs.client :as ws]
            [wscljs.format :as fmt]))

(defonce ws-connection (atom nil))

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
                                                       (cljs.pprint/pprint data)
                                                       (rf/dispatch [::purchases-loaded
                                                                     (:purchases data)])))}))
                   {}))

(rf/reg-fx :send-to-ws
           (fn [payload]
             (ws/send @ws-connection payload fmt/edn)))

(rf/reg-event-db ::initialize-db
                 (fn [_ _]
                   {:purchases []
                    :new-purchase ""
                    :loading? true
                    :loading-error nil
                    :duplication-error nil}))

(rf/reg-event-db ::purchases-loaded
                 (fn [db [_ response]]
                   (-> db
                       (assoc :purchases (mapv :name response))
                       (assoc :loading? false))))

(rf/reg-event-db ::change-new-purchase
                 (fn [db [_ new-purchase]]
                   (assoc db :new-purchase new-purchase)))

(rf/reg-event-fx ::add-purchase
                 (fn [{db :db} _]
                   {:send-to-ws {:event :add-purchase
                                 :data {:name (:new-purchase db)}}}))

(rf/reg-event-db ::purchase-added
                 (fn [db _]
                   (-> db
                       (update :purchases conj (:new-purchase db))
                       (assoc :new-purchase ""))))

(rf/reg-event-fx ::retract-purchase
                 (fn [{db :db} [_ purchase-name]]
                   {:send-to-ws {:event :retract-purchase
                                 :data {:name purchase-name}}}))

(rf/reg-event-db ::purchase-retracted
                 (fn [db [_ deleted-purchase]]
                   (update db :purchases (fn [purchases]
                                           (->> purchases
                                                (remove (partial = deleted-purchase))
                                                vec)))))
