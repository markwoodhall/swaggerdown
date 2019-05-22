(ns swaggerdown.http
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :refer [postwalk]]
            [clojure.string :as s]
            [yaml.core :as y])
  (:import [java.net UnknownHostException]))

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

(defn- c-type
  [content]
  (cond
    (and 
      (clojure.string/starts-with? content "{")
      (clojure.string/ends-with? content "}")) :json
    
    (try 
      (y/parse-string (s/replace content #"\t" "  ") false)
      (catch Exception _)) :yaml

    :else :unknown))

(defn- yaml-or-json
  [raw-string keywords?]
  (case (c-type raw-string)
    :json (parse-string raw-string keywords?)
    :yaml (keywordize-keys (disorder (y/parse-string (s/replace raw-string #"\t" "  ") false) {}))
    (throw 
      (ex-info 
        "Unable to determine type of swagger definition, it doesn't look like the url entered produces JSON or yaml."
        {:cause :ctype-unknown}))))

(defn read-swagger
  [url {:keys [keywords?]}]
  (try
    (let [raw-response (slurp url)
          parsed (yaml-or-json raw-response keywords?)]
      parsed)
    (catch UnknownHostException e
      (throw 
        (ex-info 
          (str  "Unable to load data from " 
               url \newline 
               "Are you sure you entered the right url? If you are trying to use localhost then it will not work.")
          {:cause :host-unknown})))))
