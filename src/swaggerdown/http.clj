(ns swaggerdown.http
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :refer [postwalk]]
            [clojure.string :as s]
            [yaml.core :as y]))

(defn- disorder [ordering-map map-fn]
  (postwalk #(if (map? %) (into map-fn %) %) ordering-map))

(defn- keywordize-keys
  [m]
  (let [f (fn [[k v]]
            (let [sk (if (number? k) (str k) k)]
              (if (string? sk)
                [(keyword sk) v]
                [k v])))]
    (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn- json?
  [raw-string]
  (and (clojure.string/starts-with? raw-string "{")
       (clojure.string/ends-with? raw-string "}")))

(defn- yaml-or-json
  [raw-string keywords?]
  (if (json? raw-string)
    (parse-string raw-string keywords?)
    (keywordize-keys (disorder (y/parse-string (s/replace raw-string #"\t" "  ") false) {}))))

(defn read-swagger
  [url {:keys [keywords?]}]
  (let [raw-response (slurp url)
        parsed (yaml-or-json raw-response keywords?)]
    parsed))
