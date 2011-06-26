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
(defn- read-packet-keepalive [bot]
  {})

(defn- read-packet-handshake [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :hash (-read-string-ucs2 conn))]
    payload))

(defn- read-packet-login [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                      :eid (-read-int conn)
                      :unknown (-read-string-ucs2 conn)
                      :seed (-read-long conn)
                      :dimension (-read-byte conn))]
    payload))

(defn- read-packet-chat [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :message (-read-string-ucs2 conn))]
    payload))

(defn- read-packet-timeupdate [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :time (-read-long conn))]
    payload))

(defn- read-packet-equipment [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :slot (-read-short conn)
                       :itemid (-read-short conn)
                       :unknown (-read-short conn))]
    payload))

(defn- read-packet-spawnposition [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn))]
    payload))

(defn- read-packet-useentity [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :user (-read-int conn)
                       :target (-read-int conn)
                       :leftclick (-read-bool conn))]
    payload))

(defn- read-packet-updatehealth [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :health (-read-short conn))]
    payload))

(defn- read-packet-respawn [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :world (-read-byte conn))]
    payload))

(defn- read-packet-playerpositionlook [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :x (-read-double conn)
                       :stance (-read-double conn)
                       :y (-read-double conn)
                       :z (-read-double conn)
                       :yaw (-read-float conn)
                       :pitch (-read-float conn)
                       :onground (-read-bool conn))]
    (dosync (alter (:player bot) merge {:location payload}))
    payload))

(defn- read-packet-playerdigging [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :status (-read-byte conn)
                       :x (-read-int conn)
                       :y (-read-byte conn)
                       :z (-read-int conn)
                       :face (-read-byte conn))]
    payload))

(defn- read-packet-playerblockplacement [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :x (-read-int conn)
                       :y (-read-byte conn)
                       :z (-read-int conn)
                       :direction (-read-byte conn)
                       :id (-read-short conn)
                       :amount (-read-byte conn)
                       :damage (-read-short conn))]
    payload))

(defn- read-packet-holdingchange [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :slot (-read-short conn))]
    payload))

(defn- read-packet-usebed [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :inbed (-read-byte conn)
                       :x (-read-int conn)
                       :y (-read-byte conn)
                       :z (-read-int conn))]
    payload))

(defn- read-packet-animate [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :animate (-read-byte conn))]
    payload))

(defn- read-packet-entityaction [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :action (-read-byte conn))]
    payload))

(defn- read-packet-namedentityspawn [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :playername (-read-string-ucs2 conn)
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn)
                       :rotation (-read-byte conn)
                       :pitch (-read-byte conn)
                       :currentitem (-read-short conn))]
    payload))

(defn- read-packet-pickupspawn [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :item (-read-short conn)
                       :count (-read-byte conn)
                       :damagedata (-read-short conn)
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn)
                       :rotation (-read-byte conn)
                       :pitch (-read-byte conn)
                       :roll (-read-byte conn))]
    payload))

(defn- read-packet-collectitem [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :collectedeid (-read-int conn)
                       :collectoreid (-read-int conn))]
    payload))

(defn- read-packet-addobjectvehicle [bot]
  (let [conn (:connection bot)
        payload (let [basepacket (assoc {}
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
                           :unknownz (-read-int conn))))]
    payload))

(defn- read-packet-mobspawn [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :type (-read-byte conn)
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn)
                       :yaw (-read-byte conn)
                       :pitch (-read-byte conn)
                       :datastream (-read-metadata conn))]
    payload))

(defn- read-packet-entitypainting [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :type (-read-string-ucs2 conn)
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn)
                       :direction (-read-int conn))]
    payload))

(defn- read-packet-stanceupdate [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :unknown1 (-read-float conn)
                       :unknown2 (-read-float conn)
                       :unknown3 (-read-bool conn)
                       :unknown4 (-read-bool conn)
                       :unknown5 (-read-float conn)
                       :unknown6 (-read-float conn))]
    payload))

(defn- read-packet-entityvelocity [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :velocityx (-read-short conn)
                       :velocityy (-read-short conn)
                       :velocityz (-read-short conn))]
    payload))

(defn- read-packet-entitydestroy [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn))]
    payload))

(defn- read-packet-entity [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn))]
    payload))

(defn- read-packet-entityrelativemove [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :dx (-read-byte conn)
                       :dy (-read-byte conn)
                       :dz (-read-byte conn))]
    payload))

(defn- read-packet-entitylook [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :yaw (-read-byte conn)
                       :pitch (-read-byte conn))]
    payload))

(defn- read-packet-entitylookandrelativemove [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :dx (-read-byte conn)
                       :dy (-read-byte conn)
                       :dz (-read-byte conn)
                       :yaw (-read-byte conn)
                       :pitch (-read-byte conn))]
    payload))

(defn- read-packet-entityteleport [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn)
                       :yaw (-read-byte conn)
                       :pitch (-read-byte conn))]
    payload))

(defn- read-packet-entitystatus [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :entitystatus (-read-byte conn))]
    payload))

(defn- read-packet-attachentity [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :vehicleid (-read-int conn))]
    payload))

(defn- read-packet-entitymetadata [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :metadata (-read-metadata conn))]
    payload))

(defn- read-packet-prechunk [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :x (-read-int conn)
                       :z (-read-int conn)
                       :mode (-read-bool conn))]
    payload))

(defn- read-packet-mapchunk [bot]
  (let [conn (:connection bot)
        payload (let [predata (assoc {}
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
                                          (:compressedsize predata))))]
    payload))

(defn- read-packet-multiblockchange [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :chunkx (-read-int conn)
                       :chunkz (-read-int conn)
                       :arraysize (-read-short conn)
                       :coordinatearray (-read-shortarray conn arraysize)
                       :typearray (-read-bytearray conn arraysize)
                       :metadataarray (-read-bytearray conn arraysize))]
    payload))

(defn- read-packet-blockchange [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :x (-read-int conn)
                       :y (-read-byte conn)
                       :z (-read-int conn)
                       :blocktype (-read-byte conn)
                       :blockmetadata (-read-byte conn))]
    payload))

(defn- read-packet-playnoteblock [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :x (-read-int conn)
                       :y (-read-short conn)
                       :z (-read-int conn)
                       :instrumenttype (-read-byte conn)
                       :pitch (-read-byte conn))]
    payload))

(defn- read-packet-explosion [bot]
  (let [conn (:connection bot)
        payload (let [prerecords (assoc {}
                                        :x (-read-int conn)
                                        :y (-read-short conn)
                                        :z (-read-int conn)
                                        :unknownradius (-read-byte conn)
                                        :recordcount (-read-byte conn))]
                  (assoc prerecords
                         :records (-read-bytearray conn
                                                   (* 3 (:recordcount prerecords)))))]
    payload))

(defn- read-packet-soundeffect [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :effectid (-read-int conn)
                       :x (-read-int conn)
                       :y (-read-byte conn)
                       :z (-read-int conn)
                       :sounddata (-read-int conn))]
    payload))

(defn- read-packet-newinvalidstate [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :reason (-read-byte conn))]
    payload))

(defn- read-packet-thunderbolt [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :eid (-read-int conn)
                       :unknown (-read-bool conn)
                       :x (-read-int conn)
                       :y (-read-int conn)
                       :z (-read-int conn))]
    payload))

(defn- read-packet-openwindow [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :windowid (-read-byte conn)
                       :inventorytype (-read-byte conn)
                       :windowtitle (-read-string-utf8 conn)
                       :numberofslots (-read-byte conn))]
    payload))

(defn- read-packet-closewindow [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :windowid (-read-byte conn))]
    payload))

(defn- read-packet-setslot [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :windowid (-read-byte conn)
                       :slot (-read-short conn)
                       :itemid (-read-short conn)
                       :itemcount (-read-byte conn)
                       :itemuses (-read-short conn))]
    payload))

(defn- read-packet-windowitems [bot]
  (let [conn (:connection bot)
        payload (letfn [(-read-payload-item []
                           (let [payload (assoc {} :itemid (-read-short conn))]
                              (if (= (:itemid payload) -1)
                                payload
                                (assoc payload
                                       :count (-read-byte conn)
                                       :uses (-read-short conn)))))]
                  (let [prepay (assoc {}
                                      :windowid (-read-byte conn)
                                      :count (-read-short conn))
                               pay (repeatedly (:count prepay) -read-payload-item)]
                    (assoc prepay :payload pay)))]
    payload))

(defn- read-packet-updateprogressbar [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :windowid (-read-byte conn)
                       :progressbar (-read-short conn)
                       :value (-read-short conn))]
    payload))

(defn- read-packet-transaction [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :windowid (-read-byte conn)
                       :actionnumber (-read-short conn)
                       :accepted (-read-short conn))]
    payload))

(defn- read-packet-updatesign [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :x (-read-int conn)
                       :y (-read-short conn)
                       :z (-read-int conn)
                       :text1 (-read-string-ucs2 conn)
                       :text2 (-read-string-ucs2 conn)
                       :text3 (-read-string-ucs2 conn)
                       :text4 (-read-string-ucs2 conn))]
    payload))

(defn- read-packet-mapdata [bot]
  (let [conn (:connection bot)
        payload (let [pretext (assoc {}
                                     :unknown1 (-read-int conn)
                                     :unknown2 (-read-short conn)
                                     :textlength (-read-int conn))]
                  (assoc pretext :text (-read-bytearray (:textlength pretext))))]
    payload))

(defn- read-packet-incrementstatistic [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :statisticid (-read-int conn)
                       :amount (-read-byte conn))]
    payload))

(defn- read-packet-disconnectkick [bot]
  (let [conn (:connection bot)
        payload (assoc {}
                       :reason (-read-string-ucs2 conn))]
    payload))


; Reading Wrappers -----------------------------------------------------------------
(defn read-packet [bot]
  (let [conn (:connection bot)
        packet-id (int (-read-byte conn))
        packet-type (packet-types packet-id)]
    (if (= nil packet-type)
      (do
        (println (str "UNKNOWN PACKET TYPE: " (Integer/toHexString packet-id)))
        (/ 1 0)) 
      (do
        (identity (str "\n--PACKET--> " packet-type))
        (identity
          (case packet-type
            :keepalive                 (read-packet-keepalive bot)
            :handshake                 (read-packet-handshake bot)
            :login                     (read-packet-login bot)
            :chat                      (read-packet-chat bot)
            :timeupdate                (read-packet-timeupdate bot)
            :equipment                 (read-packet-equipment bot)
            :spawnposition             (read-packet-spawnposition bot)
            :useentity                 (read-packet-useentity bot)
            :updatehealth              (read-packet-updatehealth bot)
            :respawn                   (read-packet-respawn bot)
            :playerpositionlook        (read-packet-playerpositionlook bot)
            :playerdigging             (read-packet-playerdigging bot)
            :playerblockplacement      (read-packet-playerblockplacement bot)
            :holdingchange             (read-packet-holdingchange bot)
            :usebed                    (read-packet-usebed bot)
            :animate                   (read-packet-animate bot)
            :entityaction              (read-packet-entityaction bot)
            :namedentityspawn          (read-packet-namedentityspawn bot)
            :pickupspawn               (read-packet-pickupspawn bot)
            :collectitem               (read-packet-collectitem bot)
            :addobjectvehicle          (read-packet-addobjectvehicle bot)
            :mobspawn                  (read-packet-mobspawn bot)
            :entitypainting            (read-packet-entitypainting bot)
            :stanceupdate              (read-packet-stanceupdate bot)
            :entityvelocity            (read-packet-entityvelocity bot)
            :entitydestroy             (read-packet-entitydestroy bot)
            :entity                    (read-packet-entity bot)
            :entityrelativemove        (read-packet-entityrelativemove bot)
            :entitylook                (read-packet-entitylook bot)
            :entitylookandrelativemove (read-packet-entitylookandrelativemove bot)
            :entityteleport            (read-packet-entityteleport bot)
            :entitystatus              (read-packet-entitystatus bot)
            :attachentity              (read-packet-attachentity bot)
            :entitymetadata            (read-packet-entitymetadata bot)
            :prechunk                  (read-packet-prechunk bot)
            :mapchunk                  (read-packet-mapchunk bot)
            :multiblockchange          (read-packet-multiblockchange bot)
            :blockchange               (read-packet-blockchange bot)
            :playnoteblock             (read-packet-playnoteblock bot)
            :explosion                 (read-packet-explosion bot)
            :soundeffect               (read-packet-soundeffect bot)
            :newinvalidstate           (read-packet-newinvalidstate bot)
            :thunderbolt               (read-packet-thunderbolt bot)
            :openwindow                (read-packet-openwindow bot)
            :closewindow               (read-packet-closewindow bot)
            :setslot                   (read-packet-setslot bot)
            :windowitems               (read-packet-windowitems bot)
            :updateprogressbar         (read-packet-updateprogressbar bot)
            :transaction               (read-packet-transaction bot)
            :updatesign                (read-packet-updatesign bot)
            :mapdata                   (read-packet-mapdata bot)
            :incrementstatistic        (read-packet-incrementstatistic bot)
            :disconnectkick            (read-packet-disconnectkick bot)))))
    (identity "\n\n\n")))

