Basics
======

When you connect to a server you get a `Bot` object back.  You can connect with
`(clojurecraft.core/connect {:name "hostname" :port INT})`.

Once you've got a bot you can query it for data about its world and tell it to
perform actions.

Data Structures
===============

World data is shared between all the bots you create in a single process.  This helps
keep memory usage down by not storing duplicate copies of chunk and entity
information for each bot`in the same world.

Worlds
------

`World` objects have several pieces of data.

Please read the Transactions section to learn why the data is structured the way it
is.

`(:time world)` is a ref containing the current world time.

`(:entities world)` is a ref containing a map of entity IDs to `Entity` refs.

`(:chunks world)` is a ref containing a map of chunk coordinates ([x y z] vectors)
to `Entity` refs.

Locations
---------

`Location` objects represent the locations of entities in the world.  They have the
following pieces of data:

* `(:x location)`
* `(:y location)`
* `(:z location)`
* `(:yaw location)`
* `(:pitch location)`
* `(:stance location)`
* `(:onground location)`

Entities
--------

`Entity` objects represent a single entity in the world.  One of these is your bot's
player.

`(:eid entity)` is the ID of the entity.

`(:loc entity)` is a `Location` object representing the location of the entity in the
world.

`(:despawned entity)` is a boolean that indicates whether the entity has despawned.
You should never need to read this, but plase read the Transactions section for the
reason why it's included.

Chunks
------

Blocks
------

Bots
----

`Bot` objects are your gateway to observing and interacting with the world.

`(:world bot)` is a `World` object representing the bot's world.

`(:player bot)` is a ref containing the `Entity` representing the bot.  This is just
a shortcut so you don't have to pull it out of the `:entities` map in the bot's world
all the time.

Transactions
============

There are two main types of data you'll want to observe with your bots: chunks and
entities.  A `World` object contains two maps: one of entities and one of chunks.

Each of these maps is a ref, and each of the entries in each map is also a ref.  This
may seem excessive -- why not simply make each map a ref *or* each entry a ref?

### Top-Level Refs

The maps themselves clearly need to be refs so that multiple bots sharing the same
world can update them safely.

### Entry Refs

To understand the reason for making each entity a ref consider a bot with the
following goal:

"Find all the hostile mobs.  Pick the one with the lowest health and attack it."

Now imagine that during the process of picking a mob to kill the bot received an
update about one of the peaceful entities.

If the entries of the map were not themselves refs the bot would *have* to
synchronize on the entire map.  This peaceful entity update would cause a retry of
the transaction even though the bot doesn't care about peaceful entities at all!

Making each entity its own ref means we can do the following:

* Inside of a dosync:
  * Find all the hostile mobs.
  * `ensure` on all of them.
  * Perform our calculations.

This lets us ignore updates to peaceful mobs, but retry when a hostile mob is updated
(perhaps someone else has killed one).  Keeping the "find mobs" step inside the
dosync ensures that if a mob despawns we will be looking at an accurate list the next
time we retry.

Note that if a new hostile mob is spawned it will not cause a retry.  If you are
performing an action that needs perfectly accurate data you can always synchronize
on the maps themselves, but be aware that this will probably not be very performant.

### Entity Despawns

This also reveals the reason for the `:despawn` entry in an `Entity` object: if we
simply removed the entity from the map when it despawned any transactions depending
on that entity wouldn't be restarted.

Actions
=======

Actions are functions that take a `Bot` object and some arguments and handle writing
the packets to make the bot perform the action.

### move

`(clojurecraft.actions/move bot x y z)`

The `move` action adjusts the location of the bot.  This lets it move around the
world.

At the moment it does not do any error checking, so moving too far at once or moving
into a block will simply be rejected by the server without any warning.

This action is fairly low level.  Expect to see some fun path-finding
algorithms/libraries in the future that will remove the need to call this directly.
