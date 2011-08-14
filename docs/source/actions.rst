Actions
=======

Actions are functions that take a ``Bot`` object and some arguments, and return
a delayed function that will handle writing the packets to your bot perform the
action.

If you want to make your bot perform an action immediately you should use ``force``
to make it happen::

    (force (clojurecraft.actions/jump bot))

Technically you don't need to use ``force``, because the REPL's printing will force
the result to be evaluated, but conceptually it's a good habit to get into.

chat
----

``(clojurecraft.actions/chat bot message)``

Tells the bot to send a chat message.

Note: you *will* get kicked from the server if your message is too long.  Here's the
formula for the vanilla Minecraft server::

    (defn too-long? [bot message]
      (> (+ 3 (count (:username bot)) (count message))
         100))

It's up to *you* to avoid sending messages that are too long.  This action doesn't
handle it because there are multiple options you might want:

  * Ignore messages that are too long.
  * Split them into multiple messages.

You might also be writing a bot for a modded server that allows longer messages.

jump
----

``(clojurecraft.actions/jump bot)``

Tells the bot to jump, if possible.

look-to
-------

``(clojurecraft.actions/look-to bot pitch)``

Changes the position of the bot's head.  ``pitch`` can be anywhere from ``-90`` to
``90``.

* ``-90``: looking straight up.
* ``0``: looking straight ahead.
* ``90``: looking straight down.

look-up
-------

``(clojurecraft.actions/look-up bot)``

Changes the position of the bot's head to look straight up.

Exactly equivalent to ``(clojurecraft.actions/look-to bot -90.0)``.

look-down
---------

``(clojurecraft.actions/look-down bot)``

Changes the position of the bot's head to look straight down.

Exactly equivalent to ``(clojurecraft.actions/look-to bot 90.0)``.

look-straight
-------------

``(clojurecraft.actions/look-straight bot)``

Changes the position of the bot's head to look straight ahead.

Exactly equivalent to ``(clojurecraft.actions/look-to bot 0.0)``.

turn-to
-------

``(clojurecraft.actions/turn-to bot yaw)``

Changes the direction the bot is looking.  ``yaw`` can be anywhere from ``0`` to
``359``.

* ``0``: looking in the -z direction.
* ``90``: looking in the -x direction.
* ``180``: looking in the +z direction.
* ``270``: looking in the +x direction.

A few notes:

* Changing your bot's yaw by too large an increment at once seems to be handled
  weirdly by the vanilla client.  It will turn partway toward the destination yaw,
  pause, and then turn the rest of the way.
* Changing the direction the bot is looking affects the *head* of your bot, not
  necessarily the *body*.  The body will rotate as much as is necessary to prevent
  an Exorcist-style head-spin.  I don't know of a way to force the body to face
  a certain direction at the moment, but I'll keep looking.

turn-north
----------

``(clojurecraft.actions/turn-north bot)``

Changes the direction the bot is looking to north.

Exactly equivalent to ``(clojurecraft.actions/turn-to bot 90.0)``.

turn-south
----------

``(clojurecraft.actions/turn-south bot)``

Changes the direction the bot is looking to south.

Exactly equivalent to ``(clojurecraft.actions/turn-to bot 270.0)``.

turn-east
---------

``(clojurecraft.actions/turn-east bot)``

Changes the direction the bot is looking to east.

Exactly equivalent to ``(clojurecraft.actions/turn-to bot 180.0)``.

turn-west
---------

``(clojurecraft.actions/turn-west bot)``

Changes the direction the bot is looking to west.

Exactly equivalent to ``(clojurecraft.actions/turn-to bot 0.0)``.

move
----

``(clojurecraft.actions/move bot x y z)``

The ``move`` action adjusts the location of the bot.  This lets it move around the
world.

Right now you can't really use the ``y`` argument.  Use ``clojurecraft.actions/jump``
instead.

This action is fairly low level.  Expect to see some fun path-finding
algorithms/libraries in the future that will remove the need to call this directly.

respawn
-------

``(clojurecraft.actions/respawn bot)``

The ``respawn`` action tells your bot to respawn.  Only send this if your bot has
died, because I'm not sure what the vanilla server will do otherwise.
