(defproject dd-bd-storm-2014 "0.1.0-SNAPSHOT"
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm" "test/jvm"]
  :test-paths ["test/clj"]
  :javac-options     ["-target" "1.7" "-source" "1.7"]
  :resources-path "multilang"
  :main storm.rolling-topology
  :aot :all
  :min-lein-version "2.0.0"
  :dependencies [[org.slf4j/slf4j-log4j12 "1.6.1"]
                 [commons-collections/commons-collections "3.2.1"]
                 [com.taoensso/carmine "2.4.4"]]

  :profiles {:dev {:dependencies [[storm "0.8.2"]
                     [org.clojure/clojure "1.5.1"]
                     [junit/junit "4.11"]
                     [org.testng/testng "6.1.1"]]}})
