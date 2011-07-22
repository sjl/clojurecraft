(ns clojurecraft.actions
  (:use [clojurecraft.util]))


(defn move [bot x-change y-change z-change]
  (let [player (:player bot)]
    (dosync
      (let [location (:loc @player)
            new-location (merge location
                                {:x (+ x-change (:x location))
                                 :y (+ y-change (:y location))
                                 :z (+ z-change (:z location))
                                 :stance (+ y-change (:stance location))})]
        (println (str "Moving from " location " to " new-location))
        (alter player merge {:loc new-location}))))
  bot)

