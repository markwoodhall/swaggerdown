(ns swaggerdown.generators)

(defonce generators
  [{:order 1 :title "HTML" :description "Generate HTML with no formatting" :img "img/html.png" :ext ".html" :content-type "text/html" :template "default"}
   {:order 2 :title "Fractal" :description "Generate HTML using the fractal template" :img "img/fractal.png" :ext ".html" :content-type "text/html" :template "fractal"}
   {:order 3 :title "Fractal Red" :description "Generate HTML using a red fractal template" :img "img/fractal-red.png" :ext ".html" :content-type "text/html" :template "fractal-red"}
   {:order 4 :title "Markdown" :img "img/markdown.png" :ext ".md" :content-type "application/markdown" :template "default"}
   {:order 5 :title "JSON" :img "img/json.png" :ext ".json" :content-type "application/javascript" :template "default"}
   {:order 6 :title "Yaml" :img "img/yaml.png" :ext ".yml" :content-type "application/x-yaml" :template "default"}
   {:order 7 :title "EDN" :img "img/edn.png" :ext ".edn" :content-type "application/edn" :template "default"}])

(defn by-content-type-and-template [content-type template g]
  (and (= (:content-type g) content-type)
       (= (:template g) template)))
