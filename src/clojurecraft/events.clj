(ns clojurecraft.events
  (:use [clojurecraft.actions :only [handle-action-group]]))

; Event Handlers -------------------------------------------------------------------
(defn- fire-handler [bot event-type & args]
  (let [action-groups (map #(apply (deref %) (into [bot] args))
                           (event-type @(:event-handlers bot)))]
    (dorun (map handle-action-group (cycle [bot]) action-groups))))

(defn fire-chat [bot message]
  (fire-handler bot :chat message))

(defn fire-dead [bot]
  (fire-handler bot :dead))


(defn add-handler [bot event-type handler]
  (dosync
    (let [current-handlers (event-type @(:event-handlers bot))
          updated-handlers (conj current-handlers handler)]
      (alter (:event-handlers bot) assoc event-type updated-handlers))))

(defn clear-handlers [bot event-type]
  (dosync (alter (:event-handlers bot) dissoc event-type)))


; Loops ----------------------------------------------------------------------------
(defn- run-loop [bot function sleep-ms loop-id]
  (while (and (nil? (:exit @(:connection bot)))
              (@(:loops bot) loop-id))
    (handle-action-group bot ((deref function) bot))
    (Thread/sleep sleep-ms))
  (dosync (alter (:loops bot) dissoc loop-id)))


(defn remove-loop [bot loop-id]
  (dosync (alter (:loops bot) assoc loop-id nil)))

(defn add-loop [bot function sleep-ms loop-id]
  (dosync (alter (:loops bot) assoc loop-id :running))
  (.start (Thread. #(run-loop bot function sleep-ms loop-id))))
