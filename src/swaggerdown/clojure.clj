(ns swaggerdown.clojure
  (:require [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]
            [clojure.string :refer [upper-case]]))

(defn ->clojure
  [swagger template]
  (add-filter! :key key)
  (add-filter! :keys keys)
  (add-filter! :val val)
  (add-filter! :vals vals)
  (render-file (str "templates/code-generation/clojure/clj-spec/" template ".edn") swagger))
