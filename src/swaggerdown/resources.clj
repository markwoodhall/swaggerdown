(ns swaggerdown.resources
  (:require [swaggerdown.swagger :refer [swagger]]
            [swaggerdown.markdown :refer [->markdown markdown->str]]
            [swaggerdown.html :refer [->html]]
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
     :produces #{"application/edn" "application/x-yaml" "application/markdown" "text/html" "application/html"}
     :response (fn [ctx] 
                 (let [url (or (get-in ctx [:parameters :form :url]) url)]
                   (case (yada/content-type ctx)
                     ("application/edn") (swagger url true)
                     ("application/x-yaml") (-> (swagger url false)
                                                (y/generate-string :dumper-options {:flow-style :block}))
                     ("application/markdown") (->> (swagger url true)
                                                   ->markdown
                                                   markdown->str)
                     ("text/html") (->> (swagger url true)
                                        ->markdown
                                        markdown->str
                                        md-to-html-string)
                     ("application/html") (try (->> (swagger url true)
                                                    ->html)
                                               (catch Exception e
                                                 (println e)))
                     (assoc (:response ctx) :status 406 :body "Unexpected Content-Type"))))}}})
