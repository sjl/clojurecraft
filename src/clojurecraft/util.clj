(ns clojurecraft.util)

(defmacro l [& body]
  `(let [result# (~@body)]
     (println result#)
     result#))

(defn invert [m]
  (apply assoc {} (mapcat reverse m)))
