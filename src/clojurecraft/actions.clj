(ns clojurecraft.actions
  (:use [clojurecraft.util])
  (:use [clojurecraft.out])
  (:require [clojurecraft.physics :as physics]))

(defn handle-action-group [bot action-group]
  (let [queue-action #(.put (:actionqueue bot) %)
        queue-action-group #(dorun (map queue-action %))]
    (queue-action-group action-group)))


; Performers -----------------------------------------------------------------------
(defn- move! [bot x-change y-change z-change]
  (let [player (:player bot)]
    (when (:loc @player)
      (dosync
        (let [location (:loc @player)
              new-location (merge location
                                  {:x (+ x-change (:x location))
                                   :y (+ y-change (:y location))
                                   :z (+ z-change (:z location))
                                   :stance (+ y-change (:stance location))})]
          (alter player merge {:loc new-location}))))))

(defn- jump! [bot]
  (let [player (:player bot)]
    (when (:loc @player)
      (dosync
        (let [location (:loc @player)]
          (alter player assoc-in [:loc :onground] false)
          (alter player assoc :velocity physics/JUMP-VELOCITY))))))

(defn- chat! [bot message]
  (write-packet bot :chat {:message message}))

(defn- turn-to! [bot yaw]
  (let [player (:player bot)]
    (dosync
      (let [location (:loc @player)
            new-location (assoc location :yaw yaw)]
        (alter player assoc :loc new-location)))))

(defn- look-to! [bot pitch]
  (let [player (:player bot)]
    (dosync
      (let [location (:loc @player)
            new-location (assoc location :pitch pitch)]
        (alter player assoc :loc new-location)))))

(defn- respawn! [bot]
  (write-packet bot :respawn {:world 0})) ; Always respawn in the normal (non-Nether) world for now.


(defn perform! [action]
  (let [performer ({:move move!
                    :jump jump!
                    :chat chat!
                    :turn-to turn-to!
                    :look-to look-to!
                    :respawn respawn!} (:action action))]
    (println "PERFORMING" (:action action) (:args action))
    (apply performer (:bot action) (:args action))))


; Public Actions -------------------------------------------------------------------
(defn move [bot x-change y-change z-change]
  {:bot bot :action :move :args [x-change y-change z-change]})

(defn jump [bot]
  {:bot bot :action :jump :args []})

(defn chat [bot message]
  {:bot bot :action :chat :args [message]})


(defn turn-to [bot yaw]
  {:bot bot :action :turn-to :args [yaw]})

(defn turn-north [bot]
  {:bot bot :action :turn-to :args [90.0]})

(defn turn-south [bot]
  {:bot bot :action :turn-to :args [270.0]})

(defn turn-east [bot]
  {:bot bot :action :turn-to :args [180.0]})

(defn turn-west [bot]
  {:bot bot :action :turn-to :args [0.0]})


(defn look-to [bot pitch]
  {:bot bot :action :look-to :args [pitch]})

(defn look-up [bot]
  {:bot bot :action :look-to :args [-90.0]})

(defn look-down [bot]
  {:bot bot :action :look-to :args [-90.0]})

(defn look-straight [bot]
  {:bot bot :action :look-to :args [-0.0]})


(defn respawn [bot]
  {:bot bot :action :respawn :args []})

