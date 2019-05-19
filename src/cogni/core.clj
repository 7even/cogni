(ns cogni.core
  (:require [aero.core :as aero]
            [clojure.java.io :refer [resource]]
            [cogni.db :as db]
            [cogni.http :as http]
            [integrant.core :as ig]))

(defn config
  ([] (config :development))
  ([profile]
   (-> "config.edn"
       resource
       (aero/read-config {:profile profile}))))

(defmethod ig/init-key :datomic/client [_ {:keys [uri]}]
  (println ";; Starting Datomic client")
  (db/setup-db uri))

(defmethod ig/init-key :http/handler [_ {:keys [port join?]}]
  (println ";; Starting HTTP handler")
  (http/start port join?))

(defmethod ig/halt-key! :http/handler [_ server]
  (println ";; Stopping HTTP handler")
  (http/stop server))
