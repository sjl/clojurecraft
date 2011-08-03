(ns clojurecraft.core
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.in])
  (:use [clojurecraft.out])
  (:use [clojurecraft.util])
  (:use [clojure.contrib.pprint :only (pprint)])
  (:require [clojurecraft.actions :as act])
  (:require (clojurecraft.data))
  (:import [clojurecraft.data Location Entity Block Chunk World Bot])
  (:import (java.net Socket)
           (java.io DataOutputStream DataInputStream)
           (java.util.concurrent LinkedBlockingQueue)))

(def STARTING-LOC (Location. 0 0 0 0 0 0 false))

; Worlds ---------------------------------------------------------------------------
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


; Connections ----------------------------------------------------------------------

(defn- random-username []
   (apply str (repeatedly 10 #(rand-nth "abcdefghijklmnopqrstuvwxyz"))))

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
  (let [conn (:connection bot)]
    (while (nil? (:exit @conn))
      (read-packet bot)))
  (println "done"))

(defn location-handler [bot]
  (let [conn (:connection bot)
        outqueue (:outqueue bot)]
    (while (nil? (:exit @conn))
      (let [location (:loc @(:player bot))]
        (.put outqueue [:playerpositionlook location])
        (Thread/sleep 50)))))

(defn output-handler [bot]
  (let [conn (:connection bot)
        outqueue (:outqueue bot)]
    (while (nil? (:exit @conn))
      (let [[packet-type, payload] (.take outqueue)]
        (write-packet bot packet-type payload)))))


(defn connect [server username]
  (let [username (or username (random-username))
        socket (Socket. (:name server) (:port server))
        in (DataInputStream. (.getInputStream socket))
        out (DataOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out})
        outqueue (LinkedBlockingQueue.)
        world (get-world server)
        bot (Bot. conn outqueue nil world
                  (atom {}) (atom {}))]

    (println "connecting")

    ; We need to log in to find out our bot's entity ID, so we delay creation of the
    ; player until then.
    (let [player-id (:eid (login bot username))
          player (ref (Entity. player-id STARTING-LOC false))
          bot (assoc bot :player player)]

      ; Theoretically another connected bot could fill in the player's entity entry
      ; in the world before we get here, but in practice it probably doesn't matter
      ; because we're going to fill it in anyway.
      ;
      ; The fact that there could be a ref thrown away is troubling.
      ;
      ; TODO: Think more about this.
      (dosync (alter (:entities world) assoc player-id player))

      (println "connected and logged in")

      (println "queueing initial keepalive packet")
      (.put outqueue [:keepalive {}])

      (println "starting read handler")
      (.start (Thread. #(input-handler bot)))

      (println "starting write handler")
      (.start (Thread. #(output-handler bot)))

      (println "starting location updating handler")
      (.start (Thread. #(location-handler bot)))

      (println "all systems go!")

      bot)))

(defn disconnect [bot]
  (dosync (alter (:connection bot) merge {:exit true})))


