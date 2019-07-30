# Cogni

Experimental fullstack application built with Clojure, Clojurescript, Datomic, and websockets.

## Usage

``` sh
$ git clone git@github.com:7even/cogni.git
$ cd cogni/deploy
$ docker-compose up -d
```

Application will be available at http://localhost:2000.

## Development

`dev/user.clj` has a `(go)` function to launch the backend and a `(cljs!)` function to start
the shadow-cljs server and watcher; after calling these two functions the development version
will be available at http://localhost:8890.
