(ns cogni.db
  (:require [datomic.api :as d]))

(def uri
  "datomic:free://localhost:4334/cogni?password=datomic")

(d/create-database uri)

(def conn
  (d/connect uri))

(def schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :purchase/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The name of the purchase"
    :db.install/_attribute :db.part/db}])

(d/transact conn schema)
