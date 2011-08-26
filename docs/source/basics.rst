Basic Concepts
==============

The basic flow of creating a Minecraft bot with Clojurecraft looks like this:

* Connect to a Minecraft server to get a ``Bot`` object.
* Define and add events handlers to the bot.
* Define and add loops to the bot.
* Let the bot do its thing.
* Disconnect the bot from the server.

You can also manually tell a bot to perform actions from a REPL.

Connecting
----------

You can connect to a Minecraft server with the ``connect`` function::

    (clojurecraft.core/connect {:name "hostname" :port INT} "username")

You can also pass ``nil`` as a username to get a random string of letters.

When you connect to a server you get a ``Bot`` object back.  Once you've got a bot
you can query it for data about its world, tell it to perform actions, and add event
handlers and loops.

Event Handlers
--------------

Event handlers are functions that let your bot react to things that happen in the
world.  Check out the :doc:`Event Handlers </events>` page for more information.

Loops
-----

Loops are functions that run every ``N`` milliseconds and let your bot query the
world and perform actions.  Check out the :doc:`Loops </loops>` page for more
information.

Disconnecting
-------------

To disconnect a bot from the server you simply call the ``disconnect`` function::

    (clojurecraft.core/disconnect bot)
