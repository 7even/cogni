(ns cogni.broadcaster
  (:require [clojure.core.async :refer [thread]]
            [cogni.db :as db]
            [cogni.http :as http]))

(defn- broadcast [queue enabled]
  (thread
    (while @enabled
      (let [report (.take queue)
            {:keys [t changes]} (db/read-changes report)
            happened-at (->> (:tx-data report)
                             (filter (fn [[e a v tx]]
                                       (= e tx)))
                             (map #(nth % 2))
                             first)]
        (http/broadcast :txes
                        {:type :transaction
                         :data {:t t
                                :happened-at happened-at
                                :changes changes}})))))

(defn start-watcher [db-conn]
  (let [enabled (atom true)
        queue (db/get-queue db-conn)]
    (broadcast queue enabled)
    {:enabled enabled
     :db db-conn}))

(def stop-watcher db/remove-queue)
