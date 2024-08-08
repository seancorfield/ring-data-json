(defproject com.github.seancorfield/ring-data-json "0.5.2"
  :description "Ring middleware for handling JSON"
  :url "https://github.com/seancorfield/ring-data-json"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "2.5.0"]
                 [ring/ring-core "1.12.2"]]
  :plugins [[codox "0.10.8"]]
  :aliases {"test-all" ["with-profile" "default:+1.8:+1.9:+1.10:+1.11" "test"]}
  :profiles
  {:1.8  {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9  {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}
   :1.11 {:dependencies [[org.clojure/clojure "1.11.4"]]}})
