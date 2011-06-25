(ns clojurecraft.in
  (:use [clojurecraft.mappings]))

; Reading Data ---------------------------------------------------------------------
(defn- -read-byte [conn]
  (let [b (.readByte (:in @conn))]
    b))

(defn- -read-bytearray [conn size]
  (let [ba (byte-array size)]
    (.read (:in @conn) ba 0 size)
    ba))

(defn- -read-int [conn]
  (let [i (.readInt (:in @conn))]
    i))

(defn- -read-long [conn]
  (let [i (.readLong (:in @conn))]
    i))

(defn- -read-short [conn]
  (let [i (.readShort (:in @conn))]
    i))

(defn- -read-shortarray [conn size]
  (repeatedly size (-read-short conn)))

(defn- -read-bool [conn]
  (let [b (.readBoolean (:in @conn))]
    b))

(defn- -read-double [conn]
  (let [i (.readDouble (:in @conn))]
    i))

(defn- -read-float [conn]
  (let [i (.readFloat (:in @conn))]
    i))

(defn- -read-string-utf8 [conn]
  (let [s (.readUTF (:in @conn))]
    s))

(defn- -read-string-ucs2 [conn]
  (let [str-len (.readShort (:in @conn))
                s (apply str (repeatedly str-len #(.readChar (:in @conn))))]
    s))

(defn- -read-metadata [conn]
  (loop [data []]
    (let [x (-read-byte conn)]
      (if (= x 127)
        nil
        (case (bit-shift-right x 5)
          0 (recur (conj data (-read-byte conn)))
          1 (recur (conj data (-read-short conn)))
          2 (recur (conj data (-read-int conn)))
          3 (recur (conj data (-read-float conn)))
          4 (recur (conj data (-read-string-ucs2 conn)))
          5 (recur (conj data [(-read-short conn)
                               (-read-byte conn)
                               (-read-short conn)]))
          6 (recur (conj data [(-read-int conn)
                               (-read-int conn)
                               (-read-int conn)])))))))


; Reading Packets ------------------------------------------------------------------
(defn- read-packet-keepalive [conn]
  nil)

(defn- read-packet-handshake [conn]
  (assoc {}
    :hash (-read-string-ucs2 conn)))

(defn- read-packet-login [conn]
  (assoc {}
    :eid (-read-int conn)
    :unknown (-read-string-ucs2 conn)
    :seed (-read-long conn)
    :dimension (-read-byte conn)))

(defn- read-packet-chat [conn]
  (assoc {}
    :message (-read-string-ucs2 conn)))

(defn- read-packet-timeupdate [conn]
  (assoc {}
    :time (-read-long conn)))

(defn- read-packet-equipment [conn]
  (assoc {}
    :eid (-read-int conn)
    :slot (-read-short conn)
    :itemid (-read-short conn)
    :unknown (-read-short conn)))

(defn- read-packet-spawnposition [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)))

(defn- read-packet-useentity [conn]
  (assoc {}
    :user (-read-int conn)
    :target (-read-int conn)
    :leftclick (-read-bool conn)))

(defn- read-packet-updatehealth [conn]
  (assoc {}
    :health (-read-short conn)))

(defn- read-packet-respawn [conn]
  (assoc {}
    :world (-read-byte conn)))

(defn- read-packet-playerpositionlook [conn]
  (assoc {}
    :x (-read-double conn)
    :stance (-read-double conn)
    :y (-read-double conn)
    :z (-read-double conn)
    :yaw (-read-float conn)
    :pitch (-read-float conn)
    :onground (-read-bool conn)))

(defn- read-packet-playerdigging [conn]
  (assoc {}
    :status (-read-byte conn)
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :face (-read-byte conn)))

(defn- read-packet-playerblockplacement [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :direction (-read-byte conn)
    :id (-read-short conn)
    :amount (-read-byte conn)
    :damage (-read-short conn)))

(defn- read-packet-holdingchange [conn]
  (assoc {}
    :slot (-read-short conn)))

(defn- read-packet-usebed [conn]
  (assoc {}
    :eid (-read-int conn)
    :inbed (-read-byte conn)
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)))

(defn- read-packet-animate [conn]
  (assoc {}
    :eid (-read-int conn)
    :animate (-read-byte conn)))

(defn- read-packet-entityaction [conn]
  (assoc {}
    :eid (-read-int conn)
    :action (-read-byte conn)))

(defn- read-packet-namedentityspawn [conn]
  (assoc {}
    :eid (-read-int conn)
    :playername (-read-string-ucs2 conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :rotation (-read-byte conn)
    :pitch (-read-byte conn)
    :currentitem (-read-short conn)))

(defn- read-packet-pickupspawn [conn]
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

(defn- read-packet-collectitem [conn]
  (assoc {}
    :collectedeid (-read-int conn)
    :collectoreid (-read-int conn)))

(defn- read-packet-addobjectvehicle [conn]
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

(defn- read-packet-mobspawn [conn]
  (assoc {}
    :eid (-read-int conn)
    :type (-read-byte conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)
    :datastream (-read-metadata conn)))

(defn- read-packet-entitypainting [conn]
  (assoc {}
    :eid (-read-int conn)
    :type (-read-string-ucs2 conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :direction (-read-int conn)))

(defn- read-packet-stanceupdate [conn]
  (assoc {}
    :unknown1 (-read-float conn)
    :unknown2 (-read-float conn)
    :unknown3 (-read-bool conn)
    :unknown4 (-read-bool conn)
    :unknown5 (-read-float conn)
    :unknown6 (-read-float conn)))

(defn- read-packet-entityvelocity [conn]
  (assoc {}
    :eid (-read-int conn)
    :velocityx (-read-short conn)
    :velocityy (-read-short conn)
    :velocityz (-read-short conn)))

(defn- read-packet-entitydestroy [conn]
  (assoc {}
    :eid (-read-int conn)))

(defn- read-packet-entity [conn]
  (assoc {}
    :eid (-read-int conn)))

(defn- read-packet-entityrelativemove [conn]
  (assoc {}
    :eid (-read-int conn)
    :dx (-read-byte conn)
    :dy (-read-byte conn)
    :dz (-read-byte conn)))

(defn- read-packet-entitylook [conn]
  (assoc {}
    :eid (-read-int conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)))

(defn- read-packet-entitylookandrelativemove [conn]
  (assoc {}
    :eid (-read-int conn)
    :dx (-read-byte conn)
    :dy (-read-byte conn)
    :dz (-read-byte conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)))

(defn- read-packet-entityteleport [conn]
  (assoc {}
    :eid (-read-int conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)
    :yaw (-read-byte conn)
    :pitch (-read-byte conn)))

(defn- read-packet-entitystatus [conn]
  (assoc {}
    :eid (-read-int conn)
    :entitystatus (-read-byte conn)))

(defn- read-packet-attachentity [conn]
  (assoc {}
    :eid (-read-int conn)
    :vehicleid (-read-int conn)))

(defn- read-packet-entitymetadata [conn]
  (assoc {}
    :eid (-read-int conn)
    :metadata (-read-metadata conn)))

(defn- read-packet-prechunk [conn]
  (assoc {}
    :x (-read-int conn)
    :z (-read-int conn)
    :mode (-read-bool conn)))

(defn- read-packet-mapchunk [conn]
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

(defn- read-packet-multiblockchange [conn]
  (assoc {}
    :chunkx (-read-int conn)
    :chunkz (-read-int conn)
    :arraysize (-read-short conn)
    :coordinatearray (-read-shortarray conn)
    :typearray (-read-bytearray conn)
    :metadataarray (-read-bytearray conn)))

(defn- read-packet-blockchange [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :blocktype (-read-byte conn)
    :blockmetadata (-read-byte conn)))

(defn- read-packet-playnoteblock [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-short conn)
    :z (-read-int conn)
    :instrumenttype (-read-byte conn)
    :pitch (-read-byte conn)))

(defn- read-packet-explosion [conn]
  (let [prerecords (assoc {}
                          :x (-read-int conn)
                          :y (-read-short conn)
                          :z (-read-int conn)
                          :unknownradius (-read-byte conn)
                          :recordcount (-read-byte conn))]
    (assoc prerecords
           :records (-read-bytearray conn
                                     (* 3 (:recordcount prerecords))))))

(defn- read-packet-soundeffect [conn]
  (assoc {}
    :effectid (-read-int conn)
    :x (-read-int conn)
    :y (-read-byte conn)
    :z (-read-int conn)
    :sounddata (-read-int conn)))

(defn- read-packet-newinvalidstate [conn]
  (assoc {}
    :reason (-read-byte conn)))

(defn- read-packet-thunderbolt [conn]
  (assoc {}
    :eid (-read-int conn)
    :unknown (-read-bool conn)
    :x (-read-int conn)
    :y (-read-int conn)
    :z (-read-int conn)))

(defn- read-packet-openwindow [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :inventorytype (-read-byte conn)
    :windowtitle (-read-string-utf8 conn)
    :numberofslots (-read-byte conn)))

(defn- read-packet-closewindow [conn]
  (assoc {}
    :windowid (-read-byte conn)))

(defn- read-packet-setslot [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :slot (-read-short conn)
    :itemid (-read-short conn)
    :itemcount (-read-byte conn)
    :itemuses (-read-short conn)))

(defn- read-packet-windowitems [conn]
  (letfn [(-read-payload-item []
             (let [payload (assoc {} :itemid (-read-short conn))]
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

(defn- read-packet-updateprogressbar [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :progressbar (-read-short conn)
    :value (-read-short conn)))

(defn- read-packet-transaction [conn]
  (assoc {}
    :windowid (-read-byte conn)
    :actionnumber (-read-short conn)
    :accepted (-read-short conn)))

(defn- read-packet-updatesign [conn]
  (assoc {}
    :x (-read-int conn)
    :y (-read-short conn)
    :z (-read-int conn)
    :text1 (-read-string-ucs2 conn)
    :text2 (-read-string-ucs2 conn)
    :text3 (-read-string-ucs2 conn)
    :text4 (-read-string-ucs2 conn)))

(defn- read-packet-mapdata [conn]
  (let [pretext (assoc {}
                       :unknown1 (-read-int conn)
                       :unknown2 (-read-short conn)
                       :textlength (-read-int conn))]
    (assoc pretext :text (-read-bytearray (:textlength pretext)))))

(defn- read-packet-incrementstatistic [conn]
  (assoc {}
    :statisticid (-read-int conn)
    :amount (-read-byte conn)))

(defn- read-packet-disconnectkick [conn]
  (assoc {}
    :reason (-read-string-ucs2 conn)))


; Reading Wrappers -----------------------------------------------------------------
(defn read-packet [conn]
  (let [packet-id (int (-read-byte conn))
        packet-type (packet-types packet-id)]
    (println "\n--PACKET--> " packet-type)
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
        :mapdata                   (read-packet-mapdata conn)
        :incrementstatistic        (read-packet-incrementstatistic conn)
        :disconnectkick            (read-packet-disconnectkick conn)

        :else (str "UNKNOWN PACKET TYPE: " packet-id)
        ))
    (println "\n\n\n")))

