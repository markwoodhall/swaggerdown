(ns swaggerdown.coercion
  (:require [marge.core :as marge]
            [clojure.string :as s]))

(defn- add-produces
  [col]
  [:br
   :h3 "Content Types Produced"
   :table ["Produces" (if (empty? col) ["None"] col)]])

(defn- add-consumes
  [col]
  [:br
   :h3 "Content Types Consumed"
   :table ["Consumes" (if (empty? col) ["None"] col)]])

(defn- add-responses
  [method-name method responses]
  (let [response-keys (keys responses) 
        statuses (map name response-keys)
        descriptions (map #(:description (% responses)) response-keys)]
    [:h2 (clojure.string/upper-case method-name)
     :p (:summary method)
     :p (:description method)
     :h3 "Expected Response Types"
     :table ["Response" statuses "Reason" descriptions]]))

(defn- get-methods
  [path]
  (reduce
    (fn [col m] 
      (let [method-name (name m)
            method (m path)]
        (-> col
            (concat (add-responses method-name method (:responses (m path))))
            (concat (add-produces (:produces (m path))))
            (concat (add-consumes (:consumes (m path))))))) []
    (keys path)))

(defn- get-paths
  [{:keys [paths]}]
  (reduce-kv 
    (fn [a k v]
      (-> a 
          (concat [:h2 (name k)])
          (concat (get-methods v)))) [] paths))

(defn- type-or-ref
  [{:keys [$ref type]}]
  (or $ref type))

(defn- format-or-empty
  [{:keys [format]}]
  (or format ""))

(defn- get-properties
  [definition]
  (map
    (fn [m] 
      {:property (name m)
       :type  (type-or-ref (m (:properties definition)))
       :format  (format-or-empty (m (:properties definition)))}) 
    (keys (:properties definition))))

(defn- definition-types
  [properties]
  (->> (map :type properties)
       (map 
         (fn map-def-type 
           [t] 
           (if (s/includes? t "/definitions/")
             (let [short-link (s/replace t "/definitions/" "")
                   short-text (s/replace short-link "#" "")]
               [:link 
                {:text short-text :url (s/lower-case short-link)}])
             t)))
       (flatten)))

(defn- get-definitions
  [{:keys [definitions]}]
  (reduce-kv 
    (fn [a k v] 
      (let [properties (get-properties v)]
        (conj 
          a 
          :h3 (name k) 
          :table ["Property" (map :property properties) 
                  "Type" (definition-types properties)
                  "Format" (map :format properties)]))) [] definitions))

(defn ->markdown
  [{:keys [info externalDocs] :as swagger}]
  (let [{:keys [version title description termsOfService license]} info
        email (get-in info [:contact :email])]
    (-> [:h2 title
         :p description
         :h3 "About"
         :table ["Version"
                 [version]
                 "Contact" 
                 [:link {:text email :url (str "mailto:" email) :title "Contact Email"}] 
                 "Terms of Service"
                 [:link {:text termsOfService :url termsOfService :title "Terms of Service"}]
                 "License"
                 [:link {:text (:name license) :url (:url license) :title "License"}]]
         :br
         :h2 "Api"]
        (concat (get-paths swagger))
        (concat [:h2 "Definitions"])
        (concat (get-definitions swagger))
        (concat [:h2 "Additional Resources"
                 :link {:text (:description externalDocs) :url (:url externalDocs) :title "External Documentation"}]))))

(defn markdown->str
  [markdown]
  (marge/markdown markdown))
