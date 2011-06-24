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
  0x35 :blockchange
  0x36 :playnoteblock
  0x3C :explosion
  0x3D :soundeffect
  0x46 :newinvalidstate
  0x47 :thunderbolt
  0x64 :openwindow
  0x65 :closewindow
  0x66 :windowclick
  0x67 :setslot
  0x68 :windowitems
  0x69 :updateprogressbar
  0x6A :transaction
  0x82 :updatesign
  0x83 :mapdata
  0xC8 :incrementstatistic
  0xFF :disconnectkick
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

(defn -write-string8 [conn s]
  ; TODO: Implement this.
  nil)

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

(defn write-packet-blockchange [conn {x :x y :y z :z blocktype :blocktype blockmetadata :blockmetadata}]
  (-write-byte conn (:blockchange packet-ids))

  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn blocktype)
  (-write-byte conn blockmetadata))

(defn write-packet-explosion [conn {x :x y :y z :z unknownradius :unknownradius recordcount :recordcount records :records}]
  ; TODO: Implement this.
  nil)

(defn write-packet-soundeffect [conn {effectid :effectid x :x y :y z :z sounddata :sounddata}]
  (-write-byte conn (:soundeffect packet-ids))

  (-write-int conn effectid)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-int conn sounddata))

(defn write-packet-newinvalidstate [conn {reason :reason}]
  (-write-byte conn (:newinvalidstate packet-ids))

  (-write-byte conn reason))

(defn write-packet-openwindow [conn {windowid :windowid inventorytype :inventorytype windowtitle :windowtitle numberofslots :numberofslots}]
  (-write-byte conn (:openwindow packet-ids))

  (-write-byte conn windowid)
  (-write-byte conn inventorytype)
  (-write-string8 conn windowtitle)
  (-write-byte conn numberofslots))

(defn write-packet-closewindow [conn {windowid :windowid}]
  (-write-byte conn (:closewindow packet-ids))

  (-write-byte conn windowid))

(defn write-packet-windowclick [conn {windowid :windowid slot :slot rightclick :rightclick actionnumber :actionnumber shift :shift itemid :itemid itemcount :itemcount itemuses :itemuses}]
  (-write-byte conn (:windowclick packet-ids))

  (-write-byte conn windowid)
  (-write-short conn slot)
  (-write-byte conn rightclick)
  (-write-short conn actionnumber)
  (-write-bool conn shift)
  (-write-short conn itemid)
  (-write-byte conn itemcount)
  (-write-short conn itemuses))

(defn write-packet-transaction [conn {windowid :windowid actionnumber :actionnumber accepted :accepted}]
  (-write-byte conn (:transaction packet-ids))

  (-write-byte conn windowid)
  (-write-short conn actionnumber)
  (-write-bool conn accepted))

(defn write-packet-updatesign [conn {x :x y :y z :z text1 :text1 text2 :text2 text3 :text3 text4 :text4}]
  (-write-byte conn (:transaction packet-ids))

  (-write-int conn x)
  (-write-short conn y)
  (-write-int conn z)
  (-write-string16 conn text1)
  (-write-string16 conn text2)
  (-write-string16 conn text3)
  (-write-string16 conn text4))


; Writing Wrappers -----------------------------------------------------------------
(defn flushc [conn]
  (doto (:out @conn) (.flush)))

(defn write-packet [conn packet-type payload]
  (case packet-type
    :keepalive            (write-packet-handshake conn payload)
    :handshake            (write-packet-handshake conn payload)
    :login                (write-packet-login conn payload)
    :chat                 (write-packet-chat conn payload)
    :respawn              (write-packet-respawn conn payload)
    :player               (write-packet-player conn payload)
    :playerposition       (write-packet-playerposition conn payload)
    :playerlook           (write-packet-playerlook conn payload)
    :playerpositionlook   (write-packet-playerpositionlook conn payload)
    :playerdigging        (write-packet-playerdigging conn payload)
    :playerblockplacement (write-packet-playerblockplacement conn payload)
    :holdingchange        (write-packet-holdingchange conn payload)
    :usebed               (write-packet-usebed conn payload)
    :animation            (write-packet-animation conn payload)
    :entityaction         (write-packet-entityaction conn payload)
    :pickupspawn          (write-packet-pickupspawn conn payload)
    :entitypainting       (write-packet-entitypainting conn payload)
    :stanceupdate         (write-packet-stanceupdate conn payload)
    :entityvelocity       (write-packet-entityvelocity conn payload)
    :attachentity         (write-packet-attachentity conn payload)
    :entitymetadata       (write-packet-entitymetadata conn payload)
    :multiblockchange     (write-packet-multiblockchange conn payload)
    :blockchange          (write-packet-blockchange conn payload)
    :explosion            (write-packet-explosion conn payload)
    :soundeffect          (write-packet-soundeffect conn payload)
    :newinvalidstate      (write-packet-newinvalidstate conn payload)
    :openwindow           (write-packet-openwindow conn payload)
    :closewindow          (write-packet-closewindow conn payload)
    :windowclick          (write-packet-windowclick conn payload)
    :transaction          (write-packet-transaction conn payload)
    :updatesign           (write-packet-updatesign conn payload)

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

(defn -read-string8 [conn]
  ; TODO: Implement this.
  nil)

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
  (assoc {}
    :hash (-read-string16 conn)))

(defn read-packet-login [conn]
  (assoc {}
    :eid (-read-int conn)
    :unknown (-read-string16 conn)
    :seed (-read-long conn)
    :dimension (-read-byte conn)))

(defn read-packet-chat [conn]
  (assoc {}
    :message (-read-string16 conn)))

(defn read-packet-timeupdate [conn]
  (assoc {}
    :time (-read-long conn)))

(defn read-packet-equipment [conn]
  (assoc {}
    :eid (-read-int conn)
    :slot (-read-short conn)
    :itemid (-read-short conn)
    :unknown (-read-short conn)))

(defn read-packet-spawnposition [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)))

(defn read-packet-useentity [conn]
  (assoc {}
    :user (-read-int conn)
    :target (-read-int conn)
    :leftclick (-read-bool conn)))

(defn read-packet-updatehealth [conn]
  (assoc {}
    :health (-read-short conn)))

(defn read-packet-respawn [conn]
  (assoc {}
    :world (-read-byte conn)))

(defn read-packet-playerpositionlook [conn]
  (assoc {}
    :x (-read-double conn)
    :stance (-read-double conn)
    :y (-read-double conn)
    :z (-read-double conn)
    :yaw (-read-float conn)
    :pitch (-read-float conn)
    :onground (-read-bool conn)))

(defn read-packet-playerdigging [conn]
  (assoc {}
    :status (-read-byte conn)
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :face (-read-byte conn)))

(defn read-packet-playerblockplacement [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :direction (-read-byte conn)
    :id (-read-short conn)
    :amount (-read-byte conn)
    :damage (-read-short conn)))

(defn read-packet-holdingchange [conn]
  (assoc {}
    :slot (-read-short conn)))

(defn read-packet-usebed [conn]
  (assoc {}
    :eid (-read-int conn)
    :inbed (-read-byte conn)
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)))

(defn read-packet-animate [conn]
  (assoc {}
    :eid (-read-int conn)
    :animate (-read-byte conn)))

(defn read-packet-entityaction [conn]
  (assoc {}
    :eid (-read-int conn)
    :action (-read-byte conn)))

(defn read-packet-namedentityspawn [conn]
  (assoc {}
    :eid (-read-int conn)
    :playername (-read-string16 conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :rotation (-read-byte conn)
    :pitch (-read-byte conn)
    :currentitem (-read-short conn)))

(defn read-packet-pickupspawn [conn]
  (assoc {}
    :eid (-read-int conn)
    :item (-read-short conn)
    :count (-read-byte conn)
    :damagedata (-read-short conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :rotation (-read-byte conn)
    :pitch (-read-byte conn)
    :roll (-read-byte conn)))

(defn read-packet-collectitem [conn]
  (assoc {}
    :collectedeid (-read-int conn)
    :collectoreid (-read-int conn)))

(defn read-packet-addobjectvehicle [conn]
  (let [basepacket (assoc {}
                     :eid (-read-int conn)
                     :type (-read-byte conn)
                     :x (-read-int conn)
                     :y (-read-int conn)
                     :z (-read-int conn)
                     :moar (-read-int conn))]
    (if (< 0 (:moar basepacket))
      basepacket
      (assoc basepacket
        :unknownx (-read-int conn)
        :unknowny (-read-int conn)
        :unknownz (-read-int conn)))))

(defn read-packet-mobspawn [conn]
  (assoc {}
    :eid (-read-int conn)
    :type (-read-byte conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)
    :datastream (-read-metadata conn)))

(defn read-packet-entitypainting [conn]
  (assoc {}
    :eid (-read-int conn)
    :type (-read-string16 conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :direction (-read-int conn)))

(defn read-packet-stanceupdate [conn]
  (assoc {}
    :unknown1 (-read-float conn)
    :unknown2 (-read-float conn)
    :unknown3 (-read-bool conn)
    :unknown4 (-read-bool conn)
    :unknown5 (-read-float conn)
    :unknown6 (-read-float conn)))

(defn read-packet-entityvelocity [conn]
  (assoc {}
    :eid (-read-int conn)
    :velocityx (-read-short conn)
    :velocityy (-read-short conn)
    :velocityz (-read-short conn)))

(defn read-packet-entitydestroy [conn]
  (assoc {}
    :eid (-read-int conn)))

(defn read-packet-entity [conn]
  (assoc {}
    :eid (-read-int conn)))

(defn read-packet-entityrelativemove [conn]
  (assoc {}
    :eid (-read-int conn)
    :dx (-read-byte conn)
    :dy (-read-byte conn)
    :dz (-read-byte conn)))

(defn read-packet-entitylook [conn]
  (assoc {}
    :eid (-read-int conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)))

(defn read-packet-entitylookandrelativemove [conn]
  (assoc {}
    :eid (-read-int conn)
    :dx (-read-byte conn)
    :dy (-read-byte conn)
    :dz (-read-byte conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)))

(defn read-packet-entityteleport [conn]
  (assoc {}
    :eid (-read-int conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)))

(defn read-packet-entitystatus [conn]
  (assoc {}
    :eid (-read-int conn)
    :entitystatus (-read-byte conn)))

(defn read-packet-attachentity [conn]
  (assoc {}
    :eid (-read-int conn)
    :vehicleid (-read-int conn)))

(defn read-packet-entitymetadata [conn]
  (assoc {}
    :eid (-read-int conn)
    :metadata (-read-metadata conn)))

(defn read-packet-prechunk [conn]
  (assoc {}
    :x (-read-int conn)
    :z (-read-int conn)
    :mode (-read-bool conn)))

(defn read-packet-mapchunk [conn]
  (let [predata (assoc {}
                  :x (-read-int conn)
                  :y (-read-short conn)
                  :z (-read-int conn)
                  :sizex (-read-byte conn)
                  :sizey (-read-byte conn)
                  :sizez (-read-byte conn)
                  :compressedsize (-read-int conn))]
    (assoc predata
           :compresseddata
           (-read-bytearray conn
                            (:compressedsize predata)))))

(defn read-packet-multiblockchange [conn]
  (assoc {}
    :chunkx (-read-int conn)
    :chunkz (-read-int conn)
    :arraysize (-read-short conn)
    :coordinatearray (-read-shortarray conn)
    :typearray (-read-bytearray conn)
    :metadataarray (-read-bytearray conn)))

(defn read-packet-blockchange [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :blocktype (-read-byte conn)
    :blockmetadata (-read-byte conn)))

(defn read-packet-playnoteblock [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-short conn)
    :z (-read-int conn)
    :instrumenttype (-read-byte conn)
    :pitch (-read-byte conn)))

(defn read-packet-explosion [conn]
  (let [prerecords (assoc {}
                          :x (-read-int conn)
                          :y (-read-short conn)
                          :z (-read-int conn)
                          :unknownradius (-read-byte conn)
                          :recordcount (-read-byte conn))]
    (assoc prerecords
           :records (-read-bytearray conn
                                     (* 3 (:recordcount prerecords))))))

(defn read-packet-soundeffect [conn]
  (assoc {}
    :effectid (-read-int conn)
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :sounddata (-read-int conn)))

(defn read-packet-newinvalidstate [conn]
  (assoc {}
    :reason (-read-byte conn)))

(defn read-packet-thunderbolt [conn]
  (assoc {}
    :eid (-read-int conn)
    :unknown (-read-bool conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)))

(defn read-packet-openwindow [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :inventorytype (-read-byte conn)
    :windowtitle (-read-string8 conn)
    :numberofslots (-read-byte conn)))

(defn read-packet-closewindow [conn]
  (assoc {}
    :windowid (-read-byte conn)))

(defn read-packet-setslot [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :slot (-read-short conn)
    :itemid (-read-short conn)
    :itemcount (-read-byte conn)
    :itemuses (-read-short conn)))

(defn read-packet-windowitems [conn]
  (letfn [(-read-payload-item []
             (let [payload (assoc :itemid (-read-short conn))]
               (if (= (:itemid payload) -1)
                 payload
                 (assoc payload
                        :count (-read-byte conn)
                        :uses (-read-short conn)))))]
    (let [prepayload (assoc {}
                            :windowid (-read-byte conn)
                            :count (-read-short conn))
          payload (repeatedly (:count prepayload) -read-payload-item)]
      (assoc prepayload :payload payload))))

(defn read-packet-updateprogressbar [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :progressbar (-read-short conn)
    :value (-read-short conn)))

(defn read-packet-transaction [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :actionnumber (-read-short conn)
    :accepted (-read-short conn)))

(defn read-packet-updatesign [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-short conn)
    :z (-read-int conn)
    :text1 (-read-string16 conn)
    :text2 (-read-string16 conn)
    :text3 (-read-string16 conn)
    :text4 (-read-string16 conn)))


; Reading Wrappers -----------------------------------------------------------------
(defn read-packet [conn packet-id]
  (let [packet-id (int packet-id)
                  packet-type (packet-types packet-id)]
    (println "\n----->")
    (println
      (case packet-type
        :keepalive                 (read-packet-keepalive conn)
        :handshake                 (read-packet-handshake conn)
        :login                     (read-packet-login conn)
        :chat                      (read-packet-chat conn)
        :timeupdate                (read-packet-timeupdate conn)
        :equipment                 (read-packet-equipment conn)
        :spawnposition             (read-packet-spawnposition conn)
        :useentity                 (read-packet-useentity conn)
        :updatehealth              (read-packet-updatehealth conn)
        :respawn                   (read-packet-respawn conn)
        :playerpositionlook        (read-packet-playerpositionlook conn)
        :playerdigging             (read-packet-playerdigging conn)
        :playerblockplacement      (read-packet-playerblockplacement conn)
        :holdingchange             (read-packet-holdingchange conn)
        :usebed                    (read-packet-usebed conn)
        :animate                   (read-packet-animate conn)
        :entityaction              (read-packet-entityaction conn)
        :namedentityspawn          (read-packet-namedentityspawn conn)
        :pickupspawn               (read-packet-pickupspawn conn)
        :collectitem               (read-packet-collectitem conn)
        :addobjectvehicle          (read-packet-addobjectvehicle conn)
        :mobspawn                  (read-packet-mobspawn conn)
        :entitypainting            (read-packet-entitypainting conn)
        :stanceupdate              (read-packet-stanceupdate conn)
        :entityvelocity            (read-packet-entityvelocity conn)
        :entitydestroy             (read-packet-entitydestroy conn)
        :entity                    (read-packet-entity conn)
        :entityrelativemove        (read-packet-entityrelativemove conn)
        :entitylook                (read-packet-entitylook conn)
        :entitylookandrelativemove (read-packet-entitylookandrelativemove conn)
        :entityteleport            (read-packet-entityteleport conn)
        :entitystatus              (read-packet-entitystatus conn)
        :attachentity              (read-packet-attachentity conn)
        :entitymetadata            (read-packet-entitymetadata conn)
        :prechunk                  (read-packet-prechunk conn)
        :mapchunk                  (read-packet-mapchunk conn)
        :multiblockchange          (read-packet-multiblockchange conn)
        :blockchange               (read-packet-blockchange conn)
        :playnoteblock             (read-packet-playnoteblock conn)
        :explosion                 (read-packet-explosion conn)
        :soundeffect               (read-packet-soundeffect conn)
        :newinvalidstate           (read-packet-newinvalidstate conn)
        :thunderbolt               (read-packet-thunderbolt conn)
        :openwindow                (read-packet-openwindow conn)
        :closewindow               (read-packet-closewindow conn)
        :setslot                   (read-packet-setslot conn)
        :windowitems               (read-packet-windowitems conn)
        :updateprogressbar         (read-packet-updateprogressbar conn)
        :transaction               (read-packet-transaction conn)
        :updatesign                (read-packet-updatesign conn)

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

