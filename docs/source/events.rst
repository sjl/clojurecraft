Events
======

The main way you'll interact with your Clojurecraft bots is through event handlers.

Event handlers are functions you create that respond to events that happen in the
Minecraft world.  They return a list of Actions that you want your bot to perform.

Event handlers are pure functions that should take the bot as their first argument.
Their other arguments will depend on the particular handler.

Creating and Registering Event Handlers
---------------------------------------

The first thing you need to do is create an event handling function::

    (defn jump-on-chat [bot message]
      [(clojurecraft.actions/jump bot)])

Then register the handler for the action::

    (clojurecraft.events/add-handler bot :chat #'jump-on-chat)

Notice that you don't pass the function directly to the ``add-handler`` function.
You pass a symbol to the function.  This is two extra characters to type, but it
means you can redefine the function in the REPL and your changes will take effect
immediately in all of the currently running bots.

Available Events
----------------

You can register handlers for the following events.

``:chat``
`````````

::

    (defn chat-handler [message]
      [... actions ...])

Chat events are fired when a chat message arrives.

A few things to note when writing bots for the vanilla server:

First, a "chat message" includes things like "foo joined/left the game.".  You can
parse these and take appropriate action if you like.

The other important point is that *you will receive your own messages*.  If you fire
a chat action you'll get a message back for it!  A helpful function to use might be
something like this::

    (defn message-is-own? [bot message]
      (clojure.contrib.string/substring? (str "<" (:username bot) ">")
                                         message))
