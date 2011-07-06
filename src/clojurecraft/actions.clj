(ns clojurecraft.actions
  (:use [clojurecraft.util]))


(defn move [bot x-change y-change z-change]
  (let [player (:player bot)]
    (dosync
      (let [location (:location @player)
            new-location (merge location
                                {:x (+ x-change (:x location))
                                 :y (+ y-change (:y location))
                                 :z (+ z-change (:z location))})]
        (alter player merge {:location new-location}))))
  bot)

