(ns cogni.core
  (:gen-class)
  (:require [aero.core :as aero]
            [clojure.java.io :refer [resource]]
            [cogni.broadcaster :as broadcaster]
            [cogni.db :as db]
            [cogni.http :as http]
            [integrant.core :as ig]))

(defmethod aero/reader 'ig/ref [_ _ value]
  (ig/ref value))

(defn config
  ([] (config :development))
  ([profile]
   (-> "config.edn"
       resource
       (aero/read-config {:profile profile}))))

(defmethod ig/init-key :datomic/client [_ {:keys [uri]}]
  (println ";; Starting Datomic client")
  (db/setup-db uri))

(defmethod ig/init-key :events/broadcaster [_ {:keys [db]}]
  (println ";; Starting events broadcaster")
  (broadcaster/start-watcher db))

(defmethod ig/halt-key! :events/broadcaster [_ {:keys [enabled db]}]
  (println ";; Stopping events broadcaster")
  (reset! enabled false)
  (broadcaster/stop-watcher db))

(defmethod ig/init-key :http/handler [_ {:keys [db port join?]}]
  (println ";; Starting HTTP handler")
  (http/start db port join?))

(defmethod ig/halt-key! :http/handler [_ server]
  (println ";; Stopping HTTP handler")
  (http/stop server))

(defn -main []
  (let [system-config (-> (config :production)
                          :ig/system)]
    (ig/init system-config)))
