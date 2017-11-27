(ns swaggerdown.html
  (:require [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]
            [markdown.core :refer [md-to-html-string]]
            [clojure.string :refer [upper-case]]))

(defn ->html
  [swagger template]
  (add-filter! :key key)
  (add-filter! :keys keys)
  (add-filter! :val val)
  (add-filter! :vals vals)
  (add-filter! :not-empty? (complement empty?))
  (add-filter! :not-nil? (complement nil?))
  (add-filter! :rest rest)
  (add-filter! :upper upper-case)
  (add-filter! :markdown md-to-html-string)
  (render-file (str "templates/" template "/index.html") swagger))
