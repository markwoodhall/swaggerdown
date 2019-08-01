(ns swaggerdown.markdown
  (:require
   [clojure.string :as s]
   [marge.core :as marge]))

(defn- type-or-ref
  [{:keys [$ref type]}]
  (or $ref type))

(defn- schema-or-type
  [{:keys [schema type]}]
  (or (type-or-ref schema) type))

(defn map-type-link
  [t]
  (when t
    (if (s/includes? t "/definitions/")
      (let [short-link (str (s/replace t "/definitions/" "") "-definition")
            short-text (s/replace (s/replace short-link "#" "") "-definition" "")]
        [:link
         {:text short-text :url (s/lower-case short-link)}])
      t)))

(defn cols-or-none
  [col]
  (if (empty? col) ["None"] col))

(defn- produces
  [col]
  [:br
   :h3 "Content Types Produced"
   :table ["Produces" (cols-or-none col)]])

(defn- consumes
  [col]
  [:br
   :h3 "Content Types Consumed"
   :table ["Consumes" (cols-or-none col)]])

(defn- secures
  [col]
  [:br
   :h3 "Security"
   :table ["Id" (cols-or-none (map name (flatten (map keys col))))
           "Scopes" (cols-or-none (map vals col))]])

(defn- responses
  [responses]
  (let [response-keys (keys responses)
        statuses (map name response-keys)
        descriptions (map #(:description (% responses)) response-keys)]
    [:h3 "Expected Response Types"
     :table ["Response" statuses "Reason" descriptions]]))

(defn- params
  [col]
  (let [ins (vec (map :in col))
        names (vec (map :name col))
        descriptions (vec (map :description col))
        requireds (vec (map :required col))
        types (vec (flatten (map (comp map-type-link schema-or-type) col)))]
    [:br
     :h3 "Parameters"
     :table ["Name" names
             "In" ins
             "Description" descriptions
             "Required?" requireds
             "Type" types]]))

(defn- verbs
  [path]
  (reduce
   (fn [col m]
     (let [method-name (name m)
           method (m path)]
       (-> col
           (concat [:h2 (clojure.string/upper-case method-name)
                    :h3 (:operationId method)
                    :p (:summary method)
                    :p (:description method)])
           (concat (responses (:responses (m path))))
           (concat (params (:parameters (m path))))
           (concat (produces (:produces (m path))))
           (concat (consumes (:consumes (m path))))
           (concat (secures (:security (m path)))))))
   []
   (keys path)))

(defn- paths
  [{:keys [paths]}]
  (reduce-kv
   (fn [a k v]
     (-> a
         (concat [:h2 (name k)])
         (concat (verbs v)))) [:h2 "Endpoints"] paths))

(defn- key-or-empty
  [k m]
  (or (k m) ""))

(defn- properties
  [definition]
  (map
   (fn [m]
     {:property (name m)
      :type  (type-or-ref (m (:properties definition)))
      :format  (key-or-empty :format (m (:properties definition)))})
   (keys (:properties definition))))

(defn- definition-types
  [properties]
  (->> (map :type properties)
       (map map-type-link)
       (flatten)))

(defn- definitions
  [{:keys [definitions]}]
  (reduce-kv
   (fn [a k v]
     (let [properties (properties v)]
       (conj
        a
        :h3 (str (name k) " Definition")
        :table ["Property" (map :property properties)
                "Type" (definition-types properties)
                "Format" (map :format properties)]))) [:h2 "Definitions"] definitions))

(defn- schemes
  [{:keys [schemes]}]
  [:h3 "Schemes"
   :table ["Scheme" schemes]
   :br])

(defn- security
  [{:keys [securityDefinitions]}]
  (let [defs (vals securityDefinitions)
        types (map (partial key-or-empty :type) defs)
        flows (map (partial key-or-empty :flow) defs)
        names (map (partial key-or-empty :name) defs)
        ins (map (partial key-or-empty :in) defs)
        auth-urls (map (partial key-or-empty :authorizationUrl) defs)
        scopes (map (partial key-or-empty :scopes) defs)
        scope-names (map #(if (keyword? %) (name %)  %) (flatten (map keys scopes)))
        scopes-combined (map #(if (empty? (keys %)) "" (clojure.string/join ", " (keys %))) scopes)
        scope-vals (map vals scopes)]
    [:h2 "Security Definitions"
     :table ["Id" (map name (keys securityDefinitions))
             "Type" types
             "Flow" flows
             "Authorization Url" auth-urls
             "Name" names
             "In" ins
             "Scopes" scopes-combined]
     :br
     :table ["Scope" scope-names
             "Description" scope-vals]]))

(defn ->markdown
  [{:keys [info externalDocs host basePath] :as swagger}]
  (let [{:keys [version title description termsOfService license]} info
        email (get-in info [:contact :email])]
    (-> [:h2 title
         :p description
         :h3 "About"
         :table ["Url"
                 [:link {:text (str host basePath) :url (str "http://" host basePath) :title "API url"}]
                 "Version"
                 [version]
                 "Contact"
                 [:link {:text email :url (str "mailto:" email) :title "Contact Email"}]
                 "Terms of Service"
                 [:link {:text termsOfService :url termsOfService :title "Terms of Service"}]
                 "License"
                 [:link {:text (:name license) :url (:url license) :title "License"}]]
         :br]
        (concat (schemes swagger))
        (concat (paths swagger))
        (concat (security swagger))
        (concat (definitions swagger))
        (concat [:h2 "Additional Resources"
                 :link {:text (:description externalDocs) :url (:url externalDocs) :title "External Documentation"}]))))

(defn markdown->str
  [markdown]
  (marge/markdown markdown))
