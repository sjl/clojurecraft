(ns clojurecraft.util)

(defmacro l [& body]
  `(let [result# (~@body)]
     (println result#)
     result#))
