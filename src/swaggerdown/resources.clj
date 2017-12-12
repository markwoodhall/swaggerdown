(ns swaggerdown.resources
  (:require [swaggerdown.swagger :refer [swagger]]
            [swaggerdown.markdown :refer [->markdown markdown->str]]
            [swaggerdown.html :refer [->html]]
            [swaggerdown.logger :refer [info]]
            [selmer.parser :refer [render-file]]
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
  [logger]
  {:methods 
   {:post 
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:form {(s/optional-key :url) String
             (s/optional-key :template) String}}
     :produces #{"application/edn" "application/x-yaml" "application/markdown" "text/html" "application/html"}
     :response (fn [ctx] 
                 (let [url (get-in ctx [:parameters :form :url])
                       template (get-in ctx [:parameters :form :template])]
                   (info logger "Handling documentation request for %s" url)
                   (documentation-handler url template (yada/content-type ctx) ctx)))}
    :get 
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:query {(s/optional-key :url) String
              (s/optional-key :content-type) String
              (s/optional-key :template) String}}
     :produces #{"application/edn" "application/x-yaml" "application/markdown" "text/html" "application/html"}
     :response (fn [ctx] 
                 (let [url (get-in ctx [:parameters :query :url])
                       template (get-in ctx [:parameters :query :template])
                       content-type (get-in ctx [:parameters :query :content-type])]
                   (info logger "Handling documentation request for %s" url)
                   (documentation-handler url template content-type ctx)))}}})

(defn home
  [home-map]
  {:methods 
   {:get 
    {:produces #{"text/html"}
     :response (fn [ctx]
                 (render-file "public/index.html" home-map))}}})
