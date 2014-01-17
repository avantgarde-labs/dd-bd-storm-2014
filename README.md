dd-bd-storm-2014
================

A Clojure storm example cooked up from the following projects.

<pre> https://github.com/nathanmarz/storm-starter </pre>

<pre> https://bitbucket.org/qanderson/polyglot-rolling-topology </pre>

For our Big-Data meetup in Dresden:

<pre> http://www.meetup.com/Big-Data-User-Group-Dresden </pre>



## Usage

Build steps:
------------

<pre>lein deps</pre>
<pre>lein compile</pre>

Run steps:
----------

You can run the example local with:

<pre>lein run</pre>

to run the example against a storm cluster you want to set up you storm client and stuff, find help in the storm starter project mentioned above.

If you pass a name to the main function call the topology will run not in dev mode.

<pre>lein run -m storm.rolling-topology my_test_run</pre>


Words of warning
----------------

All stuff is intended for testing purposes. Don't use this .-) .

## License

Copyright © 2014 sojoner

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
