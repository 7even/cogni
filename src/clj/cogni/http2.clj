(ns cogni.http2
  (:require [aleph.http :as http]
            [aleph.netty :refer [wait-for-close]]
            [cogni.db :as db]
            [cogni.html :as html]
            [compojure.core :refer [GET routes]]
            [compojure.route :as route]
            [datomic.api :as d]
            [manifold.bus :as bus]
            [manifold.deferred :as md]
            [manifold.stream :as s]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.resource :refer [wrap-resource]]))

(defn show-index [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body html/page})

(def events-bus
  (bus/event-bus))

(defn broadcast [topic payload]
  (bus/publish! events-bus topic (pr-str payload)))

(defn- send-to-ws [s payload]
  (s/put! s (pr-str payload)))

(defn- handle-client-message [db-conn ws-conn {:keys [command query data] :as message}]
  (cond
    (some? command) (case command
                      :add-purchase (db/add-purchase db-conn (:name data))
                      :retract-purchase (db/retract-purchase db-conn (:name data))
                      (println "Unknown command" command "with payload" data))
    (some? query) (case query
                    :snapshot (let [db (d/as-of (d/db db-conn)
                                                (:t data))
                                    snapshot (db/get-purchases db)]
                                (send-to-ws ws-conn snapshot))
                    (println "Unknown query" query "with payload" data))
    :else (println "Unknown message" message)))

(defn ws-handler [db-conn]
  (fn [req]
    (md/let-flow [conn (http/websocket-connection req)]
      (s/connect (bus/subscribe events-bus :txes)
                 conn)
      (send-to-ws conn {:type :state
                        :data {:purchases (->> (db/get-purchases (d/db db-conn))
                                               (map (fn [[name added-at]]
                                                      {:name name
                                                       :added-at added-at}))
                                               (sort-by :added-at))
                               :history (->> (db/get-history (d/db db-conn))
                                             (map (fn [[t happened-at]]
                                                    {:t t
                                                     :happened-at happened-at})))}})
      (s/consume (fn [payload]
                   (println "Client sent:" payload)
                   (handle-client-message db-conn conn (read-string payload)))
                 conn))))

(defn app-routes [db]
  (routes
   (GET "/" [] show-index)
   (GET "/ws" [] (ws-handler db))
   (route/not-found {:status 404
                     :body "Not found"})))

(defn app [db]
  (-> (app-routes db)
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified))

(defn start [db port join?]
  (let [server (http/start-server (app db) {:port port})]
    (when join?
      (wait-for-close server))
    server))

(defn stop [server]
  (.close server))
