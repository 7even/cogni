(set-env! :source-paths #{"src"}
          :resource-paths #{"resources"}
          :dependencies '[[org.clojure/clojure "1.9.0"]

                          ;; System
                          [integrant "0.7.0"]
                          [integrant/repl "0.3.1"]

                          ;; Configuration
                          [aero "1.1.3"]

                          ;; HTTP
                          [io.pedestal/pedestal.service "0.5.5"]
                          [io.pedestal/pedestal.route "0.5.5"]
                          [io.pedestal/pedestal.jetty "0.5.5"]

                          ;; Datomic
                          [com.datomic/datomic-free "0.9.5697"]])

(task-options!
 repl {:init-ns 'user})

(replace-task!
 [r repl]
 (fn [& xs]
   (merge-env! :source-paths #{"dev"})
   (apply r xs)))
