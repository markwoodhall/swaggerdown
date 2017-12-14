(ns swaggerdown.resources
  (:require [swaggerdown.swagger :refer [swagger]]
            [swaggerdown.markdown :refer [->markdown markdown->str]]
            [swaggerdown.html :refer [->html]]
            [swaggerdown.logger :refer [info wrap]]
            [selmer.parser :refer [render-file]]
            [cheshire.core :refer [generate-string]]
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
    ("application/javascript") (-> url
                                   (swagger {:keywords? true})
                                   (generate-string {:pretty true}))
    ("application/edn") (-> url
                            (swagger {:keywords? true})
                            str) 
    ("application/x-yaml") (-> url 
                               (swagger {:keywords? false})
                               (y/generate-string :dumper-options {:flow-style :block}))
    ("application/markdown") (-> url
                                  (swagger {:keywords? true})
                                  ->markdown
                                  markdown->str)
    ("text/html") (-> url
                      (swagger {:keywords? true})
                      (->html template))
    (assoc (:response ctx) :status 406 :body (str "Unexpected Content-Type:" content-type))))

(defn documentation
  [[url template] logger]
  {:methods 
   {:post 
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:form {(s/optional-key :url) String
             (s/optional-key :template) String}}
     :produces #{"application/javascript" "application/edn" "application/x-yaml" "application/markdown" "text/html" "application/html"}
     :response (wrap 
                 logger
                 (fn [ctx] 
                   (let [url (or (get-in ctx [:parameters :form :url]) url)
                         template (or (get-in ctx [:parameters :form :template]) template)]
                     (info logger "Handling documentation request for %s" url)
                     (documentation-handler url template (yada/content-type ctx) ctx))))}
    :get 
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:query {(s/optional-key :url) String
              (s/optional-key :content-type) String
              (s/optional-key :template) String}}
     :produces #{"application/javascript" "application/edn" "application/x-yaml" "application/markdown" "text/html" "application/html"}
     :response (wrap 
                 logger
                 (fn [ctx] 
                   (let [url (or (get-in ctx [:parameters :query :url]) url)
                         template (or (get-in ctx [:parameters :query :template]) template)
                         content-type (get-in ctx [:parameters :query :content-type])]
                     (info logger "Handling documentation request for %s" url)
                     (documentation-handler url template content-type ctx))))}}})

(defn home
  [home-map]
  {:methods 
   {:get 
    {:produces #{"text/html"}
     :response (fn [ctx]
                 (render-file "public/index.html" home-map))}}})
