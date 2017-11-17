(ns swaggerdown.swagger
  (:require [yada.yada :refer [listener resource as-resource]]
            [cheshire.core :refer [parse-string]]))

(defn swagger
  [url keywords?]
  (let [raw-response (slurp url)
        parsed (parse-string raw-response keywords?)]
    parsed))

