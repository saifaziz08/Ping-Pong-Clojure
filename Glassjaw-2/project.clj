(defproject Glassjaw "1.0.0-SNAPSHOT"
  :description "Glassjaw"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [quil "3.1.0"]
                 [http-kit "2.3.0"]
                 [org.clojure/core.async "0.6.532"]
                 [clj-http "3.10.0" :exclusions [cheshire]]
                 [clj-time "0.14.0"]
                 [org.clojure/data.json "0.2.7"]
                 [compojure "1.6.0"]])
