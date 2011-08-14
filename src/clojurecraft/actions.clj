(ns clojurecraft.actions
  (:use [clojurecraft.util])
  (:use [clojurecraft.out])
  (:require [clojurecraft.physics :as physics]))

(defn move [bot x-change y-change z-change]
  (delay
    (let [player (:player bot)]
      (dosync
        (let [location (:loc @player)
              new-location (merge location
                                  {:x (+ x-change (:x location))
                                   :y (+ y-change (:y location))
                                   :z (+ z-change (:z location))
                                   :stance (+ y-change (:stance location))})]
          (alter player merge {:loc new-location}))))))

(defn jump [bot]
  (delay
    (let [player (:player bot)]
      (dosync
        (let [location (:loc @player)]
          (alter player assoc-in [:loc :onground] false)
          (alter player assoc :velocity physics/JUMP-VELOCITY))))))

(defn chat [bot message]
  (delay
    (write-packet bot :chat {:message message})))


(defn turn-to [bot yaw]
  (delay
    (let [player (:player bot)]
      (dosync
        (let [location (:loc @player)
              new-location (assoc location :yaw yaw)]
          (alter player assoc :loc new-location))))))

(defn turn-north [bot]
  (turn-to bot 90.0))

(defn turn-south [bot]
  (turn-to bot 270.0))

(defn turn-east [bot]
  (turn-to bot 180.0))

(defn turn-west [bot]
  (turn-to bot 0.0))


(defn look-to [bot pitch]
  (delay
    (let [player (:player bot)]
      (dosync
        (let [location (:loc @player)
              new-location (assoc location :pitch pitch)]
          (alter player assoc :loc new-location))))))

(defn look-up [bot]
  (look-to bot -90.0))

(defn look-down[bot]
  (look-to bot 90.0))

(defn look-straight [bot]
  (look-to bot 0.0))


(defn respawn [bot]
  (delay
    (write-packet bot :respawn {:world 0}))) ; Always respawn in the normal (non-Nether) world for now.
