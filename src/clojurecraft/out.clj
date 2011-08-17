(ns clojurecraft.out
  (:use [clojurecraft.util])
  (:use [clojurecraft.mappings])
  (:import (java.io DataOutputStream)))

; Writing Data ---------------------------------------------------------------------
(defn- -write-byte [conn i]
  (io! (.writeByte ^DataOutputStream (:out @conn) (int i))))

(defn- -write-bytearray [conn ba]
  (io! (.write ^DataOutputStream (:out @conn) (byte-array (map byte ba)) 0 (count ba))))

(defn- -write-short [conn i]
  (io! (.writeShort ^DataOutputStream (:out @conn) (short i))))

(defn- -write-shortarray [conn sa]
  (doall (map #(-write-short %) sa)))

(defn- -write-int [conn i]
  (io! (.writeInt ^DataOutputStream (:out @conn) (int i))))

(defn- -write-long [conn i]
  (io! (.writeLong ^DataOutputStream (:out @conn) (long i))))

(defn- -write-double [conn i]
  (io! (.writeDouble ^DataOutputStream (:out @conn) (double i))))

(defn- -write-float [conn i]
  (io! (.writeFloat ^DataOutputStream (:out @conn) (float i))))

(defn- -write-string-utf8 [conn s]
  (io! (.writeUTF ^DataOutputStream (:out @conn) s)))

(defn- -write-string-ucs2 [conn s]
  (-write-short conn (count s))
  (io! (.writeChars ^DataOutputStream (:out @conn) s)))

(defn- -write-bool [conn b]
  (io! (.writeBoolean ^DataOutputStream (:out @conn) b)))

(defn- -write-metadata [conn m]
  ; TODO: Implement this.
  nil)


; Writing Packets ------------------------------------------------------------------
(defn- write-packet-keepalive [conn _]
  nil)

(defn- write-packet-handshake [conn {username :username}]
  (-write-string-ucs2 conn username))

(defn- write-packet-login [conn {version :version, username :username}]
  (-write-int conn version)
  (-write-string-ucs2 conn username)
  (-write-long conn 0)
  (-write-byte conn 0))

(defn- write-packet-chat [conn {message :message}]
  (-write-string-ucs2 conn message))

(defn- write-packet-respawn [conn {world :world}]
  (-write-byte conn world))

(defn- write-packet-player [conn {onground :onground}]
  (-write-bool conn onground))

(defn- write-packet-playerposition [conn {x :x y :y stance :stance z :z onground :onground}]
  (-write-double conn x)
  (-write-double conn y)
  (-write-double conn stance)
  (-write-double conn z)
  (-write-bool conn onground))

(defn- write-packet-playerlook [conn {yaw :yaw pitch :pitch onground :onground}]
  (-write-float conn yaw)
  (-write-float conn pitch)
  (-write-bool conn onground))

(defn- write-packet-playerpositionlook [conn {x :x y :y stance :stance z :z yaw :yaw pitch :pitch onground :onground}]
  (-write-double conn x)
  (-write-double conn y)
  (-write-double conn stance)
  (-write-double conn z)
  (-write-float conn yaw)
  (-write-float conn pitch)
  (-write-bool conn onground))

(defn- write-packet-playerdigging [conn {status :status x :x y :y z :z face :face}]
  (-write-byte conn status)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn face))

(defn- write-packet-playerblockplacement [conn {x :x y :y z :z direction :direction id :id amount :amount damage :damage}]
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn direction)
  (-write-short conn id)
  (-write-byte conn amount)
  (-write-short conn damage))

(defn- write-packet-holdingchange [conn {slot :slot}]
  (-write-short conn slot))

(defn- write-packet-usebed [conn {eid :eid inbed :inbed x :x y :y z :z}]
  (-write-int conn eid)
  (-write-byte conn inbed)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z))

(defn- write-packet-animation [conn {eid :eid animate :animate}]
  (-write-int conn eid)
  (-write-byte conn animate))

(defn- write-packet-entityaction [conn {eid :eid action :action}]
  (-write-int conn eid)
  (-write-byte conn action))

(defn- write-packet-pickupspawn [conn {eid :eid item :item count :count damagedata :damagedata x :x y :y z :z rotation :rotation pitch :pitch roll :roll}]
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
  (-write-int conn eid)
  (-write-string-ucs2 conn title)
  (-write-int conn x)
  (-write-int conn y)
  (-write-int conn x)
  (-write-int conn direction))

(defn- write-packet-stanceupdate [conn {unknown1 :unknown1 unknown2 :unknown2 unknown3 :unknown3 unknown4 :unknown4 unknown5 :unknown5 unknown6 :unknown6 }]
  (-write-float conn unknown1)
  (-write-float conn unknown2)
  (-write-bool conn unknown3)
  (-write-bool conn unknown4)
  (-write-float conn unknown5)
  (-write-float conn unknown6))

(defn- write-packet-entityvelocity [conn {eid :eid velocityx :velocityx velocityy :velocityy velocityz :velocityz}]
  (-write-int conn eid)
  (-write-short conn velocityx)
  (-write-short conn velocityy)
  (-write-short conn velocityz))

(defn- write-packet-attachentity [conn {eid :eid vehicleid :vehicleid}]
  (-write-int conn eid)
  (-write-int conn vehicleid))

(defn- write-packet-entitymetadata [conn {eid :eid metadata :metadata}]
  (-write-int conn eid)
  (-write-metadata conn metadata))

(defn- write-packet-multiblockchange [conn {chunkx :chunkx chunkz :chunkz arraysize :arraysize coordinatearray :coordinatearray typearray :typearray metadataarray :metadataarray}]
  (-write-int conn chunkx)
  (-write-int conn chunkz)
  (-write-short conn arraysize)
  (-write-shortarray conn coordinatearray)
  (-write-bytearray conn typearray)
  (-write-bytearray conn metadataarray))

(defn- write-packet-blockchange [conn {x :x y :y z :z blocktype :blocktype blockmetadata :blockmetadata}]
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-byte conn blocktype)
  (-write-byte conn blockmetadata))

(defn- write-packet-explosion [conn {x :x y :y z :z unknownradius :unknownradius recordcount :recordcount records :records}]
  ; TODO: Implement this.
  nil)

(defn- write-packet-soundeffect [conn {effectid :effectid x :x y :y z :z sounddata :sounddata}]
  (-write-int conn effectid)
  (-write-int conn x)
  (-write-byte conn y)
  (-write-int conn z)
  (-write-int conn sounddata))

(defn- write-packet-newinvalidstate [conn {reason :reason}]
  (-write-byte conn reason))

(defn- write-packet-openwindow [conn {windowid :windowid inventorytype :inventorytype windowtitle :windowtitle numberofslots :numberofslots}]
  (-write-byte conn windowid)
  (-write-byte conn inventorytype)
  (-write-string-utf8 conn windowtitle)
  (-write-byte conn numberofslots))

(defn- write-packet-closewindow [conn {windowid :windowid}]
  (-write-byte conn windowid))

(defn- write-packet-windowclick [conn {windowid :windowid slot :slot rightclick :rightclick actionnumber :actionnumber shift :shift itemid :itemid itemcount :itemcount itemuses :itemuses}]
  (-write-byte conn windowid)
  (-write-short conn slot)
  (-write-byte conn rightclick)
  (-write-short conn actionnumber)
  (-write-bool conn shift)
  (-write-short conn itemid)
  (-write-byte conn itemcount)
  (-write-short conn itemuses))

(defn- write-packet-transaction [conn {windowid :windowid actionnumber :actionnumber accepted :accepted}]
  (-write-byte conn windowid)
  (-write-short conn actionnumber)
  (-write-bool conn accepted))

(defn- write-packet-updatesign [conn {x :x y :y z :z text1 :text1 text2 :text2 text3 :text3 text4 :text4}]
  (-write-int conn x)
  (-write-short conn y)
  (-write-int conn z)
  (-write-string-ucs2 conn text1)
  (-write-string-ucs2 conn text2)
  (-write-string-ucs2 conn text3)
  (-write-string-ucs2 conn text4))

(defn- write-packet-incrementstatistic [conn {statisticid :statisticid amount :amount}]
  (-write-int conn statisticid)
  (-write-byte conn amount))

(defn- write-packet-disconnectkick [conn {reason :reason}]
  (-write-string-ucs2 conn reason))


(def packet-writers {:keepalive            write-packet-keepalive
                     :handshake            write-packet-handshake
                     :login                write-packet-login
                     :chat                 write-packet-chat
                     :respawn              write-packet-respawn
                     :player               write-packet-player
                     :playerposition       write-packet-playerposition
                     :playerlook           write-packet-playerlook
                     :playerpositionlook   write-packet-playerpositionlook
                     :playerdigging        write-packet-playerdigging
                     :playerblockplacement write-packet-playerblockplacement
                     :holdingchange        write-packet-holdingchange
                     :usebed               write-packet-usebed
                     :animation            write-packet-animation
                     :entityaction         write-packet-entityaction
                     :pickupspawn          write-packet-pickupspawn
                     :entitypainting       write-packet-entitypainting
                     :stanceupdate         write-packet-stanceupdate
                     :entityvelocity       write-packet-entityvelocity
                     :attachentity         write-packet-attachentity
                     :entitymetadata       write-packet-entitymetadata
                     :multiblockchange     write-packet-multiblockchange
                     :blockchange          write-packet-blockchange
                     :explosion            write-packet-explosion
                     :soundeffect          write-packet-soundeffect
                     :newinvalidstate      write-packet-newinvalidstate
                     :openwindow           write-packet-openwindow
                     :closewindow          write-packet-closewindow
                     :windowclick          write-packet-windowclick
                     :transaction          write-packet-transaction
                     :updatesign           write-packet-updatesign
                     :incrementstatistic   write-packet-incrementstatistic
                     :disconnectkick       write-packet-disconnectkick})

; Writing Wrappers -----------------------------------------------------------------
(defn- flushc [conn]
  (doto ^DataOutputStream (:out @conn) (.flush)))

(defn write-packet [bot packet-type payload]
  (let [conn (:connection bot)
        handler (packet-type packet-writers)]

    ; Record the packet type
    (dosync
      (let [counts (:packet-counts-out bot)
            current (get @counts packet-type 0)]
        (swap! counts
               assoc
               packet-type
               (inc current))))

    (-write-byte conn (packet-type packet-ids))
    (handler conn payload)
    (flushc conn)))

