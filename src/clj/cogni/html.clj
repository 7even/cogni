(ns cogni.html
  (:require [hiccup.page :refer [html5]]))

(def page
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title "Cogni"]]
   [:body
    [:div#root]
    [:script {:src "/js/main.js"}]]))
