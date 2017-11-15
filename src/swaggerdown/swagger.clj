(ns swaggerdown.swagger
  (:require [yada.yada :refer [listener resource as-resource]]
            [cheshire.core :refer [parse-string]]))

(defn swagger
  [url]
  (let [raw-response (slurp url)
        parsed (parse-string raw-response true)]
    parsed))
