(ns clojurecraft.core
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.in])
  (:use [clojurecraft.out])
  (:use [clojurecraft.util])
  (:use [clojurecraft.data])
  (:use [clojure.contrib.pprint :only (pprint)])
  (:require [clojurecraft.actions :as act])
  (:import (java.net Socket)
           (java.io DataOutputStream DataInputStream)
           (java.util.concurrent LinkedBlockingQueue)))

(def minecraft-local {:name "localhost" :port 25565})

; Connections ----------------------------------------------------------------------
(def *worlds* (ref {}))
(defn get-world [server]
  (dosync
    (ensure *worlds*)
    (let [world (@*worlds* server)]
      (if world
        world
        (do
          (alter *worlds* assoc server (World. server (ref {}) (ref {}) (ref 0)))
          (@*worlds* server))))))


(defn login [bot username]
  ; Send handshake
  (write-packet bot :handshake {:username username})

  ; Get handshake
  (read-packet bot)

  ; Send login
  (write-packet bot :login {:version 14 :username username})

  ; Get login
  (read-packet bot))


(defn input-handler [bot]
  (let [conn (:connection bot)
        test 1]
    (while (nil? (:exit @conn))
      (read-packet bot)))
  (println "done"))

(defn location-handler [bot]
  (let [conn (:connection bot)
        outqueue (:outqueue bot)]
    (while (nil? (:exit @conn))
      (let [location (:location @(:player bot))]
        (.put outqueue [:playerpositionlook location])
        (Thread/sleep 50)))))

(defn output-handler [bot]
  (let [conn (:connection bot)
        outqueue (:outqueue bot)]
    (while (nil? (:exit @conn))
      (let [[packet-type, payload] (.take outqueue)]
        (write-packet bot packet-type payload)))))


(defn connect [server username]
  (let [socket (Socket. (:name server) (:port server))
        in (DataInputStream. (.getInputStream socket))
        out (DataOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out})
        outqueue (LinkedBlockingQueue.)

        world (get-world server)
        player (ref {:location {:onground false, :pitch 0.0, :yaw 0.0, :z 240.0,
                                :y 85.0, :stance 60.0, :x -120.0}})
        bot {:connection conn, :outqueue outqueue, :player player, :world world,
             :packet-counts-in (atom {}), :packet-counts-out (atom {})}]

    (println "connecting")
    (login bot username)
    (println "connected and logged in")

    (println "starting read handler")
    (doto (Thread. #(input-handler bot)) (.start))

    (println "starting write handler")
    (doto (Thread. #(output-handler bot)) (.start))

    (println "starting location updating handler")
    (doto (Thread. #(location-handler bot)) (.start))

    (println "writing initial keepalive packet")
    (.put outqueue [:keepalive {}])

    (println "all systems go, returning bot")
    bot))

(defn disconnect [bot]
  (dosync (alter (:connection bot) merge {:exit true})))


; Utility functions ----------------------------------------------------------------

; Scratch --------------------------------------------------------------------------
;(def bot (connect minecraft-local "Honeydew"))
;(act/move bot 0 -1 0)
;(pprint @(:packet-counts-in bot))
;(pprint @(:packet-counts-out bot))
;(pprint (:player bot))
;(println (:location @(:player bot)))
;(disconnect bot)

