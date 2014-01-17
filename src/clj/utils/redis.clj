(ns utils.redis
  (:require [taoensso.carmine :as car]))

; REDIS related stuff
(def pool         (car/make-conn-pool)) ; See docstring for additional options
(def redis-server1 (car/make-conn-spec :host "127.0.0.1" :port 6379))

(defmacro wcar [& body] `(car/with-conn pool redis-server1 ~@body))

(defn lpop-redis[key-value]
  (wcar (car/lpop key-value)))

(defn rpush-redis[list-name value]
  (wcar (car/rpush list-name value)))