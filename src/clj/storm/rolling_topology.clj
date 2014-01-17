(ns storm.rolling-topology
  (:import (backtype.storm StormSubmitter LocalCluster)
           (jvm.bolt RollingCountBolt IntermediateRankingsBolt TotalRankingsBolt)
           (backtype.storm.testing TestWordSpout))
  (:use [backtype.storm clojure config])
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

(defn mk-topology []
  (topology
   {"spout" (spout-spec word-spout)}
   {"counter" (bolt-spec {"spout" ["word"]}
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

