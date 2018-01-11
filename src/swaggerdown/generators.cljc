(ns swaggerdown.generators)

(defn by-content-type-and-template [content-type template g]
  (and (= (:content-type g) content-type)
       (= (:template g) template)))
