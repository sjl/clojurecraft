Loops
=====

Another way your bots will interact with the world is through loops.

Loops are functions that repeatedly run with a delay in between each run.  They
return a list of Actions that you want your bot to perform, just like event handlers.

Loops are pure functions that should take a single argument: the bot.

Adding Loops
------------

To add a loop to your bot, you first need to create the loop function::

    (defn jump [bot]
      [(clojurecraft.actions/jump bot)])

Now you can add it to the bot::

    (clojurecraft.loops/add-loop bot #'jump 3000 :jump-loop)

The first argument to the ``add-loop`` function is your bot.

Next is a symbol to your loop function.  The reason for passing a symbol is the same
as the reason you pass a symbol to event handlers.

Next is the number of milliseconds you want to wait in between each run of the loop
function.

Finally you must pass a "loop ID" keyword.  It can be anything you like, but it must
be unique for each loop added to a given bot.  This is what you'll use to remove
the loop from the bot later.

This example adds a loop to the bot that will make it jump every three seconds.

Removing Loops
--------------

Removing a loop from a bot is as simple as calling ``remove-loop`` with the bot and
the loop ID you used when adding the loop::

    (clojurecraft.loops/remove-loop bot :jump-loop)
