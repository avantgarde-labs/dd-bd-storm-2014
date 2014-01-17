(ns storm.rolling-topology
  (:import (backtype.storm StormSubmitter LocalCluster)
   (jvm.bolt RollingCountBolt IntermediateRankingsBolt TotalRankingsBolt)
   (com.hmsonline.storm.contrib.bolt.elasticsearch.mapper DefaultTupleMapper)
   (com.hmsonline.storm.contrib.bolt.elasticsearch ElasticSearchBolt)
   (jvm.tools Rankable)
   (jvm.tools RankableObjectWithFields)
   (backtype.storm.testing TestWordSpout)
   (jvm.tools Rankings))
  (:use [backtype.storm clojure config]
        [utils.redis]
        [clojure.string])
  (:require [clojure.data.json :as json])
  (:gen-class))

(def TOP_N 5)
(def REDIS_LIST "mylist")

(defspout sentence-spout ["sentence"]
  [conf context collector]
  (spout
   (nextTuple []
     (let [string (lpop-redis REDIS_LIST)]
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

(defbolt to-elasticsearch ["document" "index" "type" "id"] [tuple collector]
  (let [rankings (.getValue tuple 0)
        rankingList (.getRankings rankings)]
      (doseq [rank rankingList]
        (let [rankedObject (.getObject rank)
              rankedCount (.getCount rank)
              time_stamp (System/currentTimeMillis)
              id (str (str time_stamp) (.toString rankedObject))
              document (json/write-str {:id id :token rankedObject :rankedCount rankedCount :time time_stamp})
              ]
          (emit-bolt! collector [document "storm-test" "rollingtoken" id] :anchor tuple)
          )
        )
      )
  (ack! collector tuple)
)


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
     :parallelism-hint 1)
   
   "toElasticSearch" (bolt-spec {"finalRanker" :shuffle} to-elasticsearch :p 3)
   
   "finalIndexer" (bolt-spec {"toElasticSearch" :shuffle}
     (ElasticSearchBolt. (new DefaultTupleMapper))
     :parallelism-hint 1)
   }))

(defn run-local! []
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "rolling-top-words" {
      TOPOLOGY-DEBUG true
      "elastic.search.cluster" "elasticsearch"
      "elastic.search.host" "192.168.178.26"
      "elastic.search.port" 9300
      } (mk-topology))
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

