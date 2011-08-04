(ns clojurecraft.chunks
  (:use [clojurecraft.mappings])
  (:use [clojurecraft.util])
  (:require [clojurecraft.data])
  (:import [clojurecraft.data Block]))

(defn coords-of-chunk-containing [x z]
  [(bit-shift-right x 4)
   (bit-shift-right z 4)])

(defn block-index-in-chunk [x y z]
  [(bit-and x 15)
   (bit-and y 127)
   (bit-and z 15)])

(defn block-from-chunk [x y z chunk]
  (let [[ix iy iz] (block-index-in-chunk x y z)
        i (+ y (* z 128) (* x 128 16))
        block-type (get (:types chunk) i)
        block-meta (get (:metadata chunk) i)
        block-light (get (:light chunk) i)
        block-sky-light (get (:sky-light chunk) i)]
    (Block. [x y z]
            (block-types (int block-type))
            block-meta
            block-light
            block-sky-light)))

(defn chunk-containing [x z chunks]
  (chunks (coords-of-chunk-containing x z)))

(defn -block [x y z chunks]
  (block-from-chunk x y z (chunk-containing x z chunks)))

(defn block [bot x y z]
  (-block x y z (:chunks (:world bot))))

(defn block-rel [bot x y z]
  (block bot
         (int (+ (:x (:loc @(:player bot))) x))
         (int (+ (:y (:loc @(:player bot))) y))
         (int (+ (:z (:loc @(:player bot))) z))))

(defn block-standing [bot]
  (block bot
         (int (:x (:loc @(:player bot))))
         (int (- (:y (:loc @(:player bot))) 0))
         (int (:z (:loc @(:player bot))))))

(defn current [bot]
  (chunk-containing
         (int (:x (:loc @(:player bot))))
         (int (:z (:loc @(:player bot))))
         @(:chunks (:world bot))))
