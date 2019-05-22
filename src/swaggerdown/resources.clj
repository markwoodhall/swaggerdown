(ns swaggerdown.resources
  (:require
   [schema.core :as s]
   [selmer.parser :refer [render-file]]
   [swaggerdown.db :refer [events record-event!]]
   [swaggerdown.generate :refer [->edn ->html ->json ->markdown ->yaml]]
   [swaggerdown.logger :refer [info error wrap]]
   [yada.yada :as yada]))

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
  [url template content-type ctx logger]
  (try 
    (case content-type
      ("application/javascript") (->json url)
      ("application/edn") (->edn url)
      ("application/x-yaml") (->yaml url)
      ("application/markdown") (->markdown url)
      ("text/html") (->html url template)
      (assoc (:response ctx) :status 406 :body (str "Unexpected Content-Type:" content-type)))
    (catch Exception e
      (error logger e)
      (if (= :ctype-unknown (-> e ex-data :cause))
        (assoc (:response ctx) :status 400 :body (.getMessage e))
        (assoc (:response ctx) :status 500 :body "There was a problem generating the documentation.")))))

(defn documentation
  [[url template] db logger]
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
                    (info
                     logger
                     "Handling documentation request for %s using content-type %s"
                     url content-type)
                    (record-event!
                     db
                     "DocumentationGenerated"
                     {:url url :template template :contenttype content-type})
                    (documentation-handler url template content-type ctx logger))))}
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
                    (info
                     logger
                     "Handling documentation request for %s using content-type %s"
                     url content-type)
                    (record-event!
                     db
                     "DocumentationGenerated"
                     {:url url :template template :contenttype content-type})
                    (documentation-handler url template content-type ctx))))}}})

(defn home
  [home-map]
  {:methods
   {:get
    {:produces #{"text/html"}
     :response (fn [ctx]
                 (render-file "public/index.html" home-map))}}})

(defn stats
  [db logger]
  {:methods
   {:get
    {:produces #{"application/edn"}
     :response (wrap
                logger
                (fn [ctx]
                  (events db "CountDocumentGenerationsByType")))}}})

(defn generators
  [generators-col logger]
  {:methods
   {:get
    {:produces #{"application/edn"}
     :response (wrap
                logger
                (fn [ctx]
                  (filter #(not (:disabled? %)) generators-col)))}}})
