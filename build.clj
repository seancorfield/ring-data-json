(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'com.github.seancorfield/ring-data-json)
(def version "0.5.3") ; version in progress
(def snapshot (str version "-SNAPSHOT"))
(def class-dir "target/classes")

(defn test "Run all the tests." [opts]
  (let [basis    (b/create-basis {:aliases (into [:test] (:aliases opts))})
        cmds     (b/java-command
                  {:basis      basis
                   :main      'clojure.main
                   :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn test-all "Run the tests for multiple Clojure versions." [opts]
  (doseq [version [:1.9 :1.10 :1.11 :1.12]]
    (println "\nTesting with Clojure" (name version))
    (test (assoc opts :aliases [version]))))

(defn- pom-template [version]
  [[:description "Ring middleware for handling JSON, using clojure.data.json."]
   [:url "https://github.com/seancorfield/ring-data-json"]
   [:licenses
    [:license
     [:name "MIT"]
     [:url "https://opensource.org/license/MIT"]]]
   [:developers
    [:developer
     [:name "James Reeves"]]
    [:developer
     [:name "Sean Corfield"]]]
   [:scm
    [:url "https://github.com/seancorfield/ring-data-json"]
    [:connection "scm:git:https://github.com/seancorfield/ring-data-json.git"]
    [:developerConnection "scm:git:ssh:git@github.com:seancorfield/ring-data-json.git"]
    [:tag (str "v" version)]]])

(defn- jar-opts [opts]
  (let [version (if (:snapshot opts) snapshot version)]
    (assoc opts
           :lib lib   :version version
           :jar-file  (format "target/%s-%s.jar" lib version)
           :basis     (b/create-basis {})
           :class-dir class-dir
           :target    "target"
           :src-dirs  ["src"]
           :pom-data  (pom-template version))))

(defn ci "Run the CI pipeline of tests (and build the JAR)." [opts]
  (test-all opts)
  (b/delete {:path "target"})
  (let [opts (jar-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["src"] :target-dir class-dir})
    (println "\nBuilding JAR..." (:jar-file opts))
    (b/jar opts))
  opts)

(defn install "Install the JAR locally." [opts]
  (let [opts (jar-opts opts)]
    (b/install opts))
  opts)

(defn deploy "Deploy the JAR to Clojars." [opts]
  (let [{:keys [jar-file] :as opts} (jar-opts opts)]
    (dd/deploy {:installer :remote :artifact (b/resolve-path jar-file)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
