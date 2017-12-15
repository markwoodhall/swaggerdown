(ns swaggerdown.html
  (:require [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]
            [markdown.core :refer [md-to-html-string]]
            [clojure.string :refer [upper-case]]))

(defn- add-filters! []
  (add-filter! :key key)
  (add-filter! :keys keys)
  (add-filter! :val val)
  (add-filter! :vals vals)
  (add-filter! :not-empty? (complement empty?))
  (add-filter! :nil? nil?)
  (add-filter! :not-nil? (complement nil?))
  (add-filter! :rest rest)
  (add-filter! :upper upper-case)
  (add-filter! :markdown md-to-html-string))

(defn- swagger2
  [swagger template]
  (render-file (str "templates/" template "/index.html") swagger))

(defn- swagger3
  [swagger template]
  (render-file (str "templates/openapi/" template "/index.html") swagger))

(defn ->html
  "Takes a map (m) and looks at the `:swagger` key to try and figure out
  the OpenAPI spec version, using this version and the supplied `template`
  renders the map to html."
  [m template]
  (add-filters!)
  (if (= (:swagger m) "2.0")
    (swagger2 m template)
    (swagger3 m template)))
