(ns clojurecraft.chunks
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.util])
  (:require [clojurecraft.data])
  (:import [clojurecraft.data Block]))

(defn- get-chunk
  "Get a chunk, making sure it has been forced."
  [chunks coords]
  (let [possible-chunk (@chunks coords)]
    (when possible-chunk
      (force @possible-chunk)
      possible-chunk)))

(defn coords-of-chunk-containing [x z]
  [(bit-shift-right x 4)
   (bit-shift-right z 4)])

(defn block-index-in-chunk
  "Return the index of the block at given world coordinates in the chunk data arrays."
  [x y z]
  (let [ix (bit-and x 15)
        iy (bit-and y 127)
        iz (bit-and z 15)]
    (+ iy (* iz 128) (* ix 128 16))))

(defn block-from-chunk [x y z chunk]
  (let [i (block-index-in-chunk x y z)
        block-type (aget ^bytes (:types (force @chunk)) i)
        block-meta (aget ^bytes (:metadata (force @chunk)) i)
        block-light (aget ^bytes (:light (force @chunk)) i)
        block-sky-light (aget ^bytes (:sky-light (force @chunk)) i)]
    (Block. [x y z]
            (block-types (int block-type))
            block-meta
            block-light
            block-sky-light)))

(defn chunk-containing [x z chunks]
  (get-chunk chunks (coords-of-chunk-containing x z)))

(defn- -block [x y z chunks]
  (block-from-chunk x y z (chunk-containing x z chunks)))

(defn block [bot x y z]
  (-block x y z (:chunks (:world bot))))

(defn block-rel [bot x y z]
  (block bot
         (int (+ (:x (:loc @(:player bot))) x))
         (+ 1 (int (+ (:y (:loc @(:player bot))) y)))
         (int (+ (:z (:loc @(:player bot))) z))))

(defn block-beneath [bot]
  (block bot
         (int (Math/floor (:x (:loc @(:player bot)))))
         (int (Math/floor (:y (:loc @(:player bot)))))
         (int (Math/floor (:z (:loc @(:player bot)))))))

(defn current [bot]
  (chunk-containing
         (int (:x (:loc @(:player bot))))
         (int (:z (:loc @(:player bot))))
         (:chunks (:world bot))))
