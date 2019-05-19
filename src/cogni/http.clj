(ns cogni.http
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn hello-world [request]
  {:status 200
   :body "Hello, World!"})

(def routes
  (route/expand-routes #{["/hello" :get hello-world :route-name :hello]}))

(def service-map
  {::http/routes routes
   ::http/type :jetty})

(defn start [port join?]
  (-> service-map
      (assoc ::http/port port
             ::http/join? join?)
      http/create-server
      http/start))

(def stop http/stop)
