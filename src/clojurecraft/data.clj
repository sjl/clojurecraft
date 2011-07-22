(ns clojurecraft.data)

; Location
;
; The location of an entity in the world.
;
; x        -> double
; y        -> double
; z        -> double
; pitch    -> double
; yaw      -> double
; stance   -> double
; onground -> boolean
(defrecord Location [x y z pitch yaw stance onground])

; Entity
;
; A single entity in the world.
;
; eid -> integer
; loc -> Location
;
; despawned -> boolean
;   True if the entity has been despawned.
;
;   This exists to notify transactions coordinating on this ref that
;   something has changed, since otherwise there would be no way to tell
;   (despawning would simply remove the object from the entity list
;   without modifying it).
(defrecord Entity [eid loc despawned])

; Chunk
;
; A single chunk in the world.
;
; blocks -> [block ...]
;   A vector of the blocks in this chunk.
(defrecord Chunk [blocks])

; Block
;
; A representation of a single block.
; This may be removed in the future if the overhead is too great.
;
; loc  -> [x y z]
;   A simple vector of three coordinates, NOT a Location (for performance).
;
; kind -> TODO
(defrecord Block [loc kind])

; Bot
;
; A single bot, connected to a server.
;
; connection -> (ref {:in DataInputStream :out :DataOutputStream})
;   The input and output streams.
;
;   Don't ever touch this -- the writing thread will handle it.
;
; outqueue -> LinkedBlockingQueue
;   A queue of packets to write, so we can coordinate the writes
;   to avoid mixing packets together.
;
;   Don't ever touch this.  Use out/-write-packet-* instead.
;
; player -> (ref Entity)
;   A ref to the Entity representing the bot's player in the world.
;
; world -> World
;   The world the bot is connected to.
;
;   NOT a ref.  Coordinating the entire world would be too much of a performance
;   hit.  Instead the individual pieces of the world are refs.
;
;   Worlds themselves should never need to be updated after creation -- instead the
;   various refs inside them are updated.
;
; packet-counts-in  -> integer
; packet-counts-out -> integer
(defrecord Bot [connection outqueue player world
                packet-counts-in packet-counts-out])

; World
;
; A representation of a single world/server, shared by all bots connected to it.
;
; server -> {:name hostname :port port}
;
; entities -> (ref {eid (ref Entity) ...})
;   A map of all the entities in the world.
;
; chunks -> (ref {[x y z] [(ref Chunk) ...] ...})
;   A map of all the chunks in the world.
;
; time -> (ref integer)
;   The current world time.
(defrecord World [server entities chunks time])

