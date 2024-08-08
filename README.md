# Ring-Data-JSON

Standard Ring middleware functions for handling JSON requests and
responses -- using
[org.clojure/data.json](https://github.com/clojure/data.json),
rather than Cheshire.

Cheshire is a great library but it depends on Jackson for JSON
encoding and decoding which can lead to "dependency hell" and is also
so widely-used that it is often targeted by malicious actors and that
leads to security concerns. `org.clojure/data.json` is a pure Clojure
JSON library that has no dependencies and is "fast enough" for most
use cases.

This library uses the `ring.middleware.data-json` namespace to
distinguish it from the original `ring.middleware.json` namespace
in Ring's [ring-json](https://github.com/ring-clojure/ring-json)
middleware, but is otherwise a drop-in replacement for that library.

In addition to supporting `:pretty`, `:escape-non-ascii`, `:keywords?`,
and `:bigdecimals?` options (for compatibility with `ring-json`, and
Cheshire), this library also supports all the options that
`clojure.data.json` supports (`:key-fn` is common to both libraries,
`:indent true` is the equivalent to `:pretty true`,
`:escape-unicode true` -- the default -- is equivalent to
`:escape-non-ascii true` so that is a difference in the default
behavior).

## Installation

To install, add the following to your project `:dependencies`:

    [com.github.seancorfield/ring-data-json "0.5.2"]

or to your `deps.edn` file's `:deps`:

    com.github.seancorfield/ring-data-json {:mvn/version "0.5.2"}

## Usage

#### wrap-json-response

The `wrap-json-response` middleware will convert any response with a
collection as a body (e.g. map, vector, set, seq, etc) into JSON:

```clojure
(require '[ring.middleware.data-json :refer [wrap-json-response]]
         '[ring.util.response :refer [response]])

(defn handler [request]
  (response {:foo "bar"}))

(def app
  (wrap-json-response handler))
```


#### wrap-json-body

The `wrap-json-body` middleware will parse the body of any request
with a JSON content-type into a Clojure data structure, and assign it
to the `:body` key.

This is the preferred way of handling JSON requests.

```clojure
(use '[ring.middleware.data-json :only [wrap-json-body]]
     '[ring.util.response :only [response]])

(defn handler [request]
  (prn (get-in request [:body "user"]))
  (response "Uploaded user."))

(def app
  (wrap-json-body handler {:keywords? true :bigdecimals? true}))
```


#### wrap-json-params

The `wrap-json-params` middleware is an alternative to
`wrap-json-body` for when it's convenient to treat a JSON request as a
map of parameters. Rather than replace the `:body` key, the parsed
data structure will be assigned to the `:json-params`. The parameters
will also be merged into the standard `:params` map.

```clojure
(require '[ring.middleware.data-json :refer [wrap-json-params]]
         '[ring.util.response :refer [response]])

(defn handler [request]
  (prn (get-in request [:params "user"]))
  (response "Uploaded user."))

(def app
  (wrap-json-params handler))
```

Note that Ring parameter maps use strings for keys. For consistency,
this means that `wrap-json-params` does not have a `:keywords?`
option. Instead, use the standard Ring `wrap-keyword-params` function:

```clojure
(require '[ring.middleware.keyword-params :refer [wrap-keyword-params]])

(def app
  (-> handler
      wrap-keyword-params
      wrap-json-params))
```


## License

Copyright © 2021-2024 James Reeves, 2024 Sean Corfield.

Distributed under the MIT License, the same as Ring.
