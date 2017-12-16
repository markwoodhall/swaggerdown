(ns swaggerdown.generate
  (:require [swaggerdown.http :refer [read-swagger]]
            [swaggerdown.html :as h]
            [swaggerdown.markdown :as m]
            [yaml.core :as y]
            [cheshire.core :refer [generate-string]]))

(defn ->json
  "Takes swagger url, which could be to a json or yaml specification, and
  produces json."
  [url]
  (-> url
      (read-swagger {:keywords? true})
      (generate-string {:pretty true})))

(defn ->edn
  "Takes swagger url, which could be to a json or yaml specification, and
  produces edn."
  [url]
  (-> url
      (read-swagger {:keywords? true})
      str))

(defn ->yaml
  "Takes swagger url, which could be to a json or yaml specification, and
  produces yaml."
  [url]
  (-> url
      (read-swagger {:keywords? false})
      (y/generate-string :dumper-options {:flow-style :block})))

(defn ->markdown
  "Takes swagger url, which could be to a json or yaml specification, and
  produces markdown."
  [url]
  (-> url
      (read-swagger {:keywords? true})
      m/->markdown
      m/markdown->str))

(defn ->html
  "Takes swagger url, which could be to a json or yaml specification, and
  produces html using the specified template."
  [url template]
  (-> url
      (read-swagger {:keywords? true})
      (h/->html template)))
