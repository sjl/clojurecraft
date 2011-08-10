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
