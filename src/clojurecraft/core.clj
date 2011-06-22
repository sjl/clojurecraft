(ns clojurecraft.core
    (:import (java.net Socket)
             (java.io DataOutputStream DataInputStream)))

(def minecraft-local {:name "localhost" :port 25565})

; Packet Type Maps -----------------------------------------------------------------
(def packet-ids {
     :keepalive 0x00
     :login 0x01
     :handshake 0x02
     :chat 0x03
})
(def packet-types (apply assoc {} (mapcat reverse packet-ids)))

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


; Writing Data ---------------------------------------------------------------------
(defn writ-byte [conn i]
      (println (str "-> PACKET ID: " (Integer/toHexString i)))
      (doto (:out @conn)
         (.writeByte (int i))))

(defn writ-short [conn i]
      (println (str "-> SHORT: " i))
      (doto (:out @conn)
         (.writeShort (int i))))

(defn writ-int [conn i]
      (println (str "-> INT: " i))
      (doto (:out @conn)
         (.writeInt (int i))))

(defn writ-long [conn i]
      (println (str "-> LONG: " i))
      (doto (:out @conn)
         (.writeLong (int i))))

(defn writ-string16 [conn s]
      (writ-short conn (count s))
      (println (str "-> STRING: " s))
      (doto (:out @conn)
         (.writeChars s)))


; Writing Packets ------------------------------------------------------------------
(defn write-packet-keepalive [conn _]
      (writ-byte conn (:handshake packet-ids)))

(defn write-packet-handshake [conn {username :username}]
      (writ-byte conn (:handshake packet-ids))

      (writ-string16 conn username))

(defn write-packet-login [conn {version :version, username :username}]
      (writ-byte conn (:login packet-ids))

      (writ-int conn version)
      (writ-string16 conn username)
      (writ-long conn 0)
      (writ-byte conn 0))


; Writing Wrappers -----------------------------------------------------------------
(defn flushc [conn]
      (doto (:out @conn) (.flush)))

(defn write-packet [conn packet-type payload]
      (cond
        (= packet-type :keepalive) (write-packet-handshake conn payload)
        (= packet-type :handshake) (write-packet-handshake conn payload)
        (= packet-type :login) (write-packet-login conn payload)
        )
      (flushc conn))


; Reading Data ---------------------------------------------------------------------
(defn red-byte [conn]
   (let [b (.readByte (:in @conn))]
     b))

(defn red-int [conn]
   (let [i (.readInt (:in @conn))]
     i))

(defn red-long [conn]
   (let [i (.readLong (:in @conn))]
     i))

(defn red-string16 [conn]
  (let [str-len (.readShort (:in @conn))
        s (apply str (repeatedly str-len #(.readChar (:in @conn))))]
    s))


; Handling Packets -----------------------------------------------------------------
(defn read-packet-keepalive [conn]
      (println "OMG got a keepalive")
      nil)

(defn read-packet-handshake [conn]
      (println "OMG got a handshake")
      (-> {}
          (assoc :hash (red-string16 conn))))

(defn read-packet-login [conn]
      (println "OMG got a login")
      (-> {}
          (assoc :eid (red-int conn))
          (assoc :unknown (red-string16 conn))
          (assoc :seed (red-long conn))
          (assoc :dimension (red-byte conn))))

(defn read-packet [conn packet-id]
      (let [packet-id (int packet-id)
            packet-type (packet-types packet-id)]
        (println "\n----->")
        (println
          (cond
            (= packet-type :keepalive) (read-packet-keepalive conn)
            (= packet-type :handshake) (read-packet-handshake conn)
            (= packet-type :login) (read-packet-login conn)
            :else (str "UNKNOWN PACKET TYPE: " packet-id)
            ))
        (println "\n\n\n")))


; Handling Wrappers ----------------------------------------------------------------
(defn login [conn]
    ; Send handshake
    (write-packet conn :handshake {:username "timmy"})

    ; Get handshake
    (let [packet-id (red-byte conn)]
      (read-packet conn packet-id))

    ; Send login
    (write-packet conn :login {:version 13 :username "timmy"})

    ; Get login
    (let [packet-id (red-byte conn)]
      (read-packet conn packet-id)))

(defn conn-handler [conn]
      (println "connecting")
      (login conn)
      (while (nil? (:exit @conn))
        (let [packet-id (.readByte (:in @conn))]
          (read-packet conn packet-id)
          ))
      (println "done"))




; REPL -----------------------------------------------------------------------------
(def server (connect minecraft-local))
;(disconnect server)

