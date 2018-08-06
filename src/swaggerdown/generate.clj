(ns swaggerdown.generate
  (:require [swaggerdown.http :refer [read-swagger]]
            [swaggerdown.html :as h]
            [swaggerdown.markdown :as m]
            [yaml.core :as y]
            [cheshire.core :refer [generate-string]]))

(defn- sort-map
  [m ks]
  (if (empty? ks)
    m
    (let [k (first ks)]
      (sort-map
       (update-in m (if (sequential? k) k [k]) (partial into (sorted-map)))
       (rest ks)))))

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
  (let [swagger (read-swagger url {:keywords? true})]
    (with-out-str (clojure.pprint/pprint swagger))))

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
      (sort-map [:paths :definitions])
      m/->markdown
      m/markdown->str))

(defn ->html
  "Takes swagger url, which could be to a json or yaml specification, and
  produces html using the specified template."
  [url template]
  (-> url
      (read-swagger {:keywords? true})
      (sort-map [:paths :definitions])
      (h/->html template)))
