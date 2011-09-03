(ns clojurecraft.loops
  (:use [clojurecraft.actions :only [handle-action-group]]))

; Loops ----------------------------------------------------------------------------
(defn- run-loop [bot function sleep-ms loop-id]
  (while (and (nil? (:exit @(:connection bot)))
              (@(:loops bot) loop-id))
    (handle-action-group bot ((deref function) bot))
    (Thread/sleep sleep-ms))
  (dosync (alter (:loops bot) dissoc loop-id))
  (println "done - loop -" loop-id)
  (println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))


(defn remove-loop [bot loop-id]
  (dosync (alter (:loops bot) assoc loop-id nil)))

(defn add-loop [bot function sleep-ms loop-id]
  (dosync (alter (:loops bot) assoc loop-id :running))
  (.start (Thread. #(run-loop bot function sleep-ms loop-id))))

