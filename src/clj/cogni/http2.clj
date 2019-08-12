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
            [ring.middleware.resource :refer [wrap-resource]]))

(defn show-index [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body html/page})

(def events-bus
  (bus/event-bus))

(defn- broadcast [topic payload]
  (bus/publish! events-bus topic (pr-str payload)))

(defn- send-to-ws [s payload]
  (s/put! s (pr-str payload)))

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
                                             (map (fn [[t when]]
                                                    {:t t
                                                     :when when})))}}))))

(defn app-routes [db]
  (routes
   (GET "/" [] show-index)
   (GET "/ws" [] (ws-handler db))
   (route/not-found {:status 404
                     :body "Not found"})))

(defn app [db]
  (wrap-resource (app-routes db)
                 "public"))

(defn start [db port join?]
  (let [server (http/start-server (app db) {:port port})]
    (when join?
      (wait-for-close server))
    server))

(defn stop [server]
  (.close server))
