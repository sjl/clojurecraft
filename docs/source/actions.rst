Actions
=======

Actions are functions that take a ``Bot`` object and some arguments and handle
writing the packets to make the bot perform the action.

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
