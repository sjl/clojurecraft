(ns clojurecraft.units)

(defn to-pixels [n type]
  (case type
    :pixels [n :pixels]
    :meters [(* 32 n) :meters]))

(defn to-meters [n type]
  (case type
    :meters [n :meters]
    :pixels [(/ n 32) :pixels]))

