(ns cogni.http
  (:require [cogni.db :as db]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.http.cors :refer [allow-origin]]
            [io.pedestal.http.route :as route]))

(defn list-purchases [{:keys [::db/value]}]
  (->> (db/get-purchases value)
       (map (fn [[id name added-at]]
              {:id id
               :name name
               :added-at added-at}))
       http/edn-response))

(defn add-purchase [{:keys [::db/conn]
                     {:keys [name]} :edn-params}]
  (db/add-purchase conn name)
  {:status 201})

(defn retract-purchase [{:keys [::db/conn]
                         {:keys [name]} :path-params}]
  (db/retract-purchase conn name)
  {:status 204})

(defn add-interceptors [db & interceptors]
  (vec (concat [(allow-origin ["http://localhost:8891"])
                (body-params)
                (db/attach-database db)]
               interceptors)))

(defn routes [db]
  (route/expand-routes
   #{["/purchases" :get (add-interceptors db list-purchases) :route-name :purchases]
     ["/purchases" :post (add-interceptors db add-purchase) :route-name :add-purchase]
     ["/purchases/:name" :delete (add-interceptors db retract-purchase) :route-name :retract-purchase]}))

(defn start [db port join?]
  (-> {::http/routes (routes db)
       ::http/port port
       ::http/join? join?
       ::http/type :jetty}
      http/create-server
      http/start))

(def stop http/stop)
