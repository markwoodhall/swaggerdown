(ns swaggerdown.client
    (:require [clojure.string :as s]
              [cljs.reader :refer [read-string]]
              [clova.core :as clova]
              [goog.crypt.base64 :as b64]
              [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce url-validation [:url clova/required? clova/url?])

(defonce app-state 
  (atom {:url "http://petstore.swagger.io/v2/swagger.json"
         :stats {:count 10000}
         :generators-visible? true
         :expanded? false
         :generators [{:order 1 :title "HTML" :description "Generate HTML with no formatting" :img "img/html.png" :ext ".html" :content-type "text/html" :template "default"}
                      {:order 2 :title "Fractal" :description "Generate HTML using the fractal template" :img "img/fractal.png" :ext ".html" :content-type "text/html" :template "fractal"}
                      {:order 3 :title "Fractal Red" :description "Generate HTML using a red fractal template" :img "img/fractal-red.png" :ext ".html" :content-type "text/html" :template "fractal-red"}
                      {:order 4 :title "Markdown" :img "img/markdown.png" :ext ".md" :content-type "application/markdown" :template "default"}
                      {:order 5 :title "JSON" :img "img/json.png" :ext ".json" :content-type "application/javascript" :template "default"}
                      {:order 6 :title "Yaml" :img "img/yaml.png" :ext ".yml" :content-type "application/x-yaml" :template "default"}
                      {:order 7 :title "EDN" :img "img/edn.png" :ext ".edn" :content-type "application/edn" :template "default"}]}))

(defn api-url
  []
  (if (s/includes? (.-href (.-location js/window)) "http://localhost:3449") ;; figwheel
    "http://localhost:3080/api"
    (str (.-origin (.-location js/window)) "/api")))

(defn clean-response [content-type response]
  (if (or (= content-type "application/markdown")
          (= content-type "application/x-yaml")
          (= content-type "text/clojure")
          (= content-type "application/edn")
          (= content-type "application/javascript"))
    (-> response
        (s/replace  " " "&nbsp;")
        (s/replace "\n" "<br />"))
    (last (s/split response #"<body.*>"))))

(defn by-content-type-and-template [content-type template g]
  (and (= (:content-type g) content-type)
       (= (:template g) template)))

(defn update-stats [{:keys [content-type template] :as generator} state]
  (->> state
       (remove (partial by-content-type-and-template content-type template))
       (cons (update-in generator [:count] inc))))

(defn generate-handler
  [{:keys [ext content-type template] :as generator} ev] 
   (when (= ev.target.status 200)
     (swap! app-state assoc :downloadable {:ext ext :template template :content-type content-type :data (b64/encodeString ev.currentTarget.responseText)})
     (->> ev.currentTarget.responseText
          (clean-response content-type)
          (swap! app-state assoc :preview)))
   (when (not= ev.target.status 200)
     (swap! app-state assoc :preview "There was a problem generating the documentation."))
   (swap! app-state assoc :error? (not= ev.target.status 200))
   (swap! app-state assoc :loading? false)
   (swap! app-state update-in [:stats :count] inc)
   (swap! app-state update :generators (partial update-stats generator)))

(defn generate 
  [generator app on-generated e]
  (let [{:keys [url]} app]
    (swap! app-state assoc :loading? true)
    (doto
      (new js/XMLHttpRequest)
      (.open "POST" (str (api-url) "/documentation"))
      (.setRequestHeader "Accept" (:content-type generator))
      (.setRequestHeader "Content-Type" "application/x-www-form-urlencoded")
      (.addEventListener "load" (comp on-generated (partial generate-handler generator)))
      (.addEventListener "error" (partial generate-handler generator))
      (.send (str "url=" url "&template=" (:template generator))))))

(defn generator 
  [app g]
  (let [{:keys [title content-type img template description] 
         :or {description (str "Generate " title " with " template " template") img "img/swagger.png"}} g
        counter (:count g)]
    (if (:coming-soon? g)
      [:div.generator.coming {:id title :key title}
       [:img {:src "img/s.png" :title (str title " Coming Soon") :width "80px" :height "80px"}]
       [:div (str title)]]
      [:div.generator {:id title :key title :on-click (partial generate g app (fn [_]))}
       [:img {:src img :title description :width "80px" :height "80px"}]
       [:div
        (str title)
        [:div.count {:title (str "Used " counter " times")}
         (cond
           (> counter 99999) "100k"
           (> counter 9999) "10k+"
           (> counter 999) "1k+"
           :else counter)]]])))

(defn url-input
  [{:keys [url loading?]}]
  [:input#url 
   (let [attributes (if loading? {:disabled true} {})]
     (merge attributes 
            {:type "text" 
             :placeholder "Enter swagger.json url"
             :value url
             :on-change (fn [ev] 
                          (let [entered-url (.-value (.-target ev))
                                generators-visible (clova/valid? url-validation {:url entered-url})]
                            (swap! app-state assoc :url entered-url)
                            (swap! app-state assoc :generators-visible? generators-visible)))}))])

(defn expand-preview
  [e]
  (swap! app-state update :expanded? not))

(defn preview-pane
  [{:keys [url preview loading? error? expanded? downloadable]}]
  (let [content-type (:content-type downloadable)
        template (:template downloadable)]
    (if preview 
      [:div 
       [:div#preview-header 
        {:on-click expand-preview}
        [:h3 (if loading?  "Loading..." (if error? "Error Loading" "Preview"))]]
       (if expanded? 
         [:div#preview.expanded [:div {:style {:width "10000px"} :dangerouslySetInnerHTML {:__html preview}}]]
         [:div#preview.collapsed [:div {:style {:width "10000px"} :dangerouslySetInnerHTML {:__html preview}}]])
       [:div#preview-footer 
        [:div 
         {:on-click expand-preview}
         (if expanded? 
           [:h3 "Hide"]
           [:h3 "Show More"])]
        [:a {:href (str (api-url) "/documentation?url=" (.encodeURIComponent js/window url) "&content-type=" (.encodeURIComponent js/window content-type) "&template=" template)} "View"]
        " | "
        [:a {:download (str "swaggerdown" (:ext downloadable)) 
             :href (str "data:" (:content-type downloadable) ";base64," (:data downloadable))} "Download"]]])))

(defn api-pane
  [{:keys [preview url downloadable]}]
  (if preview 
    [:div.outro.blue
     [:img#help {:src "img/api.png" :width "120px" :height "120px"}]
     [:h3 "Use the API!"]
     [:p "If you need to convert your OpenAPI or Swagger specification in a more automated fashion or don't want to use this user interface then you can make use of the api."]
     [:p]
     [:div#preview-header 
      [:h3 "Terminal"]]
     [:div#preview.collapsed " curl -X POST -v -H \"Accept: " (:content-type downloadable) "\"  " (api-url) "/documentation -H \"Content-Type: application/x-www-form-urlencoded\" -d \"url=" url "&template=" (:template downloadable) "\""]
     [:div#preview-footer]]))

(defn add-stats [stats g]
  (assoc 
    g 
    :count
    (-> (fn [{:keys [contenttype template]}]
          (and (= contenttype (:content-type g))
               (= template (:template g))))
        (filter stats)
        first
        :count)))

(defn stats-handler [{:keys [generators]} ev] 
  (let [mapper #(select-keys % [:contenttype :template :count])
        stats (map mapper (read-string ev.currentTarget.responseText))
        all (reduce + (map :count stats))
        generators-and-stats (map (partial add-stats stats) generators)]
    (swap! app-state assoc-in [:stats :count] all)
    (swap! app-state assoc :generators generators-and-stats)))

(defn stats [app]
  (doto
      (new js/XMLHttpRequest)
      (.open "GET" (str (api-url) "/stats"))
      (.addEventListener "load" (partial stats-handler app))
      (.send)))

(defn stats-pane
  [app]
  (let [counter (get-in app [:stats :count])]
    [:div.outro.dark-grey
     [:img#counter {:src "img/cog.png" :width "120px" :height "120px"}]
     [:h3 (str counter " Generations!")]
     [:p (str "Swaggerdown has converted Swagger and OpenAPI specifications " counter " times!")]]))

(defn generators
  [app]
  (if (:generators-visible? app)
    (map (partial generator app) (sort-by :order (:generators app)))
    [:div.error.blue 
     [:img {:src "img/error.png" :height "90px" :width "90px"}]
     [:h4 "Enter a valid swagger json url to generate documentation!"]]))

(defn start 
  [app]
  [:div
   [:div#main.blue
    (url-input @app)
    [:div#generators-container 
     [:div#generators 
      (generators @app)]]
    (preview-pane @app)
    (api-pane @app)
    (stats-pane @app)]])

(defn render [_] 
  (stats @app-state)
  (reagent/render-component [start app-state]
                            (.getElementById js/document "app")))

(-> (:generators @app-state)
    first
    (generate @app-state render nil))

