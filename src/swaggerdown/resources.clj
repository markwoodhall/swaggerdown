(ns swaggerdown.resources
  (:require [swaggerdown.generate :refer [->html ->markdown ->yaml ->edn ->json]]
            [swaggerdown.logger :refer [info wrap error]]
            [selmer.parser :refer [render-file]]
            [schema.core :as s]
            [clj-ravendb.client :as rdb]
            [tick.clock :refer [now]]
            [yada.yada :as yada]))

(defn- try-record-generation! [logger db url template content-type]
  (try
    (rdb/put-document!
     db
     (str (now))
     {:url url :template template :timestamp (str (now)) :content-type content-type
      :metadata {:Raven-Entity-Name "DocumentationGenerated"}})
    (catch Exception e
      (error logger e)))) ;; Just log the error, we don't want a failure to record to break the generation

(def access-control
  {:access-control
   {:allow-origin "*"
    :allow-credentials false
    :expose-headers #{}
    :allow-methods #{:get :post}
    :allow-headers []}})

(def documention-can-produce
  #{"application/javascript"
    "application/edn"
    "application/x-yaml"
    "application/markdown"
    "text/html"
    "application/html"})

(defn documentation-handler
  "Converts the specified url to a documentation format based on the
  supplied template and content type."
  [url template content-type ctx]
  (case content-type
    ("application/javascript") (->json url)
    ("application/edn") (->edn url)
    ("application/x-yaml") (->yaml url)
    ("application/markdown") (->markdown url)
    ("text/html") (->html url template)
    (assoc (:response ctx) :status 406 :body (str "Unexpected Content-Type:" content-type))))

(defn documentation
  [[url template] ravendb logger]
  {:methods
   {:post
    {:consumes "application/x-www-form-urlencoded"
     :parameters
     {:form {(s/optional-key :url) String
             (s/optional-key :template) String}}
     :produces documention-can-produce
     :response (wrap
                logger
                (fn [ctx]
                  (let [url (or (get-in ctx [:parameters :form :url]) url)
                        template (or (get-in ctx [:parameters :form :template]) template)
                        content-type (yada/content-type ctx)]
                    (info logger "Handling documentation request for %s" url)
                    (try-record-generation! logger ravendb url template content-type)
                    (documentation-handler url template content-type ctx))))}
    :get
    {:parameters
     {:query {(s/optional-key :url) String
              (s/optional-key :content-type) String
              (s/optional-key :template) String}}
     :produces documention-can-produce
     :response (wrap
                logger
                (fn [ctx]
                  (let [url (or (get-in ctx [:parameters :query :url]) url)
                        template (or (get-in ctx [:parameters :query :template]) template)
                        content-type (get-in ctx [:parameters :query :content-type])]
                    (info logger "Handling documentation request for %s" url)
                    (try-record-generation! logger ravendb url template content-type)
                    (documentation-handler url template content-type ctx))))}}})

(defn home
  [home-map]
  {:methods
   {:get
    {:produces #{"text/html"}
     :response (fn [ctx]
                 (render-file "public/index.html" home-map))}}})
