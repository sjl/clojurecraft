(ns clojurecraft.core
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.in])
  (:use [clojurecraft.out])
  (:import (java.net Socket)
           (java.io DataOutputStream DataInputStream)))

(def minecraft-local {:name "localhost" :port 25565})

(declare conn-handler)
(declare login)

(defn login [conn]
  ; Send handshake
  (write-packet conn :handshake {:username "timmy"})

  ; Get handshake
  (read-packet conn)

  ; Send login
  (write-packet conn :login {:version 13 :username "timmy"})

  ; Get login
  (read-packet conn))


(defn input-handler [bot]
  (let [conn (:connection bot)]
    (while (nil? (:exit @conn))
      (read-packet conn)))
  (println "done"))


(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (DataInputStream. (.getInputStream socket))
        out (DataOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out})
        bot {:connection conn}]

    (println "connecting")
    (login conn)
    (println "connected and logged in")

    (println "starting read handler")
    (doto (Thread. #(input-handler bot)) (.start))

    (println "all systems go, returning bot")
    bot))

(defn disconnect [bot]
  (dosync (alter (:connection bot) merge {:exit true})))



; Scratch --------------------------------------------------------------------------
(def bot (connect minecraft-local))
;(disconnect bot)

