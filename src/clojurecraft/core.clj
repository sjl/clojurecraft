(ns clojurecraft.core
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.in])
  (:use [clojurecraft.out])
  (:use [clojurecraft.util])
  (:use [clojure.contrib.pprint :only (pprint)])
  (:require [clojurecraft.chunks :as chunks])
  (:require [clojurecraft.physics :as physics])
  (:require [clojurecraft.actions :as act])
  (:require (clojurecraft.data))
  (:import [clojurecraft.data Location Entity Block Chunk World Bot])
  (:import (java.net Socket)
           (java.io DataOutputStream DataInputStream)
           (java.util.concurrent LinkedBlockingQueue TimeUnit)))

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
  (read-packet bot nil nil nil)

  ; Send login
  (write-packet bot :login {:version 14 :username username})

  ; Get login
  (get (read-packet bot nil nil nil) 1))


(defn input-handler [bot]
  (let [conn (:connection bot)]
    (loop [prevs [nil nil nil]]
      (when (nil? (:exit @conn))
        (recur (read-packet bot (get prevs 0) (get prevs 1) (get prevs 2))))))
  (println "done - input handler")
  (println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))


(defn update-location [bot]
  (when (chunks/current bot)
    (dosync
      (let [player (:player bot)
            loc (:loc @player)
            [bounds-min bounds-max] (physics/player-bounds loc)
            new-data-y (physics/update-loc-y bot bounds-min bounds-max)
            {new-y :y new-onground :onground new-velocity :vel} new-data-y]
        (alter player assoc :velocity new-velocity)
        (alter player assoc-in [:loc :y] new-y)
        (alter player assoc-in [:loc :stance] (+ new-y physics/CHAR-HEIGHT-EYES))
        (alter player assoc-in [:loc :onground] new-onground)))))

(defn location-handler [bot]
  (let [conn (:connection bot)
        outqueue ^LinkedBlockingQueue (:outqueue bot)]
    (while (nil? (:exit @conn))
      (let [player (:player bot)
            location (:loc @player)]
        (when (not (nil? location))
          (.put outqueue [:playerpositionlook location])
          (update-location bot))
        (Thread/sleep 50))))
  (println "done - location handler")
  (println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))

(defn output-handler [bot]
  (let [conn (:connection bot)
        outqueue ^LinkedBlockingQueue (:outqueue bot)]
    (while (nil? (:exit @conn))
      (let [packet (.poll outqueue 1 TimeUnit/SECONDS)]
        (when packet
          (let [[packet-type, payload] packet]
            (write-packet bot packet-type payload))))))
  (println "done - output handler")
  (println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))

(defn action-handler [bot]
  (let [conn (:connection bot)
        actionqueue ^LinkedBlockingQueue (:actionqueue bot)]
    (while (nil? (:exit @conn))
      (let [action (.poll actionqueue 1 TimeUnit/SECONDS)]
        (when action
          (force action)))))
  (println "done - action handler")
  (println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))


(defn connect [server username]
  (let [username (or username (random-username))
        socket (Socket. (:name server) (:port server))
        in (DataInputStream. (.getInputStream socket))
        out (DataOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out})
        outqueue ^LinkedBlockingQueue (LinkedBlockingQueue.)
        actionqueue (LinkedBlockingQueue.)
        world (get-world server)
        bot (Bot. conn username outqueue actionqueue nil world (ref {})
                  (atom {}) (atom {}))]

    (println "connecting")

    ; We need to log in to find out our bot's entity ID, so we delay creation of the
    ; player until then.
    (let [player-id (:eid (login bot username))
          player (ref (Entity. player-id nil false 0.0))
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

      (println "starting action handler")
      (.start (Thread. #(action-handler bot)))

      (println "all systems go!")

      bot)))

(defn disconnect [bot]
  (dosync (alter (:connection bot) merge {:exit true})))


(def minecraft-local {:name "localhost" :port 25565})
