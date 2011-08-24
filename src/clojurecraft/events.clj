(ns clojurecraft.events)

(defn add-handler [bot event-type handler]
  (dosync
    (let [current-handlers (event-type @(:event-handlers bot))
          updated-handlers (conj current-handlers handler)]
      (alter (:event-handlers bot) assoc event-type updated-handlers))))

(defn clear-handlers [bot event-type]
  (dosync (alter (:event-handlers bot) dissoc event-type)))


(defn- handle-action-group [bot action-group]
  (println action-group)
  (let [queue-action #(.put (:actionqueue bot) %)
        queue-action-group #(dorun (map queue-action %))]
    (queue-action-group action-group)))

(defn- fire-handler [bot event-type & args]
  (let [action-groups (map #(apply (deref %) (into [bot] args))
                           (event-type @(:event-handlers bot)))]
    (dorun (map handle-action-group (cycle [bot]) action-groups))))


(defn fire-chat [bot message]
  (fire-handler bot :chat message))

(defn fire-dead [bot]
  (fire-handler bot :dead))


(defn- run-loop [bot function sleep-ms]
  (while (nil? (:exit @(:connection bot)))
    (handle-action-group bot (function bot))
    (Thread/sleep sleep-ms)))

(defn add-loop [bot function sleep-ms]
  (.start (Thread. #(run-loop bot function sleep-ms))))
