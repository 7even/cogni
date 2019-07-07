(ns cogni.http
  (:require [clojure.string :as str]
            [cogni.db :as db]
            [cogni.html :as html]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.http.route :as route]))

(defn show-index [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body html/page})

(defn list-purchases [{:keys [::db/value]}]
  (->> (db/get-purchases value)
       (map (fn [[name added-at]]
              {:name name
               :added-at added-at}))
       (sort-by :added-at)
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
  (vec (concat [route/path-params-decoder
                (body-params)
                (db/attach-database db)]
               interceptors)))

(defn routes [db]
  (route/expand-routes
   #{["/" :get show-index :route-name :index]
     ["/purchases" :get (add-interceptors db list-purchases) :route-name :purchases]
     ["/purchases" :post (add-interceptors db add-purchase) :route-name :add-purchase]
     ["/purchases/:name" :delete (add-interceptors db retract-purchase) :route-name :retract-purchase]}))

(def log-request
  "Log the request's method and uri."
  {:name ::log-request
   :enter (fn [request]
            (println (format "%s %s"
                             (str/upper-case (name (get-in request [:request :request-method])))
                             (get-in request [:request :uri])))
            request)})

(defn start [db public-host port join?]
  (-> {::http/routes (routes db)
       ::http/host "0.0.0.0"
       ::http/port port
       ::http/allowed-origins [(str "http://" public-host)]
       ::http/file-path "public"
       ::http/request-logger log-request
       ::http/secure-headers nil
       ::http/join? join?
       ::http/type :jetty}
      http/create-server
      http/start))

(def stop http/stop)
