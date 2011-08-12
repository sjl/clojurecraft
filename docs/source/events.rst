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
