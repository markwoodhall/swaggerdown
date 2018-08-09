(ns swaggerdown.components
  (:require [cljs.core.async :refer [put! chan]]))

(defonce event-chan (chan))

(defn generator 
  [app g]
  (let [{:keys [title content-type img template description] 
         :or {description (str "Generate " title " with " template " template") img "img/swagger.png"}} g
        counter (:count g)]
    (if (:coming-soon? g)
      [:div.generator.coming {:id title :key title}
       [:img {:src "img/s.png" :title (str title " Coming Soon") :width "80px" :height "80px"}]
       [:div (str title)]]
      [:div.generator {:id title :key title :on-click (fn [_] (put! event-chan {:event :generator-clicked :data g}))}
       [:img {:src img :title description :width "80px" :height "80px"}]
       [:div
        (str title)
        [:div.count {:title (str "Used " counter " times")}
         (cond
           (> counter 99999) "100k"
           (> counter 9999) "10k+"
           (> counter 999) "1k+"
           :else counter)]]])))

(defn generators
  [app]
  (if (:generators-visible? app)
    (map (partial generator app) (sort-by :order (:generators app)))
    [:div.error.blue 
     [:img {:src "img/error.png" :height "90px" :width "90px"}]
     [:h4 "Enter a valid swagger json/yaml url to generate documentation!"]]))

(defn api-pane
  [{:keys [api-url preview url downloadable]}]
  (if preview 
    [:div.outro.blue
     [:img#help {:src "img/api.png" :width "120px" :height "120px"}]
     [:h3 "Use the API!"]
     [:p "If you need to convert your OpenAPI or Swagger specification in a more automated fashion or don't want to use this user interface then you can make use of the api."]
     [:p]
     [:div#preview-header 
      [:h3 "Terminal"]]
     [:div#preview.collapsed " curl -X POST -v -H \"Accept: " (:content-type downloadable) "\"  " api-url "/documentation -H \"Content-Type: application/x-www-form-urlencoded\" -d \"url=" url "&template=" (:template downloadable) "\""]
     [:div#preview-footer]]))

(defn stats-pane
  [app]
  (let [counter (get-in app [:stats :count])]
    [:div.outro.dark-grey
     [:img#counter {:src "img/cog.png" :width "120px" :height "120px"}]
     [:h3 (str counter " Generations!")]
     [:p (str "Swaggerdown has converted Swagger and OpenAPI specifications " counter " times!")]]))

(defn preview-pane
  [{:keys [api-url url preview loading? error? expanded? downloadable]}]
  (let [content-type (:content-type downloadable)
        template (:template downloadable)
        expander (fn [_] (put! event-chan {:event :preview-clicked}))
        language (case content-type
                  "application/edn" "Clojure"
                  "application/x-yaml" "YAML"
                  "application/javascript" "json"
                  "application/markdown" "markdown"
                  "text/html" "html"
                  "nohighlight")]
    (if preview 
      [:div 
       [:div#preview-header 
        {:on-click expander}
        [:h3 (if loading?  "Loading..." (if error? "Error Loading" "Preview"))]]
       (if expanded? 
         [:div#preview.expanded [:pre [:code {:id "code" :class language :style {:width "10000px"}} preview]]]
         [:div#preview.collapsed [:pre [:code {:id "code" :class language :style {:width "10000px"}} preview]]])
       [:div#preview-footer 
        [:div 
         {:on-click expander}
         (if expanded? 
           [:h3 "Hide"]
           [:h3 "Show More"])]
        [:img {:on-load (fn [_] (.alert js/window "hello") ) :style {:display "none"}}]
        [:a {:href (str api-url "/documentation?url=" (.encodeURIComponent js/window url) "&content-type=" (.encodeURIComponent js/window content-type) "&template=" template)} "View"]
        (when (:data downloadable) 
          " | ")
        (when (:data downloadable) 
          [:a {:download (str "swaggerdown" (:ext downloadable)) 
               :href (str "data:" (:content-type downloadable) ";base64," (:data downloadable))} "Download"])]])))
