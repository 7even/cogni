(ns cogni.broadcaster
  (:require [clojure.core.async :refer [thread]]
            [cogni.db :as db]
            [cogni.websocket :as ws]))

(defn- broadcast [queue enabled]
  (thread
    (while @enabled
      (let [report (.take queue)
            changes (db/read-changes report)]
        (doseq [[_ name added?] changes]
          (ws/broadcast (if added?
                          {:event :purchase-added
                           :data {:name name}}
                          {:event :purchase-retracted
                           :data {:name name}})))))))

(defn start-watcher [db-conn]
  (let [enabled (atom true)
        queue (db/get-queue db-conn)]
    (broadcast queue enabled)
    {:enabled enabled
     :db db-conn}))

(def stop-watcher db/remove-queue)
