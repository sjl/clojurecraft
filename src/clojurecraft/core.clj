(ns clojurecraft.core
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.in])
  (:use [clojurecraft.out])
  (:import (java.net Socket)
           (java.io DataOutputStream DataInputStream)))

(def minecraft-local {:name "localhost" :port 25565})

(declare conn-handler)

; Connection Wrappers --------------------------------------------------------------
(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (DataInputStream. (.getInputStream socket))
        out (DataOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn disconnect [conn]
  (dosync (alter conn merge {:exit true})))


; Connection Handling --------------------------------------------------------------
(defn login [conn]
  ; Send handshake
  (write-packet conn :handshake {:username "timmy"})

  ; Get handshake
  (read-packet conn)

  ; Send login
  (write-packet conn :login {:version 13 :username "timmy"})

  ; Get login
  (read-packet conn))

(defn conn-handler [conn]
  (println "connecting")
  (login conn)
  (while (nil? (:exit @conn))
    (read-packet conn))
  (println "done"))



; Scratch --------------------------------------------------------------------------
(def server (connect minecraft-local))
;(disconnect server)

