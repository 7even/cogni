(ns cogni.websocket
  (:require [clojure.core.async :as async]
            [cogni.db :as db]
            [datomic.api :as d]
            [io.pedestal.http.jetty.websockets :as ws])
  (:import org.eclipse.jetty.websocket.api.Session))

(def ws-clients (atom {}))

(defn send-to-ws [ch payload]
  (async/put! ch (pr-str payload)))

(defn new-ws-client [db-conn]
  (fn [ws-session send-ch]
    (send-to-ws send-ch
                {:type :state
                 :data {:purchases (->> (db/get-purchases (d/db db-conn))
                                        (map (fn [[name added-at]]
                                               {:name name
                                                :added-at added-at}))
                                        (sort-by :added-at))
                        :history (->> (db/get-history (d/db db-conn))
                                      (map (fn [[t when]]
                                             {:t t
                                              :when when})))}})
    (swap! ws-clients assoc ws-session send-ch)))

(defn send-message-to-all! [message]
  (doseq [[^Session session channel] @ws-clients]
    (when (.isOpen session)
      (send-to-ws channel {:type :alert
                           :data message}))))

(defn broadcast [payload]
  (doseq [[^Session session channel] @ws-clients]
    (when (.isOpen session)
      (send-to-ws channel payload))))

(defn- handle-client-message [db {:keys [command query data] :as message}]
  (cond
    (some? command) (case command
                      :add-purchase (db/add-purchase db (:name data))
                      :retract-purchase (db/retract-purchase db (:name data))
                      (println "Unknown command" command "with payload" data))
    (some? query) (case query
                    :snapshot (println "We have a query!")
                    (println "Unknown query" query "with payload" data))
    :else (println "Unknown message" message)))

(defn ws-paths [db]
  {"/ws" {:on-connect (ws/start-ws-connection (new-ws-client db))
          :on-text (fn [payload]
                     (println "Client sent:" payload)
                     (handle-client-message db (read-string payload)))
          :on-binary (fn [payload offset length]
                       (println "Binary Message!")
                       (clojure.pprint/pprint (:bytes payload)))
          :on-error (fn [t]
                      (println "WS Error happened")
                      (clojure.pprint/pprint (:exception t)))
          :on-close (fn [num-code reason-text]
                      (println "WS Closed:")
                      (clojure.pprint/pprint reason-text))}})
