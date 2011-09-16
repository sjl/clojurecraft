(ns examples.followbot
  (:use [clojure.contrib.math :only (abs)])
  (:require [clojurecraft.core :as core])
  (:require [clojurecraft.events :as events])
  (:require [clojurecraft.loops :as loops])
  (:require [clojurecraft.actions :as actions]))

; Helper functions -----------------------------------------------------------------
(defn- locs [bot entity]
  [(:loc @(:player bot))
   (:loc @entity)])

(defn- distance-between [bot entity]
  (let [[from to] (locs bot entity)]
    (+ (abs (- (:x from) (:x to)))
       (abs (- (:y from) (:y to)))
       (abs (- (:z from) (:z to))))))

(defn- toward-single [from to]
  (if (<= (abs (- from to)) 2)
    0
    (if (< from to) 1 -1)))

(defn- toward [bot entity]
  (let [[from to] (locs bot entity)]
    {:x (toward-single (:x from) (:x to))
     :z (toward-single (:z from) (:z to))}))

(defn- find-other-players [bot]
  (remove #(= (:name @%)
              (:name @(:player bot)))
          (filter #(:name @%)
                  (vals @(:entities (:world bot))))))

(defn- closest-entity [bot entities]
  (when (seq entities)
    (first (sort #(< (distance-between bot %1)
                     (distance-between bot %2))
                 entities))))


; Loops ----------------------------------------------------------------------------
(defn follow [bot]
  (when (:loc @(:player bot))
    (let [players (find-other-players bot)
          closest (closest-entity bot players)]
      (when closest
        (let [{x :x z :z} (toward bot closest)]
          [(actions/move bot x 0 z)])))))


; Event handlers -------------------------------------------------------------------
(defn handle-dead [bot]
  [(actions/chat bot "WTF mate?")
   (actions/respawn bot)])


; Creation function ----------------------------------------------------------------
(defn make-followbot [server username]
  (let [bot (core/connect server username)]
    (events/add-handler bot :dead #'handle-dead)
    (loops/add-loop bot #'follow 200 :follow-loop)
    bot))

