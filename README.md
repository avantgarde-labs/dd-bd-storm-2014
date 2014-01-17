dd-bd-storm-2014
================

A Clojure storm example cooked up from the following projects.

[storm-starter](https://github.com/nathanmarz/storm-starter)

[rolling-topology](https://bitbucket.org/qanderson/polyglot-rolling-topology)

For the Feb/14 Big-Data meetup in Dresden:

[DD-BD-Meetup](http://www.meetup.com/Big-Data-User-Group-Dresden)

Also thx to [Michael G. Noll](http://www.michael-noll.com/blog/2013/01/18/implementing-real-time-trending-topics-in-storm/).

Abstract:

This example should compute rolling trending topics via storm.
For presentation purpose i added a Redis Spout and the resulting
ranks will be indexed to elasticsearch thx to this lib (storm-elastic-search)[https://github.com/hmsonline/storm-elastic-search].

In combination with this [storm-vagrant](https://github.com/sojoner/storm-vagrant) setup,
it's very easy to get some play time with [storm](https://github.com/nathanmarz/storm)


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

Some configs you might want to consider:
----------------------------------------

<pre>src/clj/storm/rolling_topology.clj</pre>

is the place to make some elasticsearch settings, which is

<pre>
      "elastic.search.cluster" "your cluster name"
      "elastic.search.host" "your host"
      "elastic.search.port" 9300
</pre>

and the redis list to pop items from is set on top of the file,

<pre>(def REDIS_LIST "mylist")</pre>

also in,

<pre>src/clj/utils/redis.clj</pre>

you can set the

<pre> :host "127.0.0.1" :port 6379 </pre>

to your redis instance. On the $redis-cli i manually added token sequeces like this.

<pre>127.0.0.1:6379> rpush mylist "An awesome sentence with token."</pre>


Words of warning
----------------

All stuff is intended for testing purposes. Don't use this .-) .

## License

Copyright © 2014 sojoner

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
