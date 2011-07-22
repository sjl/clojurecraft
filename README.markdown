Clojurecraft
============

Clojurecraft is a framework for writing Minecraft bots in Clojure.

**It's definitely not ready for actual use yet, but if you want to poke around it has
some very basic functionality.  Just remember that *everything* is subject to
change.**

Quick Start
-----------

Clone down the repo with Mercurial or Git:

    hg clone http://bitbucket.org/sjl/clojurecraft
    git clone http://github.com/sjl/clojurecraft.git

Grab the dependencies using Leiningen (Cake will probably work too) and fire up
a REPL:

    cd clojurecraft
    lein deps
    lein repl

Now you'll need to open another terminal window to download and run the vanilla
server:

    cd path/to/clojurecraft
    bundled/bootstrap.sh
    bundled/runserver.sh

Wait for the server to finish loading (it'll say "Done") and then connect to it with
a normal Minecraft client so you can watch your bot.

Now you can go back to your REPL and get started.  Import the things you'll need:

    (require '(clojurecraft [core :as cc] [actions :as act]))

Create a bot connected to your local server:

    (def bot (cc/connect cc/minecraft-local "desired_username"))

Right now Clojurecraft doesn't support authentication, so it's turned off on the
bundled server and you can choose any username you like.  You can pass `nil` instead
of a username to get a random one.

Give your bot a little time to connect.  You should see it appear in the world
through your Minecraft client.

Once your bot is in the world you're all set to play around.  At the moment the only
action implemented is basic movement.  Move your bot around with `act/move`:

    (act/move bot 2 0 1)

The numbers are the x, y, and z distance you wish to move.

If the bot doesn't appear to move, you may have tried to make an illegal move (like
moving into a block).  Try some other numbers.  Check the server output for more
information if something goes wrong.

More Information
================

Clojurecraft isn't stable and is evolving quickly, but you can check out the
[preliminary docs](https://github.com/sjl/clojurecraft/blob/master/docs.markdown) to
read about some of the design decisions.
