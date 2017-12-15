(ns swaggerdown.generate
  (:require [swaggerdown.http :refer [read-swagger]]
            [swaggerdown.html :as h]
            [swaggerdown.markdown :as m]
            [yaml.core :as y]
            [cheshire.core :refer [generate-string]]))

(defn ->javascript [url]
  (-> url
      (read-swagger {:keywords? true})
      (generate-string {:pretty true})))

(defn ->edn [url]
  (-> url
      (read-swagger {:keywords? true})
      str))

(defn ->yaml [url]
  (-> url
      (read-swagger {:keywords? false})
      (y/generate-string :dumper-options {:flow-style :block})))

(defn ->markdown [url]
  (-> url
      (read-swagger {:keywords? true})
      m/->markdown
      m/markdown->str))

(defn ->html [url template]
  (-> url
      (read-swagger {:keywords? true})
      (h/->html template)))
