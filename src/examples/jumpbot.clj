(ns examples.jumpbot
  (:require [clojurecraft.core :as core])
  (:require [clojurecraft.events :as events])
  (:require [clojurecraft.loops :as loops])
  (:require [clojurecraft.actions :as actions]))

(defn jump [bot]
  [(actions/jump bot)])

(defn handle-dead [bot]
  [(actions/chat bot "WHY DO YOU NOT WANT ME TO JUMP?!")
   (actions/respawn bot)])


(defn make-jumpbot [server username]
  (let [bot (core/connect server username)]
    (events/add-handler bot :dead #'handle-dead)
    (loops/add-loop bot #'jump 2000 :jump-loop)
    bot))

