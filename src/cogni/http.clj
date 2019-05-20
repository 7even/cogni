(ns cogni.http
  (:require [clojure.edn :as edn]
            [cogni.db :as db]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn hello-world [request]
  {:status 200
   :body "Hello, World!"})

(defn list-purchases [{:keys [::db/value]}]
  (->> (db/get-purchases value)
       (map (fn [[id name added-at]]
              {:id id
               :name name
               :added-at added-at}))
       http/edn-response))

(defn add-purchase [{:keys [::db/conn body] :as req}]
  (let [params (-> body slurp edn/read-string)
        name (:name params)]
    (db/add-purchase conn name)
    {:status 201}))

(defn routes [db]
  (route/expand-routes
   #{["/hello" :get hello-world :route-name :hello]
     ["/purchases" :get [(db/attach-database db) list-purchases] :route-name :purchases]
     ["/purchases" :post [(db/attach-database db) add-purchase] :route-name :add-purchase]}))

(defn start [db port join?]
  (-> {::http/routes (routes db)
       ::http/port port
       ::http/join? join?
       ::http/type :jetty}
      http/create-server
      http/start))

(def stop http/stop)
