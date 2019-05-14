(ns cogni.core
  (:require [aero.core :as aero]
            [clojure.java.io :refer [resource]]
            [cogni.db :as db]
            [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn config
  ([] (config :development))
  ([profile]
   (-> "config.edn"
       resource
       (aero/read-config {:profile profile}))))

(defn hello-world [request]
  {:status 200
   :body "Hello, World!"})

(def routes
  (route/expand-routes #{["/hello" :get hello-world :route-name :hello]}))

(def service-map
  {::http/routes routes
   ::http/type :jetty})

(defmethod ig/init-key :datomic/client [_ {:keys [uri]}]
  (println ";; Starting Datomic client")
  (db/setup-db uri))

(defmethod ig/init-key :http/handler [_ {:keys [port join?]}]
  (println ";; Starting HTTP handler")
  (-> service-map
      (assoc ::http/port port
             ::http/join? join?)
      http/create-server
      http/start))

(defmethod ig/halt-key! :http/handler [_ server]
  (println ";; Stopping HTTP handler")
  (http/stop server))
