(ns ring.middleware.data-json
  "Ring middleware for parsing JSON requests and generating JSON responses."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [ring.core.protocols :as ring-protocols]
            [ring.util.request :refer [character-encoding]]
            [ring.util.response :refer [content-type]])
  (:import [java.io InputStream]
           [java.time.format DateTimeFormatter]))

(set! *warn-on-reflection* true)

(defn- is-json-request [request]
  (when-let [type (get-in request [:headers "content-type"])]
    (re-find #"^application/(.+\+)?json" type)))

(defn- read-json [request & [{:keys [keywords? bigdecimals? key-fn]}]]
  (when (is-json-request request)
    (when-let [^InputStream body (:body request)]
      (let [^String encoding (or (character-encoding request)
                                 "UTF-8")
            body-reader (java.io.InputStreamReader. body encoding)]
          (try
          [true (json/read body-reader
                           :bigdec bigdecimals?
                           :key-fn (or key-fn
                                       (when keywords?
                                         keyword)))]
          (catch Exception _ex
            [false nil]))))))

(def ^{:doc "The default response to return when a JSON request is malformed."}
  default-malformed-response
  {:status  400
   :headers {"Content-Type" "text/plain"}
   :body    "Malformed JSON in request body."})

(defn json-body-request
  "Parse a JSON request body and assoc it back into the :body key. Returns nil
  if the JSON is malformed. See: wrap-json-body."
  [request options]
  (if-let [[valid? json] (read-json request options)]
    (when valid? (assoc request :body json))
    request))

(defn wrap-json-body
  "Middleware that parses the body of JSON request maps, and replaces the :body
  key with the parsed data structure. Requests without a JSON content type are
  unaffected.

  Accepts the following options:

  :key-fn             - function that will be applied to each key
  :keywords?          - true if the keys of maps should be turned into keywords
  :bigdecimals?       - true if BigDecimals should be used instead of Doubles
  :malformed-response - a response map to return when the JSON is malformed"
  {:arglists '([handler] [handler options])}
  [handler & [{:keys [malformed-response]
               :or {malformed-response default-malformed-response}
               :as options}]]
  (fn
    ([request]
     (if-let [request (json-body-request request options)]
       (handler request)
       malformed-response))
    ([request respond raise]
     (if-let [request (json-body-request request options)]
       (handler request respond raise)
       (respond malformed-response)))))

(defn- assoc-json-params [request json]
  (if (map? json)
    (-> request
        (assoc :json-params json)
        (update-in [:params] merge json))
    request))

(defn json-params-request
  "Parse the body of JSON requests into a map of parameters, which are added
  to the request map on the :json-params and :params keys. Returns nil if the
  JSON is malformed. See: wrap-json-params."
  [request options]
  (if-let [[valid? json] (read-json request options)]
    (when valid? (assoc-json-params request json))
    request))

(defn wrap-json-params
  "Middleware that parses the body of JSON requests into a map of parameters,
  which are added to the request map on the :json-params and :params keys.

  Accepts the following options:

  :key-fn             - function that will be applied to each key
  :bigdecimals?       - true if BigDecimals should be used instead of Doubles
  :malformed-response - a response map to return when the JSON is malformed

  Use the standard Ring middleware, ring.middleware.keyword-params, to
  convert the parameters into keywords."
  {:arglists '([handler] [handler options])}
  [handler & [{:keys [malformed-response]
               :or {malformed-response default-malformed-response}
               :as options}]]
  (fn
    ([request]
     (if-let [request (json-params-request request options)]
       (handler request)
       malformed-response))
    ([request respond raise]
     (if-let [request (json-params-request request options)]
       (handler request respond raise)
       (respond malformed-response)))))

(defn- adapt-options
  "Given an options map that would work for Cheshire, adapt it to work
  with data.json:
  * :pretty -> :indent
  * :escape-non-ascii -> opposite of :escape-unicode
  * :date-format -> :date-formatter
  and unroll to a collection."
  [options]
  (-> (cond-> options
        (contains? options :pretty)
        (assoc :indent (:pretty options))
        (contains? options :escape-non-ascii)
        (assoc :escape-unicode (not (:escape-non-ascii options)))
        (contains? options :date-format)
        (assoc :date-formatter (DateTimeFormatter/ofPattern (:date-format options))))
      (dissoc :pretty :escape-non-ascii :date-format)
      (->> (mapcat identity))))

(defn- json-writer
  ([data options]
   (apply json/write-str data (adapt-options options)))
  ([data stream options]
   (let [writer (io/writer stream)]
     (apply json/write data writer (adapt-options options))
     (.flush writer))))

(defrecord JsonStreamingResponseBody [body options]
  ring-protocols/StreamableResponseBody
  (write-body-to-stream [_ _ output-stream]
    (json-writer body (io/writer output-stream) options)))

(defn json-response
  "Converts responses with a map or a vector for a body into a JSON response.
  See: wrap-json-response."
  [response options]
  (if (coll? (:body response))
    (let [generator (if (:stream? options)
                      ->JsonStreamingResponseBody
                      json-writer)
          options (dissoc options :stream?)
          json-resp (update-in response [:body] generator options)]
      (if (contains? (:headers response) "Content-Type")
        json-resp
        (content-type json-resp "application/json; charset=utf-8")))
    response))

(defn wrap-json-response
  "Middleware that converts responses with a map or a vector for a body into a
  JSON response.

  Accepts the following options:

  :key-fn            - function that will be applied to each key
  :pretty            - true if the JSON should be pretty-printed
  :escape-non-ascii  - true if non-ASCII characters should be escaped with \\u
  :stream?           - true to create JSON body as stream rather than string"
  {:arglists '([handler] [handler options])}
  [handler & [{:as options}]]
  (fn
    ([request]
     (json-response (handler request) options))
    ([request respond raise]
     (handler request (fn [response] (respond (json-response response options))) raise))))
