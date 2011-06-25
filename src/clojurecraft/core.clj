(ns clojurecraft.core
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.in])
  (:use [clojurecraft.out])
  (:import (java.net Socket)
           (java.io DataOutputStream DataInputStream)))

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


(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (DataInputStream. (.getInputStream socket))
        out (DataOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out})
        bot {:connection conn}]

    (println "connecting")
    (login bot)
    (println "connected and logged in")

    (println "starting read handler")
    (doto (Thread. #(input-handler bot)) (.start))

    (println "all systems go, returning bot")
    bot))

(defn disconnect [bot]
  (dosync (alter (:connection bot) merge {:exit true})))



; Scratch --------------------------------------------------------------------------
;(def bot (connect minecraft-local))
;(disconnect bot)

