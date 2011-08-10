(ns clojurecraft.util)

(defmacro l [& body]
  `(let [result# (~@body)]
     (println result#)
     result#))

(defmacro lc [& body]
  `(let [result# (~@body)]
     (println result#)
     (println (class result#))
     result#))

(defn invert [m]
  (apply assoc {} (mapcat reverse m)))

(defn replace-array-slice
  "Return a new byte-array with the given elements replaced."
  [old-arr start new-data]
  (let [len (alength new-data)
        new-arr (byte-array old-arr)]
    (dorun (map #(aset-byte new-arr (+ start %) (aget new-data %))
                (range len)))
    new-arr))

(defn replace-array-index
  "Return a new byte-array with the given byte replaced."
  [old-arr i b]
  (let [new-arr (byte-array old-arr)]
    (aset-byte new-arr i b)
    new-arr))

(defn sign [i]
  (if (> i 0) 1 -1))

(defn floorint [f]
  (int (Math/floor f)))

(defn any? [s]
  (not (empty? (filter identity s))))

; Bytes ----------------------------------------------------------------------------
(defn byte-seq [b]
  (loop [n 0 b b s []]
    (if (< n 8)
      (recur (inc n) (bit-shift-right b 1) (conj s (bit-and b 1)))
      (reverse s))))

(defn top [b]
  (byte (bit-shift-right (bit-and b 0xf0) 4)))

(defn bottom [b]
  (byte (bit-and b 0x0f)))

(defn to-unsigned [b]
  (bit-and b 0xff))


