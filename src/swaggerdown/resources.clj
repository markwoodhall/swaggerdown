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

(defn documentation-handler
  [url template content-type ctx]
  (case content-type
    ("application/edn") (str (swagger url true))
    ("application/x-yaml") (-> (swagger url false)
                               (y/generate-string :dumper-options {:flow-style :block}))
    ("application/markdown") (->> (swagger url true)
                                  ->markdown
                                  markdown->str)
    ("text/html") (->html (swagger url true) template)
    (assoc (:response ctx) :status 406 :body (str "Unexpected Content-Type:" content-type))))

(defn documentation
  [[url template]]
  {:methods 
   {:post 
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:form {(s/optional-key :url) String
             (s/optional-key :template) String}}
     :produces #{"application/edn" "application/x-yaml" "application/markdown" "text/html" "application/html"}
     :response (fn [ctx] 
                 (let [url (or (get-in ctx [:parameters :form :url]) url)
                       template (or (get-in ctx [:parameters :form :template]) template)]
                   (documentation-handler url template (yada/content-type ctx) ctx)))}
    :get 
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:query {(s/optional-key :url) String
              (s/optional-key :content-type) String
              (s/optional-key :template) String}}
     :produces #{"application/edn" "application/x-yaml" "application/markdown" "text/html" "application/html"}
     :response (fn [ctx] 
                 (let [url (or (get-in ctx [:parameters :query :url]) url)
                       template (or (get-in ctx [:parameters :query :template]) url)
                       content-type (get-in ctx [:parameters :query :content-type])]
                   (documentation-handler url template content-type ctx)))}}})
