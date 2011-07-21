(ns clojurecraft.in
  (:use [clojurecraft.util])
  (:use [clojurecraft.mappings])
  (:import (java.util.zip Inflater)))

; Bytes ----------------------------------------------------------------------------
(defn byte-seq [b]
  (loop [n 0 b b s []]
    (if (< n 8)
      (recur (inc n) (bit-shift-right b 1) (conj s (bit-and b 1)))
      (reverse s))))

(defn top [b]
  (bit-shift-right (bit-and b 0xf0) 4))

(defn bottom [b]
  (bit-and b 0x0f))


; Reading Data ---------------------------------------------------------------------
(defn- -read-byte [conn]
  (io! 
    (let [b (.readByte (:in @conn))]
      b)))

(defn- -read-bytearray [conn size]
  (io! 
    (let [ba (byte-array size)]
         (.read (:in @conn) ba 0 size)
         ba)))

(defn- -read-int [conn]
  (io! 
    (let [i (.readInt (:in @conn))]
      i)))

(defn- -read-long [conn]
  (io! 
    (let [i (.readLong (:in @conn))]
      i)))

(defn- -read-short [conn]
  (io!
    (let [i (.readShort (:in @conn))]
      i)))

(defn- -read-shortarray [conn size]
  (doall (repeatedly size #(-read-short conn))))

(defn- -read-bool [conn]
  (io!
    (let [b (.readBoolean (:in @conn))]
      b)))

(defn- -read-double [conn]
  (io!
    (let [i (.readDouble (:in @conn))]
      i)))

(defn- -read-float [conn]
  (io!
    (let [i (.readFloat (:in @conn))]
      i)))

(defn- -read-string-utf8 [conn]
  (io!
    (let [s (.readUTF (:in @conn))]
      s)))

(defn- -read-string-ucs2 [conn]
  (io!
    (let [str-len (.readShort (:in @conn))
          s (doall (apply str (repeatedly str-len #(.readChar (:in @conn)))))]
      s)))

(defn- -read-metadata [conn]
  (io! 
    (loop [data []]
      (let [x (-read-byte conn)]
        (if (= x 127)
          data
          (case (bit-shift-right x 5)
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
                                       :j (-read-int conn)
                                       :k (-read-int conn))))))))))


; Reading Packets ------------------------------------------------------------------
(defn- read-packet-keepalive [bot conn]
  {})

(defn- read-packet-handshake [bot conn]
  (assoc {}
         :hash (-read-string-ucs2 conn)))

(defn- read-packet-login [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :unknown (-read-string-ucs2 conn)
         :seed (-read-long conn)
         :dimension (-read-byte conn)))

(defn- read-packet-chat [bot conn]
  (assoc {}
         :message (-read-string-ucs2 conn)))

(defn- read-packet-timeupdate [bot conn]
  (let [payload (assoc {}
                       :time (-read-long conn))]
    (dosync (alter (:world bot) assoc :time (:time payload)))
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
  (assoc {}
         :user (-read-int conn)
         :target (-read-int conn)
         :leftclick (-read-bool conn)))

(defn- read-packet-updatehealth [bot conn]
  (assoc {}
         :health (-read-short conn)))

(defn- read-packet-respawn [bot conn]
  (assoc {}
         :world (-read-byte conn)))

(defn- read-packet-playerpositionlook [bot conn]
  (let [payload (assoc {}
                       :x (-read-double conn)
                       :stance (-read-double conn)
                       :y (-read-double conn)
                       :z (-read-double conn)
                       :yaw (-read-float conn)
                       :pitch (-read-float conn)
                       :onground (-read-bool conn))]
    (dosync (alter (:player bot) merge {:location payload}))
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
         :id (-read-short conn)
         :amount (-read-byte conn)
         :damage (-read-short conn)))

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
  (assoc {}
         :eid (-read-int conn)
         :playername (-read-string-ucs2 conn)
         :x (-read-int conn)
         :y (-read-int conn)
         :z (-read-int conn)
         :rotation (-read-byte conn)
         :pitch (-read-byte conn)
         :currentitem (-read-short conn)))

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
                          :moar (-read-int conn))]
    (if (< 0 (:moar basepacket))
      basepacket
      (assoc basepacket
             :unknownx (-read-int conn)
             :unknowny (-read-int conn)
             :unknownz (-read-int conn)))))

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

(defn- read-packet-stanceupdate [bot conn]
  (assoc {}
         :unknown1 (-read-float conn)
         :unknown2 (-read-float conn)
         :unknown3 (-read-bool conn)
         :unknown4 (-read-bool conn)
         :unknown5 (-read-float conn)
         :unknown6 (-read-float conn)))

(defn- read-packet-entityvelocity [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :velocityx (-read-short conn)
         :velocityy (-read-short conn)
         :velocityz (-read-short conn)))

(defn- read-packet-entitydestroy [bot conn]
  (assoc {}
         :eid (-read-int conn)))

(defn- read-packet-entity [bot conn]
  (assoc {}
         :eid (-read-int conn)))

(defn- read-packet-entityrelativemove [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :dx (-read-byte conn)
         :dy (-read-byte conn)
         :dz (-read-byte conn)))

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
  (assoc {}
         :eid (-read-int conn)
         :x (-read-int conn)
         :y (-read-int conn)
         :z (-read-int conn)
         :yaw (-read-byte conn)
         :pitch (-read-byte conn)))

(defn- read-packet-entitystatus [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :entitystatus (-read-byte conn)))

(defn- read-packet-attachentity [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :vehicleid (-read-int conn)))

(defn- read-packet-entitymetadata [bot conn]
  (assoc {}
         :eid (-read-int conn)
         :metadata (-read-metadata conn)))

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
      [nibbles data]
      (let [next-byte (get data 0)
            top-byte (top next-byte)
            bottom-byte (bottom next-byte)]
        (recur (+ i 2)
               (conj nibbles bottom-byte top-byte)
               (subvec data 1))))))

(defn- -read-packet-mapchunk-decode [predata data-ba]
  (let [len (* (:sizex predata) (:sizey predata) (:sizez predata))
        data (into [] data-ba)
        block-types (subvec data 0 len)
        data (subvec data len)]
    (let [[block-metadata data] (-parse-nibbles len data)
          [block-light data] (-parse-nibbles len data)
          [sky-light data] (-parse-nibbles len data)]
      (map #({:blocktype %1 :blockmeta %2 :blocklight %3 :skylight %4})
           block-types block-metadata block-light sky-light))))

(defn- -read-packet-mapchunk-chunkdata [conn predata]
  (let [raw-data (-read-bytearray conn (:compressedsize predata))
        buffer (byte-array (/ (* 5
                                 (:sizex predata)
                                 (:sizey predata)
                                 (:sizez predata)) 2))
        decompressor (Inflater.)]
    (.setInput decompressor raw-data 0 (:compressedsize predata))
    (.inflate decompressor buffer)
    (.end decompressor)
    buffer))

(defn- read-packet-mapchunk [bot conn]
  (let [predata (assoc {}
                       :x (-read-int conn)
                       :y (-read-short conn)
                       :z (-read-int conn)
                       :sizex (+ 1 (-read-byte conn))
                       :sizey (+ 1 (-read-byte conn))
                       :sizez (+ 1 (-read-byte conn))
                       :compressedsize (-read-int conn))]
    (let [decompressed-data (-read-packet-mapchunk-chunkdata conn predata)]
      (assoc predata :data (-read-packet-mapchunk-decode predata decompressed-data)))))


(defn- read-packet-multiblockchange [bot conn]
  (let [prearrays (assoc {}
                         :chunkx (-read-int conn)
                         :chunkz (-read-int conn)
                         :arraysize (-read-short conn))]
    (assoc prearrays
           :coordinatearray (-read-shortarray conn (:arraysize prearrays))
           :typearray (-read-bytearray conn (:arraysize prearrays))
           :metadataarray (-read-bytearray conn (:arraysize prearrays)))))

(defn- read-packet-blockchange [bot conn]
  (assoc {}
         :x (-read-int conn)
         :y (-read-byte conn)
         :z (-read-int conn)
         :blocktype (-read-byte conn)
         :blockmetadata (-read-byte conn)))

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
         :reason (-read-byte conn)))

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
         :windowtitle (-read-string-utf8 conn)
         :numberofslots (-read-byte conn)))

(defn- read-packet-closewindow [bot conn]
  (assoc {}
         :windowid (-read-byte conn)))

(defn- read-packet-setslot [bot conn]
  (let [preiteminfo (assoc {}
                           :windowid (-read-byte conn)
                           :slot (-read-short conn)
                           :itemid (-read-short conn))]
    (if (= -1 (:itemid preiteminfo))
      preiteminfo
      (assoc preiteminfo
             :itemcount (-read-byte conn)
             :itemuses (-read-short conn)))))

(defn- -read-packet-windowitems-payloaditem [conn]
  (let [payload (assoc {} :itemid (-read-short conn))]
    (if (= (:itemid payload) -1)
      payload
      (assoc payload
        :count (-read-byte conn)
        :uses (-read-short conn)))))

(defn- read-packet-windowitems [bot conn]
  (let [prepayload (assoc {}
                     :windowid (-read-byte conn)
                     :count (-read-short conn))
        items (doall (repeatedly (:count prepayload)
                                 #(-read-packet-windowitems-payloaditem conn)))]
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
  (let [pretext (assoc {}
                  :unknown1 (-read-int conn)
                  :unknown2 (-read-short conn)
                  :textlength (-read-int conn))]
    (assoc pretext :text (-read-bytearray (:textlength pretext)))))

(defn- read-packet-incrementstatistic [bot conn]
  (assoc {}
    :statisticid (-read-int conn)
    :amount (-read-byte conn)))

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
                     :stanceupdate              read-packet-stanceupdate
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
                     :updatesign                read-packet-updatesign
                     :mapdata                   read-packet-mapdata
                     :incrementstatistic        read-packet-incrementstatistic
                     :disconnectkick            read-packet-disconnectkick})

; Reading Wrappers -----------------------------------------------------------------
(defn read-packet [bot]
  (let [conn (:connection bot)
        packet-id-byte (-read-byte conn)]
    (let [packet-id (when (not (nil? packet-id-byte))
                      (int packet-id-byte))
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
          (println (str "UNKNOWN PACKET TYPE: " (Integer/toHexString packet-id)))
          (/ 1 0))
        (let [payload (do ((packet-type packet-readers) bot conn))]
          (do
            (when (#{} packet-type)
              (println (str "--PACKET--> " packet-type)))
            payload))))))

