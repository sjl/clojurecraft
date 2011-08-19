(ns clojurecraft.physics
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.util])
  (:use [clojure.contrib.combinatorics :only (cartesian-product)])
  (:require [clojurecraft.chunks :as chunks]))

; TODO: Investigate these.  I'm not convinced.
(def G -27.0) ; meters/second^2
(def TICK 50/1000) ; seconds
(def CHAR-HEIGHT-EYES 1.62) ; meters
(def CHAR-HEIGHT-TOP 1.74) ; meters
(def CHAR-HEIGHT-HALF (/ CHAR-HEIGHT-TOP 2)) ; meters
(def CHAR-RADIUS 0.32) ; meters
(def TERMINAL-VELOCITY 18.0) ; meters/second
(def MAX-HORIZONTAL-VELOCITY 4.0) ; meters/second
(def JUMP-VELOCITY 8.2) ; meters/second


(defn player-bounds [{x :x y :y z :z}]
  [(map floorint [(- x CHAR-RADIUS) y (- z CHAR-RADIUS)])
   (map floorint [(+ x CHAR-RADIUS) (+ y CHAR-HEIGHT-TOP) (+ z CHAR-RADIUS)])])


(defn bound-velocity-vertical [velocity]
  (if (> 0 velocity)
    (min velocity TERMINAL-VELOCITY)
    (max velocity (* -1 TERMINAL-VELOCITY))))

(defn bound-velocity-horizontal [velocity]
  (if (> 0 velocity)
    (min velocity MAX-HORIZONTAL-VELOCITY)
    (max velocity (* -1 MAX-HORIZONTAL-VELOCITY))))


(def is-solid (comp not non-solid-blocks :type))
(defn coords-are-solid [bot [x y z]]
  (is-solid (chunks/block bot x y z)))

(defn collision [bot [min-x min-y min-z] [max-x max-y max-z]]
  (let [block-coords (cartesian-product (range min-x (+ 1 max-x))
                                        (range min-y (+ 1 max-y))
                                        (range min-z (+ 1 max-z)))]
    (any? (map coords-are-solid (cycle [bot]) block-coords))))


(defn resolve-collision-y [y velocity]
  (+ y
     ; If we're traveling downwards, and there was a collison, we need our new
     ; y to be on top of the block beneath us (add 1 to y).
     ;
     ; If we're traveling upwards, and there was a collison, we need the top of our
     ; head to be just beneath the block above us (subtract our height from y).
     ;
     ; We also go just a bit further to make sure we're out.
     (* (if (< velocity 0)
          1
          (* -1 CHAR-HEIGHT-TOP))
        1.001)))


(defn update-loc-y [bot [min-x min-y min-z] [max-x max-y max-z]]
  (let [player (:player bot)
        old-y (:y (:loc @player))
        unbounded-vel (+ (:velocity @player)
                         (* G TICK))
        new-vel (bound-velocity-vertical unbounded-vel)
        tentative-new-y (+ old-y (* new-vel TICK))
        block-y (floorint (+ tentative-new-y
                             CHAR-HEIGHT-HALF
                             (* (sign new-vel) CHAR-HEIGHT-HALF)))]
    (if (collision bot [min-x block-y min-z] [max-x block-y max-z])
      (let [resolved-y (resolve-collision-y block-y new-vel)
            new-onground (if (< new-vel 0) true false)]
        {:y resolved-y :onground new-onground :vel 0})
      {:y tentative-new-y :onground false :vel new-vel})))

