(ns clojurecraft.events)

(defn add-handler [bot event-type handler]
  (dosync
    (let [current-handlers (event-type @(:event-handlers bot))
          updated-handlers (conj current-handlers handler)]
      (alter (:event-handlers bot) assoc event-type updated-handlers))))

(defn clear-handlers [bot event-type]
  (dosync (alter (:event-handlers bot) dissoc event-type)))


(defn- fire-handler [bot event-type & args]
  (dorun (map #(apply (eval %) (into [bot] args))
              (event-type @(:event-handlers bot)))))

(defn fire-chat [bot message]
  (fire-handler bot :chat message))

