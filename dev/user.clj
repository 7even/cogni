(ns user
  (:require [cogni.core :refer [config]]
            [integrant.repl :refer [go halt]]))

(integrant.repl/set-prep! #(-> (config :development)
                               :ig/system))

(defn system []
  integrant.repl.state/system)

(def reset integrant.repl/reset)
