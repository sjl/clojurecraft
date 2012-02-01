(ns clojurecraft.in
  (:use [clojurecraft.util])
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.chunks])
  (:import (java.io DataInputStream))
  (:require [clojurecraft.data])
  (:require [clojurecraft.events :as events])
  (:import [clojurecraft.data Location Entity Chunk])
  (:import (java.util.zip Inflater)))

(def FULL-CHUNK (* 16 16 128))
(def BLANK-CHUNK-ARRAY (byte-array FULL-CHUNK))

; Convenience Functions ------------------------------------------------------------
(defn- blank-entity []
  (Entity. nil
           (Location. nil nil nil nil nil nil nil)
           nil nil false 0.0))

(def enchantable? #{
  0x103 0x105 0x15A 0x167

  0x10C 0x10D 0x10E 0x10F 0x122
  0x110 0x111 0x112 0x113 0x123
  0x10B 0x100 0x101 0x102 0x124
  0x114 0x115 0x116 0x117 0x125
  0x11B 0x11C 0x11D 0x11E 0x126

  0x12A 0x12B 0x12C 0x12D
  0x12E 0x12F 0x130 0x131
  0x132 0x133 0x134 0x135
  0x136 0x137 0x138 0x139
  0x13A 0x13B 0x13C 0x13D })

; Reading Data ---------------------------------------------------------------------
(defn- -read-byte-bare [conn]
  (io!
    (let [b (.readByte ^DataInputStream (:in @conn))]
      b)))

(defn- -read-byte [conn]
  (int (-read-byte-bare conn)))

(defn- -read-byte-unsigned [conn]
  ; TODO Fix this.
  (int (-read-byte-bare conn)))

(defn- -read-bytearray-bare [conn size]
  (io!
    (let [ba (byte-array size)]
         (.readFully ^DataInputStream (:in @conn) ba 0 size)
         ba)))

(defn- -read-bytearray [conn size]
  (vec (-read-bytearray-bare conn size)))

(defn- -read-int [conn]
  (io!
    (let [i (.readInt ^DataInputStream (:in @conn))]
      (Integer. i))))

(defn- -read-long [conn]
  (io!
    (let [l (.readLong ^DataInputStream (:in @conn))]
      (Long. l))))

(defn- -read-short [conn]
  (io!
    (let [s (.readShort ^DataInputStream (:in @conn))]
      (Short. s))))

(defn- -read-shortarray [conn size]
  (doall (repeatedly size #(-read-short conn))))

(defn- -read-bool [conn]
  (io!
    (let [b (.readBoolean ^DataInputStream (:in @conn))]
      (Boolean. b))))

(defn- -read-double [conn]
  (io!
    (let [d (.readDouble ^DataInputStream (:in @conn))]
      (Double. d))))

(defn- -read-float [conn]
  (io!
    (let [f (.readFloat ^DataInputStream (:in @conn))]
      (Float. f))))

(defn- -read-string-utf8 [conn]
  (io!
    (let [s (.readUTF ^DataInputStream (:in @conn))]
      s)))

(defn- -read-string-ucs2 [conn]
  (io!
    (let [str-len (.readShort ^DataInputStream (:in @conn))
          s (doall (apply str (repeatedly str-len #(.readChar ^DataInputStream (:in @conn)))))]
      s)))

(defn- -read-metadata [conn]
  (io!
    (loop [data []]
      (let [x (-read-byte conn)]
        (if (= x 127)
          data
          (case (bit-shift-right (to-unsigned x) 5)
            0 (recur (conj data (-read-byte conn)))
            1 (recur (conj data (-read-short conn)))
            2 (recur (conj data (-read-int conn)))
            3 (recur (conj data (-read-float conn)))
            4 (recur (conj data (-read-string-ucs2 conn)))
            5 (recur (conj data (assoc {}
                                       :id (-read-short conn)
                                       :count (-read-byte conn)
                                       :damage (-read-short conn))))
            6 (recur (conj data (assoc {}
                                       :i (-read-int conn)
                                       :y (-read-int conn)
                                       :z (-read-int conn))))))))))

(defn- -read-slot [conn]
  (io!
    (let [data {:id (-read-short conn)}]
      (if (= (:id data) -1)
        data ; Empty slot.
        (let [data (assoc data
                          :count (-read-byte conn)
                          :damagemeta (-read-short conn))]
          (if (not (enchantable? (:id data)))
            data
            (let [nbt-length (-read-short conn)
                  nbt-data (-read-bytearray conn nbt-length)]
              ; TODO: Actually decompress and store this NBT data.
              data)))))))


; Reading Packets ------------------------------------------------------------------

(defn- read-packet-keepalive [bot conn]
  (assoc {}
    :keep-alive-id (-read-int conn)))

(defn- read-packet-handshake [bot conn]
  (assoc {}
         :hash (-read-string-ucs2 conn)))

(defn- read-packet-login [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :unknown (-read-string-ucs2 conn)
         :seed (-read-long conn)
         :leveltype (-read-string-ucs2 conn)
         :servermode (-read-int conn)
         :dimension (-read-byte conn)
         :difficulty (-read-byte conn)
         :worldheight (-read-byte-unsigned conn)
         :maxplayers (-read-byte-unsigned conn)))

(defn- read-packet-chat [bot conn]
  (let [payload (assoc {}
                       :message (-read-string-ucs2 conn))]
    (events/fire-chat bot (:message payload))
    payload))

(defn- read-packet-timeupdate [bot conn]
  (let [payload (assoc {}
                       :time (-read-long conn))]
    (dosync (ref-set (:time (:world bot)) (:time payload)))
    payload))

(defn- read-packet-equipment [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :slot (-read-short conn)
         :itemid (-read-short conn)
         :unknown (-read-short conn)))

(defn- read-packet-spawnposition [bot conn]
  (assoc {}
         :x (-read-int conn)
         :y (-read-int conn)
         :z (-read-int conn)))

(defn- read-packet-useentity [bot conn]
  (let [payload (assoc {}
                       :user (-read-int conn)
                       :target (-read-int conn)
                       :leftclick (-read-bool conn))]
    payload))

(defn- read-packet-updatehealth [bot conn]
  (let [payload (assoc {}
                       :health (-read-short conn)
                       :food (-read-short conn)
                       :foodsaturation (-read-float conn))]
    (if (<= (:health payload) 0)
      (events/fire-dead bot))
    payload))

(defn- read-packet-respawn [bot conn]
  (assoc {}
         :world (-read-byte conn)
         :difficulty (-read-byte conn)
         :creative (-read-byte conn)
         :worldheight (-read-short conn)
         :mapseed (-read-long conn)
         :leveltype (-read-string-ucs2 conn)))

(defn- read-packet-playerpositionlook [bot conn]
  (let [payload (assoc {}
                       :x (-read-double conn)
                       :stance (-read-double conn)
                       :y (-read-double conn)
                       :z (-read-double conn)
                       :yaw (-read-float conn)
                       :pitch (-read-float conn)
                       :onground (-read-bool conn))]
    (dosync
      (alter (:player bot)
             assoc :loc (merge (Location. nil nil nil nil nil nil nil)
                               payload)))
    payload))

(defn- read-packet-playerdigging [bot conn]
  (assoc {}
         :status (-read-byte conn)
         :x (-read-int conn)
         :y (-read-byte conn)
         :z (-read-int conn)
         :face (-read-byte conn)))

(defn- read-packet-playerblockplacement [bot conn]
  (assoc {}
         :x (-read-int conn)
         :y (-read-byte conn)
         :z (-read-int conn)
         :direction (-read-byte conn)
         :helditem (-read-slot conn)))

(defn- read-packet-holdingchange [bot conn]
  (assoc {}
         :slot (-read-short conn)))

(defn- read-packet-usebed [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :inbed (-read-byte conn)
         :x (-read-int conn)
         :y (-read-byte conn)
         :z (-read-int conn)))

(defn- read-packet-animation [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :animate (-read-byte conn)))

(defn- read-packet-entityaction [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :action (-read-byte conn)))

(defn- read-packet-namedentityspawn [bot conn]
  (let [payload (assoc {}
                       :eid (-read-int conn)
                       :playername (-read-string-ucs2 conn)
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn)
                       :rotation (-read-byte conn)
                       :pitch (-read-byte conn)
                       :currentitem (-read-short conn))
        entity-data {:eid (:eid payload)
                     :name (:playername payload)
                     :holding (item-types (:currentitem payload))
                     :despawned false}
        location-data {:x (float (/ (:x payload) 32)) ; These are sent as absolute
                       :y (float (/ (:y payload) 32)) ; ints, not normal floats.
                       :z (float (/ (:z payload) 32))}]
    (dosync
      (let [eid (:eid payload)
            entities (:entities (:world bot))
            entity (or (let [e (@entities eid)] (when e @e)) (blank-entity))
            new-loc (merge (:loc entity) location-data)
            new-entity-data (assoc entity-data :loc new-loc)]
        ; I'm not sure this is right.  We should probably be altering the entity ref
        ; if it already exists.
        (alter entities assoc eid (ref (merge entity new-entity-data)))))
    payload))

(defn- read-packet-pickupspawn [bot conn]
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

(defn- read-packet-collectitem [bot conn]
  (assoc {}
         :collectedeid (-read-int conn)
         :collectoreid (-read-int conn)))

(defn- read-packet-addobjectvehicle [bot conn]
  (let [basepacket (assoc {}
                          :eid (-read-int conn)
                          :type (-read-byte conn)
                          :x (-read-int conn)
                          :y (-read-int conn)
                          :z (-read-int conn)
                          :throwerid (-read-int conn))]
    (if (> (:throwerid basepacket) 0)
      (assoc basepacket
             :speedx (-read-int conn)
             :speedy (-read-int conn)
             :speedz (-read-int conn))
      basepacket)))

(defn- read-packet-mobspawn [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :type (-read-byte conn)
         :x (-read-int conn)
         :y (-read-int conn)
         :z (-read-int conn)
         :yaw (-read-byte conn)
         :pitch (-read-byte conn)
         :datastream (-read-metadata conn)))

(defn- read-packet-entitypainting [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :type (-read-string-ucs2 conn)
         :x (-read-int conn)
         :y (-read-int conn)
         :z (-read-int conn)
         :direction (-read-int conn)))

(defn- read-packet-experienceorb [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :x (-read-int conn)
         :y (-read-int conn)
         :z (-read-int conn)
         :count (-read-short conn)))

(defn- read-packet-entityvelocity [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :velocityx (-read-short conn)
         :velocityy (-read-short conn)
         :velocityz (-read-short conn)))

(defn- read-packet-entitydestroy [bot conn]
  (let [payload (assoc {}
                       :eid (-read-int conn))]
    (dosync (alter (:entities (:world bot)) dissoc (:eid payload)))
    payload))

(defn- read-packet-entity [bot conn]
  (let [payload (assoc {}
                       :eid (-read-int conn))]
    (dosync
      (let [eid (:eid payload)
            entities (:entities (:world bot))
            entity (@entities eid)]
        (when-not entity
          (alter entities assoc eid (ref (Entity. eid nil nil nil false 0.0))))))
    payload))

(defn- read-packet-entityrelativemove [bot conn]
  ; TODO: handle items
  (let [payload (assoc {}
                       :eid (-read-int conn)
                       :dx (float (/ (-read-byte conn) 32))
                       :dy (float (/ (-read-byte conn) 32))
                       :dz (float (/ (-read-byte conn) 32)))]
    (dosync
      (let [entity (@(:entities (:world bot)) (:eid payload))]
        (when entity
          (let [old-loc (:loc @entity)
                new-loc (merge old-loc {:x (+ (:x old-loc) (:dx payload))
                                        :y (+ (:y old-loc) (:dy payload))
                                        :z (+ (:z old-loc) (:dz payload))})]
            (alter entity assoc :loc new-loc)))))
    payload))

(defn- read-packet-entitylook [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :yaw (-read-byte conn)
         :pitch (-read-byte conn)))

(defn- read-packet-entitylookandrelativemove [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :dx (-read-byte conn)
         :dy (-read-byte conn)
         :dz (-read-byte conn)
         :yaw (-read-byte conn)
         :pitch (-read-byte conn)))

(defn- read-packet-entityteleport [bot conn]
  ; TODO: record yaw/pitch
  (let [payload (assoc {}
                       :eid (-read-int conn)
                       :x (float (/ (-read-int conn) 32))
                       :y (float (/ (-read-int conn) 32))
                       :z (float (/ (-read-int conn) 32))
                       :yaw (-read-byte conn)
                       :pitch (-read-byte conn))]
    #_(dosync
      (let [entity (@(:entities (:world bot)) (:eid payload))
            old-loc (:loc @entity)
            new-loc (merge old-loc {:x (:x payload)
                                    :y (:y payload)
                                    :z (:z payload)})]
        (alter entity assoc :loc new-loc)))
    payload))

(defn- read-packet-entitystatus [bot conn]
  (let [payload (assoc {}
                       :eid (-read-int conn)
                       :entitystatus (-read-byte conn))]
    payload))

(defn- read-packet-attachentity [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :vehicleid (-read-int conn)))

(defn- read-packet-entitymetadata [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :metadata (-read-metadata conn)))

(defn- read-packet-entityeffect [bot conn]
  (let [payload (assoc {}
                       :eid (-read-int conn)
                       :effectid (-read-byte conn)
                       :amplifier (-read-byte conn)
                       :duration (-read-short conn))]
    payload))

(defn- read-packet-removeentityeffect [bot conn]
  (let [payload (assoc {}
                       :eid (-read-int conn)
                       :effectid (-read-byte conn))]
    payload))

(defn- read-packet-experience [bot conn]
  (let [payload (assoc {}
                       :experiencebar (-read-float conn)
                       :level (-read-short conn)
                       :totalexperience (-read-short conn))]
    payload))


(defn- read-packet-prechunk [bot conn]
  (assoc {}
         :x (-read-int conn)
         :z (-read-int conn)
         :mode (-read-bool conn)))


(defn- -parse-nibbles [len data]
  (loop [i 0
         nibbles []
         data data]
    (if (= i len)
      [(byte-array nibbles) data]
      (let [next-byte (get data 0)
            top-byte (top next-byte)
            bottom-byte (bottom next-byte)]
        (recur (+ i 2)
               (conj nibbles bottom-byte top-byte)
               (subvec data 1))))))

(defn- -get-or-make-chunk [chunks coords]
  (or (@chunks coords)
      (let [chunk (ref (Chunk. BLANK-CHUNK-ARRAY
                               BLANK-CHUNK-ARRAY
                               BLANK-CHUNK-ARRAY
                               BLANK-CHUNK-ARRAY))]
        (alter chunks assoc coords chunk)
        chunk)))

(defn- -decode-mapchunk [postdata data-ba]
  (let [len (* (:sizex postdata) (:sizey postdata) (:sizez postdata))
        data (into (vector-of :byte) data-ba) ; Make the data a vector for easier parsing.
        block-types (byte-array (subvec data 0 len))
        data (subvec data len)]
    (let [[block-metadata data] (-parse-nibbles len data)
          [block-light data] (-parse-nibbles len data)
          [sky-light data] (-parse-nibbles len data)]
      [block-types block-metadata block-light sky-light])))

(defn- -decompress-mapchunk [postdata]
  (let [buffer (byte-array (/ (* 5
                                 (:sizex postdata)
                                 (:sizey postdata)
                                 (:sizez postdata)) 2))
        decompressor (Inflater.)]
    (.setInput decompressor (:raw-data postdata) 0 (:compressedsize postdata))
    (.inflate decompressor buffer)
    (.end decompressor)
    buffer))

(defn- -read-mapchunk-predata [conn]
  (assoc {}
         :x (-read-int conn)
         :y (-read-short conn)
         :z (-read-int conn)
         :sizex (+ 1 (-read-byte conn))
         :sizey (+ 1 (-read-byte conn))
         :sizez (+ 1 (-read-byte conn))
         :compressedsize (-read-int conn)))


(defn- -chunk-from-full-data [postdata]
  (let [decompressed-data (-decompress-mapchunk postdata)
        [types meta light sky] (-decode-mapchunk postdata decompressed-data)] ; These are all byte-array's!
    (Chunk. types meta light sky)))

(defn- -chunk-from-partial-data [{{chunks :chunks} :world} postdata]
  (let [x (:x postdata)
        y (:y postdata)
        z (:z postdata)
        decompressed-data (-decompress-mapchunk postdata)
        [types meta light sky] (-decode-mapchunk postdata decompressed-data)  ; These are all byte-array's!
        chunk-coords (coords-of-chunk-containing x z)
        chunk (force (-get-or-make-chunk chunks chunk-coords))
        start-index (block-index-in-chunk x y z)]
    (Chunk. (replace-array-slice (:types (force @chunk)) start-index types)
            (replace-array-slice (:metadata (force @chunk)) start-index meta)
            (replace-array-slice (:light (force @chunk)) start-index light)
            (replace-array-slice (:sky-light (force @chunk)) start-index sky))))

(defn- read-packet-mapchunk [bot conn]
  (let [predata (-read-mapchunk-predata conn)
        postdata (assoc predata :raw-data (-read-bytearray-bare conn
                                                                (:compressedsize predata)))
        chunk-size (* (:sizex postdata) (:sizey postdata) (:sizez postdata))
        chunk-coords (coords-of-chunk-containing (:x postdata) (:z postdata))]
    (dosync (alter (:chunks (:world bot))
                   assoc chunk-coords (if (= FULL-CHUNK chunk-size)
                                        (ref (delay (-chunk-from-full-data postdata)))
                                        (ref (-chunk-from-partial-data bot postdata)))))
    predata))



(defn update-delayed [chunk index type meta]
  (let [chunk (force chunk)]
    (assoc chunk
           :types (replace-array-index (:types chunk) index type)
           :metadata (replace-array-index (:metadata chunk) index meta))))

(defn -update-single-block [bot x y z type meta]
  (dosync
    (let [chunk (chunk-containing x z (:chunks (:world bot)))
          i (block-index-in-chunk x y z)]
      (when chunk
        (alter chunk update-delayed i type meta)))))

(defn- read-packet-blockchange [bot conn]
  (let [data (assoc {}
                    :x (-read-int conn)
                    :y (-read-byte conn)
                    :z (-read-int conn)
                    :blocktype (-read-byte conn)
                    :blockmetadata (-read-byte conn))]
    (-update-single-block bot
                          (:x data) (:y data) (:z data)
                          (:blocktype data) (:blockmetadata data))
    data))

(defn- read-packet-multiblockchange [bot conn]
  (let [prearrays (assoc {}
                         :chunkx (-read-int conn)
                         :chunkz (-read-int conn)
                         :arraysize (-read-short conn))
        payload (assoc prearrays
                       :coordinatearray (-read-shortarray conn (:arraysize prearrays))
                       :typearray (-read-bytearray conn (:arraysize prearrays))
                       :metadataarray (-read-bytearray conn (:arraysize prearrays)))
        parse-coords (fn [s] [(top-4 s) (mid-4 s) (bottom-8 s)])
        coords (map parse-coords (:coordinatearray payload))]
    (dorun (map #(-update-single-block bot (get %1 0) (get %1 2) (get %1 1) %2 %3)
                coords
                (:typearray payload)
                (:metadataarray payload)))
    payload))


(defn- read-packet-playnoteblock [bot conn]
  (assoc {}
         :x (-read-int conn)
         :y (-read-short conn)
         :z (-read-int conn)
         :instrumenttype (-read-byte conn)
         :pitch (-read-byte conn)))

(defn- read-packet-explosion [bot conn]
  (let [prerecords (assoc {}
                          :x (-read-int conn)
                          :y (-read-short conn)
                          :z (-read-int conn)
                          :unknownradius (-read-byte conn)
                          :recordcount (-read-byte conn))]
    (assoc prerecords
           :records (-read-bytearray conn
                                     (* 3 (:recordcount prerecords))))))

(defn- read-packet-soundeffect [bot conn]
  (assoc {}
         :effectid (-read-int conn)
         :x (-read-int conn)
         :y (-read-byte conn)
         :z (-read-int conn)
         :sounddata (-read-int conn)))

(defn- read-packet-newinvalidstate [bot conn]
  (assoc {}
         :reason (-read-byte conn)
         :game-mode (-read-byte conn)))

(defn- read-packet-thunderbolt [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :unknown (-read-bool conn)
         :x (-read-int conn)
         :y (-read-int conn)
         :z (-read-int conn)))

(defn- read-packet-openwindow [bot conn]
  (assoc {}
         :windowid (-read-byte conn)
         :inventorytype (-read-byte conn)
         :windowtitle (-read-string-ucs2 conn)
         :numberofslots (-read-byte conn)))

(defn- read-packet-closewindow [bot conn]
  (assoc {}
         :windowid (-read-byte conn)))

(defn- read-packet-setslot [bot conn]
  (let [payload (assoc {}
                       :windowid (-read-byte conn)
                       :slot (-read-short conn)
                       :slotdata (-read-slot conn))]
    payload))

(defn- read-packet-windowitems [bot conn]
  (let [prepayload (assoc {}
                     :windowid (-read-byte conn)
                     :count (-read-short conn))
        items (doall (repeatedly (:count prepayload)
                                 #(-read-slot conn)))]
    (assoc prepayload :items items)))

(defn- read-packet-updateprogressbar [bot conn]
  (assoc {}
    :windowid (-read-byte conn)
    :progressbar (-read-short conn)
    :value (-read-short conn)))

(defn- read-packet-transaction [bot conn]
  (assoc {}
    :windowid (-read-byte conn)
    :actionnumber (-read-short conn)
    :accepted (-read-short conn)))

(defn- read-packet-creativeinventoryaction [bot conn]
  (assoc {}
    :slot (-read-short conn)
    :clickeditem (-read-slot conn)))

(defn- read-packet-enchantitem [bot conn]
  (assoc {}
    :windowid (-read-byte conn)
    :enchantment (-read-byte conn)))

(defn- read-packet-updatesign [bot conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-short conn)
    :z (-read-int conn)
    :text1 (-read-string-ucs2 conn)
    :text2 (-read-string-ucs2 conn)
    :text3 (-read-string-ucs2 conn)
    :text4 (-read-string-ucs2 conn)))

(defn- read-packet-mapdata [bot conn]
  ; TODO: Fix this
  (let [pretext (assoc {}
                  :unknown1 (-read-int conn)
                  :unknown2 (-read-short conn)
                  :textlength (-read-int conn))]
    (assoc pretext :text (-read-bytearray (:textlength pretext)))))

(defn- read-packet-incrementstatistic [bot conn]
  (assoc {}
    :statisticid (-read-int conn)
    :amount (-read-byte conn)))

(defn- read-packet-playerlistitem [bot conn]
  (assoc {}
    :playername (-read-string-ucs2 conn)
    :online (-read-bool conn)
    :ping (-read-short conn)))

(defn- read-packet-pluginmessage [bot conn]
  (let [predata (assoc {}
                       :channel (-read-string-ucs2 conn)
                       :length (-read-short conn))]
    (assoc predata :data (-read-bytearray conn (:length predata)))))

(defn- read-packet-disconnectkick [bot conn]
  (assoc {}
    :reason (-read-string-ucs2 conn)))


(def packet-readers {:keepalive                 read-packet-keepalive
                     :handshake                 read-packet-handshake
                     :login                     read-packet-login
                     :chat                      read-packet-chat
                     :timeupdate                read-packet-timeupdate
                     :equipment                 read-packet-equipment
                     :spawnposition             read-packet-spawnposition
                     :useentity                 read-packet-useentity
                     :updatehealth              read-packet-updatehealth
                     :respawn                   read-packet-respawn
                     :playerpositionlook        read-packet-playerpositionlook
                     :playerdigging             read-packet-playerdigging
                     :playerblockplacement      read-packet-playerblockplacement
                     :holdingchange             read-packet-holdingchange
                     :usebed                    read-packet-usebed
                     :animation                 read-packet-animation
                     :entityaction              read-packet-entityaction
                     :namedentityspawn          read-packet-namedentityspawn
                     :pickupspawn               read-packet-pickupspawn
                     :collectitem               read-packet-collectitem
                     :addobjectvehicle          read-packet-addobjectvehicle
                     :mobspawn                  read-packet-mobspawn
                     :entitypainting            read-packet-entitypainting
                     :experienceorb             read-packet-experienceorb
                     :entityvelocity            read-packet-entityvelocity
                     :entitydestroy             read-packet-entitydestroy
                     :entity                    read-packet-entity
                     :entityrelativemove        read-packet-entityrelativemove
                     :entitylook                read-packet-entitylook
                     :entitylookandrelativemove read-packet-entitylookandrelativemove
                     :entityteleport            read-packet-entityteleport
                     :entitystatus              read-packet-entitystatus
                     :attachentity              read-packet-attachentity
                     :entitymetadata            read-packet-entitymetadata
                     :entityeffect              read-packet-entityeffect
                     :removeentityeffect        read-packet-removeentityeffect
                     :experience                read-packet-experience
                     :prechunk                  read-packet-prechunk
                     :mapchunk                  read-packet-mapchunk
                     :multiblockchange          read-packet-multiblockchange
                     :blockchange               read-packet-blockchange
                     :playnoteblock             read-packet-playnoteblock
                     :explosion                 read-packet-explosion
                     :soundeffect               read-packet-soundeffect
                     :newinvalidstate           read-packet-newinvalidstate
                     :thunderbolt               read-packet-thunderbolt
                     :openwindow                read-packet-openwindow
                     :closewindow               read-packet-closewindow
                     :setslot                   read-packet-setslot
                     :windowitems               read-packet-windowitems
                     :updateprogressbar         read-packet-updateprogressbar
                     :transaction               read-packet-transaction
                     :creativeinventoryaction   read-packet-creativeinventoryaction
                     :enchantitem               read-packet-enchantitem
                     :updatesign                read-packet-updatesign
                     :mapdata                   read-packet-mapdata
                     :incrementstatistic        read-packet-incrementstatistic
                     :playerlistitem            read-packet-playerlistitem
                     :pluginmessage             read-packet-pluginmessage
                     :disconnectkick            read-packet-disconnectkick})

; Reading Wrappers -----------------------------------------------------------------
(defn read-packet [bot prev prev-prev prev-prev-prev]
  (let [conn (:connection bot)
        packet-id-byte (to-unsigned (-read-byte conn))]
    (let [packet-id (when (not (nil? packet-id-byte))
                      packet-id-byte)
          packet-type (packet-types packet-id)]

      ; Record the packet type
      (dosync
        (let [counts (:packet-counts-in bot)
              current (get @counts packet-type 0)]
          (swap! counts
                 assoc
                 packet-type
                 (inc current))))

      ; Handle packet
      (if (nil? packet-type)
        (do
          (println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
          (println "UNKNOWN PACKET TYPE:" (Integer/toHexString packet-id) packet-id)
          (println "THREE-AGO:" prev-prev-prev)
          (println "TWO-AGO:" prev-prev)
          (println "ONE-AGO:" prev)
          (/ 1 0))
        (let [payload (do ((packet-type packet-readers) bot conn))]
          (do
            (when (#{} packet-type)
              (println (str "--PACKET--> " packet-type)))
            [[packet-type payload] prev prev-prev]))))))

