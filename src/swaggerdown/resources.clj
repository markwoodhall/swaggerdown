(ns swaggerdown.resources
  (:require [swaggerdown.swagger :refer [swagger]]
            [swaggerdown.coercion :refer [->markdown markdown->str]]
            [markdown.core :refer [md-to-html-string]]
            [schema.core :as s]
            [yaml.core :as y]
            [yada.yada :as yada]))

(def access-control
  {:access-control
  {:allow-origin "*"
   :allow-credentials false
   :expose-headers #{}
   :allow-methods #{:get :post}
   :allow-headers []
   }})

(defn documentation
  [[url]]
  {:methods 
   {:post 
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:form {(s/optional-key :url) String}}
     :produces #{"application/x-yaml" "application/markdown" "text/html"}
     :response (fn [ctx] 
                 (let [url (or (get-in ctx [:parameters :form :url]) url)]
                   (case (yada/content-type ctx)
                     ("application/x-yaml") (-> (swagger url)
                                                (y/generate-string :dumper-options {:flow-style :block}))
                     ("application/markdown") (->> (swagger url)
                                                   ->markdown
                                                   markdown->str)
                     ("text/html") (->> (swagger url)
                                        ->markdown
                                        markdown->str
                                        md-to-html-string)
                     (assoc (:response ctx) :status 406 :body "Unexpected Content-Type"))))}}})
