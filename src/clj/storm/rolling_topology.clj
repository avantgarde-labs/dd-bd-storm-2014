(ns storm.rolling-topology
  (:import (backtype.storm StormSubmitter LocalCluster)
   (jvm.bolt RollingCountBolt IntermediateRankingsBolt TotalRankingsBolt)
   (backtype.storm.testing TestWordSpout))
  (:use [backtype.storm clojure config]
        [utils.redis]
        [clojure.string])
  (:gen-class))

(def TOP_N 5)

(defspout word-spout ["word"]
  [conf context collector]
  (let [words ["storm"
   "development"
   "is"
   "even"
   "more"
   "enjoyable"
   "on"
   "a"
   "MacBook"]]
   (spout
     (nextTuple []
       (Thread/sleep 100)
       (emit-spout! collector [(rand-nth words)])         
       )
     (ack [id]
      ))))

; a spout reading strings from a redis list.

(defspout sentence-spout ["sentence"]
  [conf context collector]
  (spout
   (nextTuple []
     (let [string (lpop-redis "mylist")]
       (cond string
        (emit-spout! collector [string])
        )))

   (ack [id])))

(defbolt split-string ["word"] [tuple collector]
  (let [ line   (.getString tuple 0)
         words (split line #"\s+")]
      (doseq [word words]
        (emit-bolt! collector [word] :anchor tuple)))      
  (ack! collector tuple))

(defn mk-topology []
  (topology
   ;{"spout" (spout-spec word-spout)}
   {"spout" (spout-spec sentence-spout)}
   {"splitter" (bolt-spec {"spout" :shuffle} split-string :p 3)
    ; oh my god java
    "counter" (bolt-spec {"splitter" ["word"]}
     (RollingCountBolt. 9 3)
     :parallelism-hint 4)
   "intermediateRanker" (bolt-spec {"counter" ["obj"]}
     (IntermediateRankingsBolt. TOP_N)
     :parallelism-hint 4)
   "finalRanker" (bolt-spec {"intermediateRanker" :global}
     (TotalRankingsBolt. TOP_N)
     :parallelism-hint 1)}))

(defn run-local! []
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "rolling-top-words" {TOPOLOGY-DEBUG true} (mk-topology))
    ;;(Thread/sleep 10000)
    ;;(.shutdown cluster)
    ))

(defn submit-topology! [name]
  (StormSubmitter/submitTopology
   name
   {TOPOLOGY-DEBUG true
    TOPOLOGY-WORKERS 3}
    (mk-topology)))

(defn -main
  ([]
   (run-local!))
  ([name]
   (submit-topology! name)))

