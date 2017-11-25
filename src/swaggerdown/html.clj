(ns swaggerdown.html
  (:require [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]))

(defn ->html
  [swagger]
  (add-filter! :key key)
  (add-filter! :val val)
  (add-filter! :not-empty? (complement empty?))
  (render-file "templates/fractal/index.html" swagger))
