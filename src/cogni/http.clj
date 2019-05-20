(ns cogni.http
  (:require [cogni.db :as db]
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

(defn routes [db]
  (route/expand-routes
   #{["/hello" :get hello-world :route-name :hello]
     ["/purchases" :get [(db/attach-database db) list-purchases] :route-name :purchases]}))

(defn start [db port join?]
  (-> {::http/routes (routes db)
       ::http/port port
       ::http/join? join?
       ::http/type :jetty}
      http/create-server
      http/start))

(def stop http/stop)
