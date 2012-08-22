(ns metaq.core
  (:require [clojure.tools.logging :as logging])

  (:import [java.util.concurrent  TimeUnit])
  (:import [com.taobao.metamorphosis Message])
  (:import [com.taobao.metamorphosis.client
            MetaClientConfig
            MetaMessageSessionFactory])
  (:import [com.taobao.metamorphosis.client.producer
            MessageProducer])
  (:import [com.taobao.metamorphosis.client.consumer
            ConsumerConfig
            MessageListener])
  (:import [com.taobao.metamorphosis.exception MetaClientException])
  (:import [com.taobao.metamorphosis.utils ZkUtils$ZKConfig]))

(defn zookeeper-based-metaq-config [zkaddr zkroot]
  (let [meta-config (MetaClientConfig.)
        zk-config (ZkUtils$ZKConfig.)]
    (set! (. zk-config zkConnect) zkaddr)
    (set! (. zk-config zkRoot) zkroot)
    (.setZkConfig meta-config zk-config)
    meta-config))

(defn metaq-session-factory [^MetaClientConfig config]
  (MetaMessageSessionFactory. config))

(defn start-producer
  "should be started in main function"
  [zkaddr zkroot]
  (let [config (zookeeper-based-metaq-config zkaddr zkroot)
        factory (metaq-session-factory config)
        producer (.createProducer factory)]
    producer))

(defn publish [^MessageProducer producer topic]
  (.publish producer topic))

(defn produce [^MessageProducer producer topic data]
  (try
    (let [r (.sendMessage producer (Message. topic data)
                               (long 10) TimeUnit/SECONDS)]
      (when-not (.isSuccess r)
        (logging/warn "Error publishing to MQ: " (.getErrorMessage r))))
    (catch MetaClientException e
      (do
        (logging/warn e "Error publishing to MQ.")))))

(defmacro defhandler [name executor arg-vec & handler-body]
  `(def ~name
     (let [executor# ~executor]
       (reify MessageListener
         (^void recieveMessages [this# ^Message  msg#]
           (try
             ((fn ~arg-vec ~@handler-body) (.getData msg#))
             (catch Exception e#
               (logging/warn e# "Error processing message: "
                             (String. (.getData msg#) "UTF-8")))))
         (getExecutor [this]
           executor#)))))

(defn start-consumer
  "should be started in main function"
  [zkaddr zkroot group]
  (let [config (zookeeper-based-metaq-config zkaddr zkroot)
        factory (metaq-session-factory config)
        consumer (.createConsumer
                   ^MetaMessageSessionFactory factory
                   (ConsumerConfig. group))]
    consumer))

(defn subscribe [consumer topic handler]
  (.subscribe consumer topic (* 1024 1024) handler))

(defn subscribe-done [consumer]
  (.completeSubscribe consumer))
