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
                {:purchases (->> (db/get-purchases (d/db db-conn))
                                 (map (fn [[name added-at]]
                                        {:name name
                                         :added-at added-at}))
                                 (sort-by :added-at))})
    (swap! ws-clients assoc ws-session send-ch)))

(defn send-message-to-all! [message]
  (doseq [[^Session session channel] @ws-clients]
    (when (.isOpen session)
      (send-to-ws channel {:text message}))))

(defn ws-paths [db]
  {"/ws" {:on-connect (ws/start-ws-connection (new-ws-client db))
          :on-text (fn [msg]
                     (println (str "A client sent - " msg)))
          :on-binary (fn [payload offset length]
                       (println "Binary Message!")
                       (clojure.pprint/pprint (:bytes payload)))
          :on-error (fn [t]
                      (println "WS Error happened")
                      (clojure.pprint/pprint (:exception t)))
          :on-close (fn [num-code reason-text]
                      (println "WS Closed:")
                      (clojure.pprint/pprint reason-text))}})
