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
  0x06 :spawnposition
  0x07 :useentity
  0x08 :updatehealth
  0x09 :respawn
  0x0A :player
  0x0B :playerposition
  0x0C :playerlook
  0x0D :playerpositionlook
  0x0E :playerdigging
  0x0F :playerblockplacement
  0x10 :holdingchange
  0x11 :usebed
  0x12 :animation
  0x13 :entityaction
  0x14 :namedentityspawn
  0x15 :pickupspawn
  0x16 :collectitem
  0x17 :addobjectvehicle
  0x18 :mobspawn
  0x19 :entitypainting
  0x1B :stanceupdate
  0x1C :entityvelocity
  0x1D :entitydestroy
  0x1E :entity
  0x1F :entityrelativemove
  0x20 :entitylook
  0x21 :entitylookandrelativemove
  0x22 :entityteleport
  0x26 :entitystatus
  0x27 :attachentity
  0x28 :entitymetadata
  0x32 :prechunk
  0x33 :mapchunk
  0x34 :multiblockchange
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

(defn -write-bytearray [conn ba]
  ; TODO: Implement this.
  nil)

(defn -write-short [conn i]
  (println (str "-> SHORT: " i))
  (doto (:out @conn)
    (.writeShort (int i))))

(defn -write-shortarray [conn sa]
  ; TODO: Implement this.
  nil)

(defn -write-int [conn i]
  (println (str "-> INT: " i))
  (doto (:out @conn)
    (.writeInt (int i))))

(defn -write-long [conn i]
  (println (str "-> LONG: " i))
  (doto (:out @conn)
    (.writeLong (int i))))

(defn -write-double [conn i]
  (println (str "-> DOUBLE: " i))
  (doto (:out @conn)
    (.writeDouble (int i))))

(defn -write-float [conn i]
  (println (str "-> FLOAT: " i))
  (doto (:out @conn)
    (.writeFloat (int i))))

(defn -write-string16 [conn s]
  (-write-short conn (count s))
  (println (str "-> STRING: " s))
  (doto (:out @conn)
    (.writeChars s)))

(defn -write-bool [conn b]
  (println (str "-> BOOL: " b))
  (doto (:out @conn)
    (.writeBoolean b)))

(defn -write-metadata [conn m]
  ; TODO: Implement this.
  nil)


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

(defn write-packet-respawn [conn {world :world}]
  (-write-byte conn (:respawn packet-ids))

  (-write-bool conn world))

(defn write-packet-player [conn {onground :onground}]
  (-write-byte conn (:player packet-ids))

  (-write-bool conn onground))

(defn write-packet-playerposition [conn {x :x y :y stance :stance z :z onground :onground}]
  (-write-byte conn (:playerposition packet-ids))

  (-write-double conn x)
  (-write-double conn y)
  (-write-double conn stance)
  (-write-double conn z)
  (-write-bool conn onground))

(defn write-packet-playerlook [conn {yaw :yaw pitch :pitch onground :onground}]
  (-write-byte conn (:playerlook packet-ids))

  (-write-float conn yaw)
  (-write-float conn pitch)
  (-write-bool conn onground))

(defn write-packet-playerpositionlook [conn {x :x y :y stance :stance z :z yaw :yaw pitch :pitch onground :onground}]
  (-write-byte conn (:playerpositionlook packet-ids))

  (-write-double conn x)
  (-write-double conn y)
  (-write-double conn stance)
  (-write-double conn z)
  (-write-float conn yaw)
  (-write-float conn pitch)
  (-write-bool conn onground))

(defn write-packet-playerdigging [conn {status :status x :x y :y z :z face :face}]
  (-write-byte conn (:playerdigging packet-ids))

  (-write-byte conn status)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn face))

(defn write-packet-playerblockplacement [conn {x :x y :y z :z direction :direction id :id amount :amount damage :damage}]
  (-write-byte conn (:playerblockplacement packet-ids))

  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn direction)
  (-write-short conn id)
  (-write-byte conn amount)
  (-write-short conn damage))

(defn write-packet-holdingchange [conn {slot :slot}]
  (-write-byte conn (:holdingchange packet-ids))

  (-write-short conn slot))

(defn write-packet-usebed [conn {eid :eid inbed :inbed x :x y :y z :z}]
  (-write-byte conn (:usebed packet-ids))

  (-write-int conn eid)
  (-write-byte conn inbed)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z))

(defn write-packet-animation [conn {eid :eid animate :animate}]
  (-write-byte conn (:animation packet-ids))

  (-write-int conn eid)
  (-write-byte conn animate))

(defn write-packet-entityaction [conn {eid :eid action :action}]
  (-write-byte conn (:entityaction packet-ids))

  (-write-int conn eid)
  (-write-byte conn action))

(defn write-packet-pickupspawn [conn {eid :eid item :item count :count damagedata :damagedata x :x y :y z :z rotation :rotation pitch :pitch roll :roll}]
  (-write-byte conn (:pickupspawn packet-ids))

  (-write-int conn eid)
  (-write-short conn item)
  (-write-byte conn count)
  (-write-short conn damagedata)
  (-write-int conn x)
  (-write-int conn y)
  (-write-int conn x)
  (-write-byte conn rotation)
  (-write-byte int conn pitch)
  (-write-byte int conn roll))

(defn write-packet-entitypainting [conn {eid :eid title :title x :x y :y z :z direction :direction}]
  (-write-byte conn (:entitypainting packet-ids))

  (-write-int conn eid)
  (-write-string16 conn title)
  (-write-int conn x)
  (-write-int conn y)
  (-write-int conn x)
  (-write-int conn direction))

(defn write-packet-stanceupdate [conn {unknown1 :unknown1 unknown2 :unknown2 unknown3 :unknown3 unknown4 :unknown4 unknown5 :unknown5 unknown6 :unknown6 }]
  (-write-byte conn (:stanceupdate packet-ids))

  (-write-float conn unknown1)
  (-write-float conn unknown2)
  (-write-bool conn unknown3)
  (-write-bool conn unknown4)
  (-write-float conn unknown5)
  (-write-float conn unknown6))

(defn write-packet-entityvelocity [conn {eid :eid velocityx :velocityx velocityy :velocityy velocityz :velocityz}]
  (-write-byte conn (:entityvelocity packet-ids))

  (-write-int conn eid)
  (-write-short conn velocityx)
  (-write-short conn velocityy)
  (-write-short conn velocityz))

(defn write-packet-attachentity [conn {eid :eid vehicleid :vehicleid}]
  (-write-byte conn (:attachentity packet-ids))

  (-write-int conn eid)
  (-write-int conn vehicleid))

(defn write-packet-entitymetadata [conn {eid :eid metadata :metadata}]
  (-write-byte conn (:attachentity packet-ids))

  (-write-int conn eid)
  (-write-metadata conn metadata))

(defn write-packet-multiblockchange [conn {chunkx :chunkx chunkz :chunkz arraysize :arraysize coordinatearray :coordinatearray typearray :typearray metadataarray :metadataarray}]
  (-write-byte conn (:multiblockchange packet-ids))

  (-write-int conn chunkx)
  (-write-int conn chunkz)
  (-write-short conn arraysize)
  (-write-shortarray conn coordinatearray)
  (-write-bytearray conn typearray)
  (-write-bytearray conn metadataarray))


; Writing Wrappers -----------------------------------------------------------------
(defn flushc [conn]
  (doto (:out @conn) (.flush)))

(defn write-packet [conn packet-type payload]
  (cond
    (= packet-type :keepalive)            (write-packet-handshake conn payload)
    (= packet-type :handshake)            (write-packet-handshake conn payload)
    (= packet-type :login)                (write-packet-login conn payload)
    (= packet-type :chat)                 (write-packet-chat conn payload)
    (= packet-type :respawn)              (write-packet-respawn conn payload)
    (= packet-type :player)               (write-packet-player conn payload)
    (= packet-type :playerposition)       (write-packet-playerposition conn payload)
    (= packet-type :playerlook)           (write-packet-playerlook conn payload)
    (= packet-type :playerpositionlook)   (write-packet-playerpositionlook conn payload)
    (= packet-type :playerdigging)        (write-packet-playerdigging conn payload)
    (= packet-type :playerblockplacement) (write-packet-playerblockplacement conn payload)
    (= packet-type :holdingchange)        (write-packet-holdingchange conn payload)
    (= packet-type :usebed)               (write-packet-usebed conn payload)
    (= packet-type :animation)            (write-packet-animation conn payload)
    (= packet-type :entityaction)         (write-packet-entityaction conn payload)
    (= packet-type :pickupspawn)          (write-packet-pickupspawn conn payload)
    (= packet-type :entitypainting)       (write-packet-entitypainting conn payload)
    (= packet-type :stanceupdate)         (write-packet-stanceupdate conn payload)
    (= packet-type :entityvelocity)       (write-packet-entityvelocity conn payload)
    (= packet-type :attachentity)         (write-packet-attachentity conn payload)
    (= packet-type :entitymetadata)       (write-packet-entitymetadata conn payload)
    (= packet-type :multiblockchange)     (write-packet-multiblockchange conn payload)

    )
  (flushc conn))


; Reading Data ---------------------------------------------------------------------
(defn -read-byte [conn]
  (let [b (.readByte (:in @conn))]
    b))

(defn -read-bytearray [conn size]
  ; TODO: Implement this.
  nil)

(defn -read-int [conn]
  (let [i (.readInt (:in @conn))]
    i))

(defn -read-long [conn]
  (let [i (.readLong (:in @conn))]
    i))

(defn -read-short [conn]
  (let [i (.readShort (:in @conn))]
    i))

(defn -read-shortarray [conn size]
  ; TODO: Implement this.
  nil)

(defn -read-bool [conn]
  (let [b (.readBoolean (:in @conn))]
    b))

(defn -read-double [conn]
  (let [i (.readDouble (:in @conn))]
    i))

(defn -read-float [conn]
  (let [i (.readFloat (:in @conn))]
    i))

(defn -read-string16 [conn]
  (let [str-len (.readShort (:in @conn))
                s (apply str (repeatedly str-len #(.readChar (:in @conn))))]
    s))

(defn -read-metadata [conn]
  ; TODO: Implement this.
  nil)


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

(defn read-packet-spawnposition [conn]
  (-> {}
    (assoc :x (-read-int conn))
    (assoc :y (-read-int conn))
    (assoc :z (-read-int conn))))

(defn read-packet-useentity [conn]
  (-> {}
    (assoc :user (-read-int conn))
    (assoc :target (-read-int conn))
    (assoc :leftclick (-read-bool conn))))

(defn read-packet-updatehealth [conn]
  (-> {}
    (assoc :health (-read-short conn))))

(defn read-packet-respawn [conn]
  (-> {}
    (assoc :world (-read-byte conn))))

(defn read-packet-playerpositionlook [conn]
  (-> {}
    (assoc :x (-read-double conn))
    (assoc :stance (-read-double conn))
    (assoc :y (-read-double conn))
    (assoc :z (-read-double conn))
    (assoc :yaw (-read-float conn))
    (assoc :pitch (-read-float conn))
    (assoc :onground (-read-bool conn))))

(defn read-packet-playerdigging [conn]
  (-> {}
    (assoc :status (-read-byte conn))
    (assoc :x (-read-int conn))
    (assoc :y (-read-byte conn))
    (assoc :z (-read-int conn))
    (assoc :face (-read-byte conn))))

(defn read-packet-playerblockplacement [conn]
  (-> {}
    (assoc :x (-read-int conn))
    (assoc :y (-read-byte conn))
    (assoc :z (-read-int conn))
    (assoc :direction (-read-byte conn))
    (assoc :id (-read-short conn))
    (assoc :amount (-read-byte conn))
    (assoc :damage (-read-short conn))))

(defn read-packet-holdingchange [conn]
  (-> {}
    (assoc :slot (-read-short conn))))

(defn read-packet-usebed [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :inbed (-read-byte conn))
    (assoc :x (-read-int conn))
    (assoc :y (-read-byte conn))
    (assoc :z (-read-int conn))))

(defn read-packet-animate [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :animate (-read-byte conn))))

(defn read-packet-entityaction [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :action (-read-byte conn))))

(defn read-packet-namedentityspawn [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :playername (-read-string16 conn))
    (assoc :x (-read-int conn))
    (assoc :y (-read-int conn))
    (assoc :z (-read-int conn))
    (assoc :rotation (-read-byte conn))
    (assoc :pitch (-read-byte conn))
    (assoc :currentitem (-read-short conn))))

(defn read-packet-pickupspawn [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :item (-read-short conn))
    (assoc :count (-read-byte conn))
    (assoc :damagedata(-read-short conn))
    (assoc :x (-read-int conn))
    (assoc :y (-read-int conn))
    (assoc :z (-read-int conn))
    (assoc :rotation (-read-byte conn))
    (assoc :pitch (-read-byte conn))
    (assoc :roll (-read-byte conn))))

(defn read-packet-collectitem [conn]
  (-> {}
    (assoc :collectedeid (-read-int conn))
    (assoc :collectoreid (-read-int conn))))

(defn read-packet-addobjectvehicle [conn]
  (let [basepacket (-> {}
                     (assoc :eid (-read-int conn))
                     (assoc :type (-read-byte conn))
                     (assoc :x (-read-int conn))
                     (assoc :y (-read-int conn))
                     (assoc :z (-read-int conn))
                     (assoc :moar (-read-int conn)))]
    (if (< 0 (:moar basepacket))
      basepacket
      (-> basepacket
        (assoc :unknownx (-read-int conn))
        (assoc :unknowny (-read-int conn))
        (assoc :unknownz (-read-int conn))))))

(defn read-packet-mobspawn [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :type (-read-byte conn))
    (assoc :x (-read-int conn))
    (assoc :y (-read-int conn))
    (assoc :z (-read-int conn))
    (assoc :yaw (-read-byte conn))
    (assoc :pitch (-read-byte conn))
    (assoc :datastream (-read-metadata conn))))

(defn read-packet-entitypainting [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :type (-read-string16 conn))
    (assoc :x (-read-int conn))
    (assoc :y (-read-int conn))
    (assoc :z (-read-int conn))
    (assoc :direction (-read-int conn))))

(defn read-packet-stanceupdate [conn]
  (-> {}
    (assoc :unknown1 (-read-float conn))
    (assoc :unknown2 (-read-float conn))
    (assoc :unknown3 (-read-bool conn))
    (assoc :unknown4 (-read-bool conn))
    (assoc :unknown5 (-read-float conn))
    (assoc :unknown6 (-read-float conn))))

(defn read-packet-entityvelocity [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :velocityx (-read-short conn))
    (assoc :velocityy (-read-short conn))
    (assoc :velocityz (-read-short conn))))

(defn read-packet-entitydestroy [conn]
  (-> {}
    (assoc :eid (-read-int conn))))

(defn read-packet-entity [conn]
  (-> {}
    (assoc :eid (-read-int conn))))

(defn read-packet-entityrelativemove [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :dx (-read-byte conn))
    (assoc :dy (-read-byte conn))
    (assoc :dz (-read-byte conn))))

(defn read-packet-entitylook [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :yaw (-read-byte conn))
    (assoc :pitch (-read-byte conn))))

(defn read-packet-entitylookandrelativemove [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :dx (-read-byte conn))
    (assoc :dy (-read-byte conn))
    (assoc :dz (-read-byte conn))
    (assoc :yaw (-read-byte conn))
    (assoc :pitch (-read-byte conn))))

(defn read-packet-entityteleport [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :x (-read-int conn))
    (assoc :y (-read-int conn))
    (assoc :z (-read-int conn))
    (assoc :yaw (-read-byte conn))
    (assoc :pitch (-read-byte conn))))

(defn read-packet-entitystatus [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :entitystatus (-read-byte conn))))

(defn read-packet-attachentity [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :vehicleid (-read-int conn))))

(defn read-packet-entitymetadata [conn]
  (-> {}
    (assoc :eid (-read-int conn))
    (assoc :metadata (-read-metadata conn))))

(defn read-packet-prechunk [conn]
  (-> {}
    (assoc :x (-read-int conn))
    (assoc :z (-read-int conn))
    (assoc :mode (-read-bool conn))))

(defn read-packet-mapchunk [conn]
  (let [predata (-> {}
                  (assoc :x (-read-int conn))
                  (assoc :y (-read-short conn))
                  (assoc :z (-read-int conn))
                  (assoc :sizex (-read-byte conn))
                  (assoc :sizey (-read-byte conn))
                  (assoc :sizez (-read-byte conn))
                  (assoc :compressedsize (-read-int conn)))]
    (assoc predata
           :compresseddata
           (-read-bytearray conn
                            (:compressedsize predata)))))

(defn read-packet-multiblockchange [conn]
  (-> {}
    (assoc :chunkx (-read-int conn))
    (assoc :chunkz (-read-int conn))
    (assoc :arraysize (-read-short conn))
    (assoc :coordinatearray (-read-shortarray conn))
    (assoc :typearray (-read-bytearray conn))
    (assoc :metadataarray (-read-bytearray conn))))


; Reading Wrappers -----------------------------------------------------------------
(defn read-packet [conn packet-id]
  (let [packet-id (int packet-id)
                  packet-type (packet-types packet-id)]
    (println "\n----->")
    (println
      (cond
        (= packet-type :keepalive)                 (read-packet-keepalive conn)
        (= packet-type :handshake)                 (read-packet-handshake conn)
        (= packet-type :login)                     (read-packet-login conn)
        (= packet-type :chat)                      (read-packet-chat conn)
        (= packet-type :timeupdate)                (read-packet-timeupdate conn)
        (= packet-type :equipment)                 (read-packet-equipment conn)
        (= packet-type :spawnposition)             (read-packet-spawnposition conn)
        (= packet-type :useentity)                 (read-packet-useentity conn)
        (= packet-type :updatehealth)              (read-packet-updatehealth conn)
        (= packet-type :respawn)                   (read-packet-respawn conn)
        (= packet-type :playerpositionlook)        (read-packet-playerpositionlook conn)
        (= packet-type :playerdigging)             (read-packet-playerdigging conn)
        (= packet-type :playerblockplacement)      (read-packet-playerblockplacement conn)
        (= packet-type :holdingchange)             (read-packet-holdingchange conn)
        (= packet-type :usebed)                    (read-packet-usebed conn)
        (= packet-type :animate)                   (read-packet-animate conn)
        (= packet-type :entityaction)              (read-packet-entityaction conn)
        (= packet-type :namedentityspawn)          (read-packet-namedentityspawn conn)
        (= packet-type :pickupspawn)               (read-packet-pickupspawn conn)
        (= packet-type :collectitem)               (read-packet-collectitem conn)
        (= packet-type :addobjectvehicle)          (read-packet-addobjectvehicle conn)
        (= packet-type :mobspawn)                  (read-packet-mobspawn conn)
        (= packet-type :entitypainting)            (read-packet-entitypainting conn)
        (= packet-type :stanceupdate)              (read-packet-stanceupdate conn)
        (= packet-type :entityvelocity)            (read-packet-entityvelocity conn)
        (= packet-type :entitydestroy)             (read-packet-entitydestroy conn)
        (= packet-type :entity)                    (read-packet-entity conn)
        (= packet-type :entityrelativemove)        (read-packet-entityrelativemove conn)
        (= packet-type :entitylook)                (read-packet-entitylook conn)
        (= packet-type :entitylookandrelativemove) (read-packet-entitylookandrelativemove conn)
        (= packet-type :entityteleport)            (read-packet-entityteleport conn)
        (= packet-type :entitystatus)              (read-packet-entitystatus conn)
        (= packet-type :attachentity)              (read-packet-attachentity conn)
        (= packet-type :entitymetadata)            (read-packet-entitymetadata conn)
        (= packet-type :prechunk)                  (read-packet-prechunk conn)
        (= packet-type :mapchunk)                  (read-packet-mapchunk conn)
        (= packet-type :multiblockarray)           (read-packet-multiblockchange conn)

        :else (str "UNKNOWN PACKET TYPE: " packet-id)
        ))
    (println "\n\n\n")))


; Connection Handling --------------------------------------------------------------
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




; Scratch --------------------------------------------------------------------------
;(def server (connect minecraft-local))
;(disconnect server)

