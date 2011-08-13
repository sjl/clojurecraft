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
