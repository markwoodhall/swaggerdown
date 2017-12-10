(ns swaggerdown.swagger
  (:require [yada.yada :refer [listener resource as-resource]]
            [yaml.core :as y]
            [cheshire.core :refer [parse-string]]))

(defn json?
  [raw-string]
  (and (clojure.string/starts-with? raw-string "{")
       (clojure.string/ends-with? raw-string "}")))

(defn yaml-or-json
  [raw-string keywords?]
  (if (json? raw-string)
    (parse-string raw-string keywords?)
    (y/parse-string raw-string)))

(defn swagger
  [url keywords?]
  (let [raw-response (slurp url)
        parsed (yaml-or-json raw-response keywords?)]
    parsed))

