(set-env! :source-paths #{"src/clj" "src/cljs"}
          :resource-paths #{"resources"}
          :dependencies '[[org.clojure/clojure "1.9.0"]

                          ;; System
                          [integrant "0.7.0"]
                          [integrant/repl "0.3.1"]

                          ;; Configuration
                          [aero "1.1.3"]

                          ;; HTTP
                          [compojure "1.6.1"]
                          [aleph "0.4.6"]
                          [hiccup "1.0.5"]

                          ;; Datomic
                          [com.datomic/datomic-free "0.9.5697" :exclusions [com.google.guava/guava]]

                          ;; Clojurescript
                          [thheller/shadow-cljs "2.8.37"]
                          [reagent "0.8.1"]
                          [re-frame "0.10.6"]
                          [day8.re-frame/http-fx "0.1.6"]
                          [nilenso/wscljs "0.2.0"]
                          [com.andrewmcveigh/cljs-time "0.5.2"]])

(require '[shadow.cljs.devtools.api :as shadow])

(task-options!
 repl {:init-ns 'user}
 aot {:namespace '#{cogni.core}}
 jar {:file "cogni.jar"
      :main 'cogni.core})

(replace-task!
 [r repl]
 (fn [& xs]
   (merge-env! :source-paths #{"dev"})
   (apply r xs)))

(deftask build-clj
  "Build a production release of backend."
  []
  (comp (uber)
        (aot)
        (jar)
        (target)))

(deftask build-cljs
  "Build a production release of frontend."
  []
  (shadow/release :dev))
