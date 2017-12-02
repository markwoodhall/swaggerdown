(ns swaggerdown.client
    (:require [reagent.core :as reagent :refer [atom]]
              [clojure.string :as s]
              [goog.crypt.base64 :as b64]
              [clova.core :as clova]))

(enable-console-print!)

(defonce url-validation [:url clova/required? clova/url?])

(defonce app-state 
  (atom {:title "Swaggerdown" 
         :tagline "Generate documentation from your swagger!"
         :url "http://petstore.swagger.io/v2/swagger.json"
         :generators-visible? true
         :expanded? false
         :generators [{:title "HTML" :description "Generate HTML with no formatting" :img "img/html.png" :ext ".html" :content-type "text/html" :template "default"}
                      {:title "Fractal" :description "Generate HTML using the fractal template" :img "img/fractal.png" :ext ".html" :content-type "text/html" :template "fractal"}
                      {:title "Fractal Red" :description "Generate HTML using a red fractal template" :img "img/fractal-red.png" :ext ".html" :content-type "text/html" :template "fractal-red"}
                      {:title "Markdown" :img "img/markdown.png" :ext ".md" :content-type "application/markdown" :template "default"}
                      {:title "Yaml" :img "img/yaml.png" :ext ".yml" :content-type "application/x-yaml" :template "default"}
                      {:title "EDN" :img "img/edn.png" :ext ".edn" :content-type "application/edn" :template "default"}]}))

(defn api-url
  []
  (if (s/includes? (.-href (.-location js/window)) "http://localhost:3449") ;; figwheel
    "http://localhost:3080/api"
    (str (.-origin (.-location js/window)) "/api")))

(defn generate-handler
  [ext content-type template ev] 
  (when (= ev.target.status 200)
    (swap! app-state assoc :downloadable {:ext ext :template template :content-type content-type :data (b64/encodeString ev.currentTarget.responseText)})
    (->> (if (or (= content-type "application/markdown")
                 (= content-type "application/x-yaml")
                 (= content-type "text/clojure")
                 (= content-type "application/edn"))
           (-> ev.currentTarget.responseText
               (s/replace  " " "&nbsp;")
               (s/replace "\n" "<br />"))
           ev.currentTarget.responseText)
         (swap! app-state assoc :preview)))
  (swap! app-state assoc :error? (not= ev.target.status 200))
  (swap! app-state assoc :loading? false))

(defn generate [generator app e]
  (let [{:keys [url]} app
        {:keys [ext content-type template]} generator]
    (swap! app-state assoc :loading? true)
    (doto
      (new js/XMLHttpRequest)
      (.open "POST" (str (api-url) "/documentation"))
      (.setRequestHeader "Accept" content-type)
      (.setRequestHeader "Content-Type" "application/x-www-form-urlencoded")
      (.addEventListener "load" (partial generate-handler ext content-type template))
      (.addEventListener "error" (partial generate-handler ext content-type template))
      (.send (str "url=" url)))))

(defn generator 
  [app g]
  (let [{:keys [title content-type img template description] 
         :or {description (str "Generate " title " with " template " template") img "img/swagger.png"}} g]
    (if (:coming-soon? g)
      [:div.generator.coming {:id title :key title}
       [:img {:src "img/s.png" :title (str title " Coming Soon") :width "80px" :height "80px"}]
       [:div (str title)]]
      [:div.generator {:id title :key title :on-click (partial generate g app)}
       [:img {:src img :title description  :width "80px" :height "80px"}]
       [:div
        (str title)]])))

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
     [:p "If you need to convert your swagger json in a more automated fashion or don't want to use this interface then you can make use of the api."]
     [:p]
     [:div#preview-header 
      [:h3 "Terminal"]]
     [:div#preview.collapsed " curl -X POST -v -H \"Accept: " (:content-type downloadable) "\"  " (api-url) "/documentation -H \"Content-Type: application/x-www-form-urlencoded\" -d \"url=" url "&template=" (:template downloadable) "\""]
     [:div#preview-footer]]))

(defn generators
  [app]
  (if (:generators-visible? app)
    (map (partial generator app) (:generators app))
    [:div.error.grey 
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
    (api-pane @app)]])

(generate (first (:generators @app-state)) @app-state nil)

(reagent/render-component [start app-state]
                          (. js/document (getElementById "app")))

