(ns cogni.db
  (:require [clojure.edn :as edn]
            [clojure.java.io :refer [resource]]
            [datomic.api :as d]))

(def schema
  (->> "schema.edn"
       resource
       slurp
       (edn/read-string {:readers {'db/id datomic.db/id-literal}})))

(defn setup-db [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (d/transact conn schema)
    conn))
