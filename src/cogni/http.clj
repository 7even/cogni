(ns cogni.http
  (:require [cogni.db :refer [attach-database]]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn hello-world [request]
  {:status 200
   :body "Hello, World!"})

(defn routes [db]
  (route/expand-routes #{["/hello" :get hello-world :route-name :hello]}))

(defn start [db port join?]
  (-> {::http/routes (routes db)
       ::http/port port
       ::http/join? join?
       ::http/type :jetty}
      http/create-server
      http/start))

(def stop http/stop)
