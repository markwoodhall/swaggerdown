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
  [swagger template]
  (add-filters!)
  (if (= (:swagger swagger) "2.0")
    (swagger2 swagger template)
    (swagger3 swagger template)))
