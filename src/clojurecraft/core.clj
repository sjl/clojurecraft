(ns clojurecraft.core
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.in])
  (:use [clojurecraft.out])
  (:import (java.net Socket)
           (java.io DataOutputStream DataInputStream)
           (java.util.concurrent LinkedBlockingQueue)))

(def minecraft-local {:name "localhost" :port 25565})

(declare conn-handler)
(declare login)

(defn login [bot]
  ; Send handshake
  (write-packet bot :handshake {:username "timmy"})

  ; Get handshake
  (read-packet bot)

  ; Send login
  (write-packet bot :login {:version 13 :username "timmy"})

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
      (let [player (:player bot)
            location (:location @player)]
        (when (not (nil? location))
          (.put outqueue [:playerpositionlook location]))
        (Thread/sleep 50)))))

(defn output-handler [bot]
  (let [conn (:connection bot)
        outqueue (:outqueue bot)]
    (while (nil? (:exit @conn))
      (let [[packet-type, payload] (.take outqueue)]
        (write-packet bot packet-type payload)))))


(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (DataInputStream. (.getInputStream socket))
        out (DataOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out})
        outqueue (LinkedBlockingQueue.)
        player (ref {:location nil})
        world (ref {})
        bot {:connection conn, :outqueue outqueue, :player player, :world world}]

    (println "connecting")
    (login bot)
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



; Scratch --------------------------------------------------------------------------
(def bot (connect minecraft-local))
(disconnect bot)

