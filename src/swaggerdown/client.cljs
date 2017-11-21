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
         :generators [{:title "HTML" :ext ".html" :content-type "text/html"}
                      {:title "Markdown" :ext ".md" :content-type "application/markdown"}
                      {:title "Yaml" :ext ".yml" :content-type "application/x-yaml"}
                      {:title "EDN" :ext ".edn" :content-type "application/edn"}
                      {:title "Fractal HTML" :ext ".html" :content-type "application/html" :coming-soon? true}
                      {:title "Api Blueprint" :ext ".apib" :content-type "application/mson" :coming-soon? true}
                      {:title "PDF" :ext ".pdf" :content-type "application/pdf" :coming-soon? true}
                      {:title "Ascii Doc" :ext ".adoc" :content-type "text/asciidoc" :coming-soon? true}]}))

(defn api-url
  []
  (if (s/includes? (.-href (.-location js/window)) "http://localhost:3449") ;; figwheel
    "http://localhost:3080/api"
    (str (.-origin (.-location js/window)) "/api")))

(.-href (.-location js/window))

(defn generate-handler
  [ext content-type ev] 
  (when (= ev.target.status 200)
    (swap! app-state assoc :downloadable {:ext ext :content-type content-type :data (b64/encodeString ev.currentTarget.responseText)})
    (->> (if (or (= content-type "application/markdown")
                 (= content-type "application/x-yaml"))
           (-> ev.currentTarget.responseText
               (s/replace  " " "&nbsp;")
               (s/replace "\n" "<br />"))
           ev.currentTarget.responseText)
         (swap! app-state assoc :preview)))
  (swap! app-state assoc :error? (not= ev.target.status 200))
  (swap! app-state assoc :loading? false))

(defn generate [generator app e]
  (let [{:keys [url]} app
        {:keys [ext content-type]} generator]
    (swap! app-state assoc :loading? true)
    (doto
      (new js/XMLHttpRequest)
      (.open "POST" (str (api-url) "/documentation"))
      (.setRequestHeader "Accept" content-type)
      (.setRequestHeader "Content-Type" "application/x-www-form-urlencoded")
      (.addEventListener "load" (partial generate-handler ext content-type))
      (.addEventListener "error" (partial generate-handler ext content-type))
      (.send (str "url=" url)))))

(defn generator 
  [app g]
  (let [{:keys [title content-type]} g]
    (if (:coming-soon? g)
      [:div.generator.coming {:id title :key title}
       [:img {:src "img/s.png" :title (str title " Coming Soon") :width "80px" :height "80px"}]
       [:div (str title)]]
      [:div.generator {:id title :key title :on-click (partial generate g app)}
       [:img {:src "img/swagger.png":title (str "Generate " title " Coming Soon")  :width "80px" :height "80px"}]
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
  [{:keys [preview loading? error? expanded? downloadable]}]
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
      [:a {:download (str "swaggerdown" (:ext downloadable)) 
           :href (str "data:" (:content-type downloadable) ";base64," (:data downloadable))} "Download"]]]))

(defn api-pane
  [{:keys [preview url downloadable]}]
  (if preview 
    [:div.outro.grey
     [:img#help {:src "img/api.png" :width "120px" :height "120px"}]
     [:h3 "Use the API!"]
     [:p "If you need to convert your swagger json in a more automated fashion or don't want to use this interface then you can make use of the api."]
     [:p]
     [:div#preview-header 
      [:h3 "Terminal"]]
     [:div#preview.collapsed " curl -X POST -v -H \"Accept: " (:content-type downloadable) "\"  " (api-url) "/documentation -H \"Content-Type: application/x-www-form-urlencoded\" -d \"url=" url "\""]
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
   [:div#main
    (url-input @app)
    [:div#generators-container 
     [:div#generators 
      (generators @app)]]
    (preview-pane @app)
    (api-pane @app)]])

(generate (first (:generators @app-state)) @app-state nil)

(reagent/render-component [start app-state]
                          (. js/document (getElementById "app")))

