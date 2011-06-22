(ns clojurecraft.core
    (:import (java.net Socket)
             (java.io DataOutputStream DataInputStream)))

(def minecraft-local {:name "localhost" :port 25565})

; Packet Type Maps -----------------------------------------------------------------
(def packet-types {
     0x00 :keepalive 
     0x01 :login 
     0x02 :handshake 
     0x03 :chat 
     0x04 :timeupdate 
     0x05 :equipment 
})
(def packet-ids (apply assoc {} (mapcat reverse packet-types)))

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
(defn -write-byte [conn i]
      (println (str "-> PACKET ID: " (Integer/toHexString i)))
      (doto (:out @conn)
         (.writeByte (int i))))

(defn -write-short [conn i]
      (println (str "-> SHORT: " i))
      (doto (:out @conn)
         (.writeShort (int i))))

(defn -write-int [conn i]
      (println (str "-> INT: " i))
      (doto (:out @conn)
         (.writeInt (int i))))

(defn -write-long [conn i]
      (println (str "-> LONG: " i))
      (doto (:out @conn)
         (.writeLong (int i))))

(defn -write-string16 [conn s]
      (-write-short conn (count s))
      (println (str "-> STRING: " s))
      (doto (:out @conn)
         (.writeChars s)))

(defn -write-bool [conn b]
      (println (str "-> BOOL: " b))
      (doto (:out @conn)
         (.writeBoolean b)))


; Writing Packets ------------------------------------------------------------------
(defn write-packet-keepalive [conn _]
      (-write-byte conn (:handshake packet-ids)))

(defn write-packet-handshake [conn {username :username}]
      (-write-byte conn (:handshake packet-ids))

      (-write-string16 conn username))

(defn write-packet-login [conn {version :version, username :username}]
      (-write-byte conn (:login packet-ids))

      (-write-int conn version)
      (-write-string16 conn username)
      (-write-long conn 0)
      (-write-byte conn 0))

(defn write-packet-chat [conn {message :message}]
      (-write-byte conn (:chat packet-ids))

      (-write-string16 conn message))


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
(defn -read-byte [conn]
   (let [b (.readByte (:in @conn))]
     b))

(defn -read-int [conn]
   (let [i (.readInt (:in @conn))]
     i))

(defn -read-long [conn]
   (let [i (.readLong (:in @conn))]
     i))

(defn -read-short [conn]
   (let [i (.readShort (:in @conn))]
     i))

(defn -read-string16 [conn]
  (let [str-len (.readShort (:in @conn))
        s (apply str (repeatedly str-len #(.readChar (:in @conn))))]
    s))


; Reading Packets ------------------------------------------------------------------
(defn read-packet-keepalive [conn]
      nil)

(defn read-packet-handshake [conn]
      (-> {}
          (assoc :hash (-read-string16 conn))))

(defn read-packet-login [conn]
      (-> {}
          (assoc :eid (-read-int conn))
          (assoc :unknown (-read-string16 conn))
          (assoc :seed (-read-long conn))
          (assoc :dimension (-read-byte conn))))

(defn read-packet-chat [conn]
      (-> {}
          (assoc :message (-read-string16 conn))))

(defn read-packet-timeupdate [conn]
      (-> {}
          (assoc :time (-read-long conn))))

(defn read-packet-equipment [conn]
      (-> {}
          (assoc :eid (-read-int conn))
          (assoc :slot (-read-short conn))
          (assoc :itemid (-read-short conn))
          (assoc :unknown (-read-short conn))))


(defn read-packet [conn packet-id]
      (let [packet-id (int packet-id)
            packet-type (packet-types packet-id)]
        (println "\n----->")
        (println
          (cond
            (= packet-type :keepalive) (read-packet-keepalive conn)
            (= packet-type :handshake) (read-packet-handshake conn)
            (= packet-type :login) (read-packet-login conn)
            (= packet-type :chat) (read-packet-chat conn)
            (= packet-type :timeupdate) (read-packet-timeupdate conn)
            :else (str "UNKNOWN PACKET TYPE: " packet-id)
            ))
        (println "\n\n\n")))


; Reading Wrappers -----------------------------------------------------------------
(defn login [conn]
    ; Send handshake
    (write-packet conn :handshake {:username "timmy"})

    ; Get handshake
    (let [packet-id (-read-byte conn)]
      (read-packet conn packet-id))

    ; Send login
    (write-packet conn :login {:version 13 :username "timmy"})

    ; Get login
    (let [packet-id (-read-byte conn)]
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
;(def server (connect minecraft-local))
;(disconnect server)

