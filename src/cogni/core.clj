(ns cogni.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn hello-world [request]
  {:status 200
   :body "Hello, World!"})

(def routes
  (route/expand-routes #{["/hello" :get hello-world :route-name :hello]}))

(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8890})

(defonce server (atom nil))

(defn start-dev []
  (let [new-server (-> service-map
                       (assoc ::http/join? false)
                       http/create-server
                       http/start)]
    (reset! server new-server)))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))
