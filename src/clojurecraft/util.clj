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

(defn replace-slice [v start items]
  (if (empty? items)
    v
    (recur (assoc v start (first items))
           (+ 1 start)
           (rest items))))
