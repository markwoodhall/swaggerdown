(ns swaggerdown.clojure
  (:require [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]
            [clojure.string :refer [upper-case]]))

(defn ->clojure
  [swagger template code-generator]
  (add-filter! :key key)
  (add-filter! :keys keys)
  (add-filter! :val val)
  (add-filter! :vals vals)
  (render-file (str "templates/code-generation/" code-generator "/" template ".edn") swagger))
