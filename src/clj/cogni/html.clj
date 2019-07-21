(ns cogni.html
  (:require [hiccup.page :refer [html5 include-css]]))

(def page
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title "Cogni"]
    (include-css "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css")]
   [:body
    [:div#root]
    [:script {:src "/js/main.js"}]]))
