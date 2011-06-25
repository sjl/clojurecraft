(ns clojurecraft.out
  (:use [clojurecraft.mappings]))

; Writing Data ---------------------------------------------------------------------
(defn- -write-byte [conn i]
  (println (str "-> PACKET ID: " (Integer/toHexString i)))
  (doto (:out @conn)
    (.writeByte (int i))))

(defn- -write-bytearray [conn ba]
  (println (str "-> BYTEARRAY: " ba))
  (doto (:out @conn)
    (.write ba 0 (count ba))))

(defn- -write-short [conn i]
  (println (str "-> SHORT: " i))
  (doto (:out @conn)
    (.writeShort (int i))))

(defn- -write-shortarray [conn sa]
  (map #(-write-short %) sa))

(defn- -write-int [conn i]
  (println (str "-> INT: " i))
  (doto (:out @conn)
    (.writeInt (int i))))

(defn- -write-long [conn i]
  (println (str "-> LONG: " i))
  (doto (:out @conn)
    (.writeLong (int i))))

(defn- -write-double [conn i]
  (println (str "-> DOUBLE: " i))
  (doto (:out @conn)
    (.writeDouble (int i))))

(defn- -write-float [conn i]
  (println (str "-> FLOAT: " i))
  (doto (:out @conn)
    (.writeFloat (int i))))

(defn- -write-string-utf8 [conn s]
  (println (str "-> STRING: " s))
  (doto (:out @conn)
    (.writeUTF s)))

(defn- -write-string-ucs2 [conn s]
  (-write-short conn (count s))
  (println (str "-> STRING: " s))
  (doto (:out @conn)
    (.writeChars s)))

(defn- -write-bool [conn b]
  (println (str "-> BOOL: " b))
  (doto (:out @conn)
    (.writeBoolean b)))

(defn- -write-metadata [conn m]
  ; TODO: Implement this.
  nil)


; Writing Packets ------------------------------------------------------------------
(defn- write-packet-keepalive [conn _]
  (-write-byte conn (:handshake packet-ids)))

(defn- write-packet-handshake [conn {username :username}]
  (-write-byte conn (:handshake packet-ids))

  (-write-string-ucs2 conn username))

(defn- write-packet-login [conn {version :version, username :username}]
  (-write-byte conn (:login packet-ids))

  (-write-int conn version)
  (-write-string-ucs2 conn username)
  (-write-long conn 0)
  (-write-byte conn 0))

(defn- write-packet-chat [conn {message :message}]
  (-write-byte conn (:chat packet-ids))

  (-write-string-ucs2 conn message))

(defn- write-packet-respawn [conn {world :world}]
  (-write-byte conn (:respawn packet-ids))

  (-write-bool conn world))

(defn- write-packet-player [conn {onground :onground}]
  (-write-byte conn (:player packet-ids))

  (-write-bool conn onground))

(defn- write-packet-playerposition [conn {x :x y :y stance :stance z :z onground :onground}]
  (-write-byte conn (:playerposition packet-ids))

  (-write-double conn x)
  (-write-double conn y)
  (-write-double conn stance)
  (-write-double conn z)
  (-write-bool conn onground))

(defn- write-packet-playerlook [conn {yaw :yaw pitch :pitch onground :onground}]
  (-write-byte conn (:playerlook packet-ids))

  (-write-float conn yaw)
  (-write-float conn pitch)
  (-write-bool conn onground))

(defn- write-packet-playerpositionlook [conn {x :x y :y stance :stance z :z yaw :yaw pitch :pitch onground :onground}]
  (-write-byte conn (:playerpositionlook packet-ids))

  (-write-double conn x)
  (-write-double conn y)
  (-write-double conn stance)
  (-write-double conn z)
  (-write-float conn yaw)
  (-write-float conn pitch)
  (-write-bool conn onground))

(defn- write-packet-playerdigging [conn {status :status x :x y :y z :z face :face}]
  (-write-byte conn (:playerdigging packet-ids))

  (-write-byte conn status)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn face))

(defn- write-packet-playerblockplacement [conn {x :x y :y z :z direction :direction id :id amount :amount damage :damage}]
  (-write-byte conn (:playerblockplacement packet-ids))

  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn direction)
  (-write-short conn id)
  (-write-byte conn amount)
  (-write-short conn damage))

(defn- write-packet-holdingchange [conn {slot :slot}]
  (-write-byte conn (:holdingchange packet-ids))

  (-write-short conn slot))

(defn- write-packet-usebed [conn {eid :eid inbed :inbed x :x y :y z :z}]
  (-write-byte conn (:usebed packet-ids))

  (-write-int conn eid)
  (-write-byte conn inbed)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z))

(defn- write-packet-animation [conn {eid :eid animate :animate}]
  (-write-byte conn (:animation packet-ids))

  (-write-int conn eid)
  (-write-byte conn animate))

(defn- write-packet-entityaction [conn {eid :eid action :action}]
  (-write-byte conn (:entityaction packet-ids))

  (-write-int conn eid)
  (-write-byte conn action))

(defn- write-packet-pickupspawn [conn {eid :eid item :item count :count damagedata :damagedata x :x y :y z :z rotation :rotation pitch :pitch roll :roll}]
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

(defn- write-packet-entitypainting [conn {eid :eid title :title x :x y :y z :z direction :direction}]
  (-write-byte conn (:entitypainting packet-ids))

  (-write-int conn eid)
  (-write-string-ucs2 conn title)
  (-write-int conn x)
  (-write-int conn y)
  (-write-int conn x)
  (-write-int conn direction))

(defn- write-packet-stanceupdate [conn {unknown1 :unknown1 unknown2 :unknown2 unknown3 :unknown3 unknown4 :unknown4 unknown5 :unknown5 unknown6 :unknown6 }]
  (-write-byte conn (:stanceupdate packet-ids))

  (-write-float conn unknown1)
  (-write-float conn unknown2)
  (-write-bool conn unknown3)
  (-write-bool conn unknown4)
  (-write-float conn unknown5)
  (-write-float conn unknown6))

(defn- write-packet-entityvelocity [conn {eid :eid velocityx :velocityx velocityy :velocityy velocityz :velocityz}]
  (-write-byte conn (:entityvelocity packet-ids))

  (-write-int conn eid)
  (-write-short conn velocityx)
  (-write-short conn velocityy)
  (-write-short conn velocityz))

(defn- write-packet-attachentity [conn {eid :eid vehicleid :vehicleid}]
  (-write-byte conn (:attachentity packet-ids))

  (-write-int conn eid)
  (-write-int conn vehicleid))

(defn- write-packet-entitymetadata [conn {eid :eid metadata :metadata}]
  (-write-byte conn (:attachentity packet-ids))

  (-write-int conn eid)
  (-write-metadata conn metadata))

(defn- write-packet-multiblockchange [conn {chunkx :chunkx chunkz :chunkz arraysize :arraysize coordinatearray :coordinatearray typearray :typearray metadataarray :metadataarray}]
  (-write-byte conn (:multiblockchange packet-ids))

  (-write-int conn chunkx)
  (-write-int conn chunkz)
  (-write-short conn arraysize)
  (-write-shortarray conn coordinatearray)
  (-write-bytearray conn typearray)
  (-write-bytearray conn metadataarray))

(defn- write-packet-blockchange [conn {x :x y :y z :z blocktype :blocktype blockmetadata :blockmetadata}]
  (-write-byte conn (:blockchange packet-ids))

  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn blocktype)
  (-write-byte conn blockmetadata))

(defn- write-packet-explosion [conn {x :x y :y z :z unknownradius :unknownradius recordcount :recordcount records :records}]
  ; TODO: Implement this.
  nil)

(defn- write-packet-soundeffect [conn {effectid :effectid x :x y :y z :z sounddata :sounddata}]
  (-write-byte conn (:soundeffect packet-ids))

  (-write-int conn effectid)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-int conn sounddata))

(defn- write-packet-newinvalidstate [conn {reason :reason}]
  (-write-byte conn (:newinvalidstate packet-ids))

  (-write-byte conn reason))

(defn- write-packet-openwindow [conn {windowid :windowid inventorytype :inventorytype windowtitle :windowtitle numberofslots :numberofslots}]
  (-write-byte conn (:openwindow packet-ids))

  (-write-byte conn windowid)
  (-write-byte conn inventorytype)
  (-write-string-utf8 conn windowtitle)
  (-write-byte conn numberofslots))

(defn- write-packet-closewindow [conn {windowid :windowid}]
  (-write-byte conn (:closewindow packet-ids))

  (-write-byte conn windowid))

(defn- write-packet-windowclick [conn {windowid :windowid slot :slot rightclick :rightclick actionnumber :actionnumber shift :shift itemid :itemid itemcount :itemcount itemuses :itemuses}]
  (-write-byte conn (:windowclick packet-ids))

  (-write-byte conn windowid)
  (-write-short conn slot)
  (-write-byte conn rightclick)
  (-write-short conn actionnumber)
  (-write-bool conn shift)
  (-write-short conn itemid)
  (-write-byte conn itemcount)
  (-write-short conn itemuses))

(defn- write-packet-transaction [conn {windowid :windowid actionnumber :actionnumber accepted :accepted}]
  (-write-byte conn (:transaction packet-ids))

  (-write-byte conn windowid)
  (-write-short conn actionnumber)
  (-write-bool conn accepted))

(defn- write-packet-updatesign [conn {x :x y :y z :z text1 :text1 text2 :text2 text3 :text3 text4 :text4}]
  (-write-byte conn (:transaction packet-ids))

  (-write-int conn x)
  (-write-short conn y)
  (-write-int conn z)
  (-write-string-ucs2 conn text1)
  (-write-string-ucs2 conn text2)
  (-write-string-ucs2 conn text3)
  (-write-string-ucs2 conn text4))

(defn- write-packet-incrementstatistic [conn {statisticid :statisticid amount :amount}]
  (-write-byte conn (:incrementstatistic packet-ids))

  (-write-int conn statisticid)
  (-write-byte conn amount))

(defn- write-packet-disconnectkick [conn {reason :reason}]
  (-write-byte conn (:disconnectkick packet-ids))

  (-write-string-ucs2 conn reason))


; Writing Wrappers -----------------------------------------------------------------
(defn- flushc [conn]
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
    :incrementstatistic   (write-packet-incrementstatistic conn payload)
    :disconnectkick       (write-packet-disconnectkick conn payload)

    )
  (flushc conn))

