# Ring-Data-JSON [![Clojure CI Release](https://github.com/seancorfield/ring-data-json/actions/workflows/test-and-release.yml/badge.svg)](https://github.com/seancorfield/ring-data-json/actions/workflows/test-and-release.yml) [![Clojure CI Develop](https://github.com/seancorfield/ring-data-json/actions/workflows/test-and-snapshot.yml/badge.svg)](https://github.com/seancorfield/ring-data-json/actions/workflows/test-and-snapshot.yml) [![Clojure CI Pull Request](https://github.com/seancorfield/ring-data-json/actions/workflows/test.yml/badge.svg)](https://github.com/seancorfield/ring-data-json/actions/workflows/test.yml)

Standard Ring middleware functions for handling JSON requests and
responses -- using
[org.clojure/data.json](https://github.com/clojure/data.json),
rather than Cheshire.

## TL;DR

The latest versions on Clojars and on cljdoc:

[![Clojars](https://img.shields.io/badge/clojars-com.github.seancorfield/ring-data-json_0.5.2-SNAPSHOT-blue.svg?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAABjFBMVEUAAAAdCh0qDikdChwAAAAnDSY0EjM2FjUnDiYnDSYnDSYpDigyEDEEAQRGNUb///////8mDSYAAAAAAAAAAAAFAgUqEyoAAAAAAAAAAAAFAgUAAABXU1c2FjVMx+dQx+f///////9Nx+b////4/f6y4vRPt+RQtOT///9Qt+P///8oDSey4vRQr9/////3/P5hzelNx+dNx+dNx+f///8AAAAuDy0zETIAAAAoDScAAAAAAAARBREAAAAvDy40ETMwEC9gSF+Ne42ilKKuoK6Rg5B5ZXlaP1o4Gzf///9nTWZ4YncyEDF/bn/8/Pz9/P339/c1FTUlDCRRM1AbCRtlS2QyEDEuDy1gRWAxEDAzETIwEC/g4OAvDy40EjOaiZorDiq9sbzNyM3UzdQyEDE0ETMzETKflZ/UzdQ5Fzmu4fNYyuhNx+dPt+RLu9xQyOhBbo81GTuW2vCo4PJNx+c4MFE5N1lHiLFEhKQyEDGDboMzETI5Fjh5bXje2d57aHrIw8jc2NyWhJUrDioxe9o4AAAAPnRSTlMAkf+IAQj9+e7n6e31RtqAD/QAAAED+A0ZEQ8DwvkLBsmcR4aG8+cdAD6C8/MC94eP+qoTrgH+/wj1HA8eEvpXOCUAAAABYktHRA8YugDZAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3wcHFjou4Z/shwAAAUpJREFUOMul0/VTwzAUB/AAwyW4y3B3h8EDNuTh7u6UDHcd8I+TbHSjWdrjju/1h77kc+3Lu5aQvyakF/r6B5wu1+DQMEBomLRtG0EpozYDCEccA4iIjIqOiY0bB5iYxHgZ4FQCpYneKmmal0aQPMOXZnUAvJhLkbpInf8NFtKCTrGImK6DJcTlDGl/BXGV6oCsrSNIYAM3aQDwl2xJYBtBB5lZAuyYgWzY3YMcNcjN2wc4EGMEFTg8+hlyfgEenygAj71Q9FBExH0wKC4p1bRTJlJWXqEAVNM05ovbXfkPAHBmAUQPAGaAsXMBLiwA8z3h0gRcsWsObuAWLJu8Awb3ZoB5T8EvS/CgBo9Y5Z8TPwXBJwlUI9Ia/yRrEZ8lID71Olrf0MiamkkL4kurDEjba+C/e2sninR0wrsH8eMTvrqIWbodjh7jyjdtCY3Aniz4jwAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNS0wNy0wN1QyMjo1ODo0NiswMjowMCgWtSoAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTUtMDctMDdUMjI6NTg6NDYrMDI6MDBZSw2WAAAAAElFTkSuQmCC)](https://clojars.org/com.github.seancorfield/ring-data-json)
[![cljdoc](https://cljdoc.org/badge/com.github.seancorfield/ring-data-json?0.5.2-SNAPSHOT)](https://cljdoc.org/d/com.github.seancorfield/ring-data-json/CURRENT)
[![Slack](https://img.shields.io/badge/slack-ring-data-json-orange.svg?logo=slack)](https://clojurians.slack.com/app_redirect?channel=sql)
[![Join Slack](https://img.shields.io/badge/slack-join_clojurians-orange.svg?logo=slack)](http://clojurians.net)

### Rationale

Cheshire is a great library but it depends on Jackson for JSON
encoding and decoding which can lead to "dependency hell" and is also
so widely-used that it is often targeted by malicious actors and that
leads to security concerns. `org.clojure/data.json` is a pure Clojure
JSON library that has no dependencies and is "fast enough" for most
use cases.

This library uses the `ring.middleware.data-json` namespace to
distinguish it from the original `ring.middleware.json` namespace
in Ring's [ring-json](https://github.com/ring-clojure/ring-json)
middleware, but is otherwise intended to be a drop-in replacement
for that library.

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

### Differences from Ring-JSON

Although this is intended to be a drop-in replacement for Ring-JSON,
there are inevitably going to be some corner case differences. Those
will be documented here as they are discovered.

#### Options

The following options are supported for compatibility:

* `:bigdecimals?` -- can be set to `true` to return floating point numbers as `BigDecimal`; the equivalent `data.json` option is `:bigdec true`.
* `:escape-non-ascii` -- can be set to `true` or `false` to enable or disable escaping of non-ASCII characters in the output; *the default for Ring-JSON is `false` but the default for Ring-Data-JSON is `true`*; the underlying `data.json` option is `:escape-unicode`.
* `:key-fn` -- this is the same for both libraries (and it is asymmetric between response and params/body).
* `:keywords?` -- can be set to `true` to convert keys in maps to keywords; the equivalent `data.json` option is `:key-fn keyword` (which can also be used by both libraries).
* `:pretty` -- can be set to `true` to pretty-print the JSON output; the equivalent `data.json` option is `:indent true` and there are some slight formatting differences (e.g., no space before the colon in maps).

Ring-Data-JSON does not support Cheshire-specific options beyond these,
but it supports all the options that `data.json` supports.

In particular, for Ring-JSON, you could specify `:date-format` as
as format string for `java.text.SimpleDateFormat` to control how dates
were formatted in the output. Ring-Data-JSON instead uses the
`:date-formatter` option which should be
a `java.time.format.DateTimeFormatter` instance.

#### Date/Time Formatting

As noted above, Ring-Data-JSON uses `java.time.format.DateTimeFormatter`
and coerces date/time values to `java.time.Instant` before formatting.

Since Ring-JSON uses `java.text.SimpleDateFormat` and older date/time
types, there are some differences in which formats are supported.

> Note: discussion is ongoing about enhancing `data.json`'s date/time formatting support that may make this difference moot.

#### Supported Clojure Versions

Ring-JSON still supports Clojure 1.7.0. Ring-Data-JSON requires
at least Clojure 1.8.0.

## License

Copyright © 2021-2024 James Reeves, 2024 Sean Corfield.

Distributed under the MIT License, the same as Ring.
