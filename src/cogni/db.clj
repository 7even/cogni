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
  (if (d/create-database uri)
    (let [conn (d/connect uri)]
      @(d/transact conn schema)
      conn)
    (d/connect uri)))

;;; Pedestal interceptor
(defn attach-database [db-conn]
  {:name ::attach-database
   :enter (fn [context]
            (-> context
                (assoc-in [:request ::conn] db-conn)
                (assoc-in [:request ::value] (d/db db-conn))))})

(comment
  ;; check if schema is present
  (d/q '[:find ?e
         :where [?e :db/ident :purchase/name]]
       (d/db (user/db-conn)))
  ;; get all purchase names
  (d/q '[:find ?name
         :where [_ :purchase/name ?name]]
       (d/db (user/db-conn))))
