(ns swaggerdown.html
  (:require [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]
            [clojure.string :refer [upper-case]]))

(defn ->html
  [swagger template]
  (add-filter! :key key)
  (add-filter! :keys keys)
  (add-filter! :val val)
  (add-filter! :vals vals)
  (add-filter! :not-empty? (complement empty?))
  (add-filter! :rest rest)
  (add-filter! :upper upper-case)
  (render-file (str "templates/" template "/index.html") swagger))
