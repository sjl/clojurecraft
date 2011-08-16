(ns examples.givebot
  (:require [clojure.contrib.string :as s])
  (:require [clojurecraft.core :as core])
  (:require [clojurecraft.events :as events])
  (:require [clojurecraft.actions :as actions]))

(def item-map {"tnt" "46", "lever" "69"})
(def WANT-RE #"^<(\w+)> i want (\d+)? ?(.+)?$")

(defn- get-item [matches]
  (get matches 3))

(defn- get-username [matches]
  (get matches 1))

(defn- get-number [matches]
  (or (get matches 2) "1"))

(defn- get-numbers
  "Return a sequence of numbers that add up to the desired number of objects.

  e.g.:

  1 -> [1]
  64 -> [64]
  128 -> [64 64]
  129 -> [64 64 1]"
  [matches]
  (loop [number (Integer. (get-number matches))
         numbers []]
    (if (<= number 0)
      numbers
      (recur (- number 64)
             (conj numbers (min 64 number))))))


(defn- give-string [username item number]
  (str "/give " username " " item " " number))

(defn- handle-chat [bot message]
  (let [matches (re-find WANT-RE (s/lower-case message))]
    (when matches
      (let [item (item-map (get-item matches))
            username (get-username matches)
            numbers (get-numbers matches)]
        (map #(actions/chat bot (give-string username item %)) numbers)))))

(defn handle-dead [bot]
  [(actions/chat bot "WHY?!")
   (actions/respawn bot)])


(defn make-givebot [server username]
  (let [bot (core/connect server username)]
    (events/add-handler bot :dead #'handle-dead)
    bot))


