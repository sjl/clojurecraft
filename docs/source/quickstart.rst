Quick Start
===========

Clone down the repo with Mercurial or Git::

    hg clone http://bitbucket.org/sjl/clojurecraft
    git clone http://github.com/sjl/clojurecraft.git

Or if you don't want to bother you can just download and extract a tarball::

    wget https://bitbucket.org/sjl/clojurecraft/get/tip.tar.gz -O clojurecraft.tar.gz
    tar xzf clojurecraft.tar.gz

Grab the dependencies using Leiningen (Cake will probably work too) and fire up
a REPL::

    cd clojurecraft
    lein deps
    lein repl

Now you'll need to open another terminal window to download and run the vanilla
server::

    cd path/to/clojurecraft
    bundled/bootstrap.sh
    bundled/runserver.sh

Wait for the server to finish loading (it'll say "Done") and then connect to it with
a normal Minecraft client so you can watch your bot.

Now you can go back to your REPL and get started.  Import the things you'll need::

    (require '(clojurecraft [core :as core] [actions :as actions]))

Create a bot connected to your local server::

    (def bot (core/connect core/minecraft-local "desired_username"))

Right now Clojurecraft doesn't support authentication, so it's turned off on the
bundled server and you can choose any username you like.  You can pass ``nil``
instead of a username to get a random one.

Give your bot a little time to connect.  You should see it appear in the world
through your Minecraft client.

Once your bot is in the world you're all set to play around.  At the moment the only
action implemented is basic movement.  Move your bot around with ``actions/move``::

    (actions/perform! (actions/move bot 2 0 1))

The numbers are the x, y, and z distance you wish to move.  For now you can't use the
``y`` argument -- you must always pass ``0``.

If the bot doesn't appear to move, you may have tried to make an illegal move (like
moving into a block).  Try some other numbers.  Check the server output for more
information if something goes wrong.

Now try jumping::

    (actions/perform! (actions/jump bot))

Clojurecraft isn't stable and is evolving quickly, but you can check out these docs
to read about some of the design decisions.  As soon as you see a ``v1.0.0`` tag
in the repo you'll know I've started caring about backwards compatibility.
