(ns cogni.core
  (:require [reagent.core :as ra]
            [re-frame.core :as rf]
            [cogni.events :as events]
            [cogni.views :as views]))

(defn render []
  (ra/render [views/ui]
             (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::events/load-purchases])
  (render))
