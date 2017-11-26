(ns swaggerdown.html
  (:require [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]))

(defn ->html
  [swagger]
  (add-filter! :key key)
  (add-filter! :keys keys)
  (add-filter! :val val)
  (add-filter! :not-empty? (complement empty?))
  (add-filter! :rest rest)
  (render-file "templates/fractal/index.html" swagger))
