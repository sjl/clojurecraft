Data Structures
===============

World data is shared between all the bots you create in a single process.  This helps
keep memory usage down by not storing duplicate copies of chunk and entity
information for each bot in the same world.

Worlds
------

``World`` objects have several pieces of data.  Please read the Transactions section
to learn why the data is structured the way it is.

``(:time world)`` is a ref containing the current world time.

``(:entities world)`` is a ref containing a map of entity IDs to ``Entity`` refs.

``(:chunks world)`` is a ref containing a map of chunk coordinates ([x y z] vectors)
to ``Chunk`` refs.

Locations
---------

``Location`` objects represent the locations of entities in the world.  They have the
following pieces of data:

* ``(:x location)``
* ``(:y location)``
* ``(:z location)``
* ``(:yaw location)``
* ``(:pitch location)``
* ``(:stance location)``
* ``(:onground location)``

Entities
--------

``Entity`` objects represent a single entity in the world.  One of these is your
bot's player.

``(:eid entity)`` is the ID of the entity.

``(:loc entity)`` is a ``Location`` object representing the location of the entity in
the world.

``(:despawned entity)`` is a boolean that indicates whether the entity has despawned.
You should never need to read this, but please read the Transactions section for the
reason why it's included.

``(:velocity entity)`` is the y velocity of the entity.  Only exists for bots, and
you should never need to touch it.

Chunks
------

A chunk has four arrays representing the data for blocks in the chunk.  You shouldn't
need to access chunk data directly -- there are helper functions in
``clojurecraft.chunks`` that will look up block objects for you.

Blocks
------

Bots
----

``Bot`` objects are your gateway to observing and interacting with the world.

``(:world bot)`` is a ``World`` object representing the bot's world.

``(:player bot)`` is a ref containing the ``Entity`` representing the bot.  This is
just a shortcut so you don't have to pull it out of the ``:entities`` map in the
bot's world all the time.

