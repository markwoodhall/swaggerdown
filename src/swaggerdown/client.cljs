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
                      {:title "Api Blueprint" :ext ".apib" :content-type "application/mson" :coming-soon? true}
                      {:title "PDF" :ext ".pdf" :content-type "application/pdf" :coming-soon? true}
                      {:title "Ascii Doc" :ext ".adoc" :content-type "text/asciidoc" :coming-soon? true}]}))

(defn api-url
  []
  (if (s/includes? (.-href (.-location js/window)) "http://localhost:3449") ;; figwheel
    "http://localhost:3080/api"
    "/api"))

(.-href (.-location js/window))

(defn generate-handler
  [content-type ev] 
  (when (= ev.target.status 200)
    (swap! app-state assoc :downloadable {:content-type content-type :data (b64/encodeString ev.currentTarget.responseText)})
    (->> (if (or (= content-type "application/markdown")
                 (= content-type "application/x-yaml"))
           (-> ev.currentTarget.responseText
               (s/replace  " " "&nbsp;")
               (s/replace "\n" "<br />"))
           ev.currentTarget.responseText)
         (swap! app-state assoc :preview))
    (swap! app-state assoc :error? (not= ev.target.status 200))
    (swap! app-state assoc :loading? false)))

(defn generate [generator app e]
  (let [{:keys [url]} app
        {:keys [content-type]} generator]
    (swap! app-state assoc :loading? true)
    (doto
      (new js/XMLHttpRequest)
      (.open "POST" (str (api-url) "/documentation"))
      (.setRequestHeader "Accept" content-type)
      (.setRequestHeader "Content-Type" "application/x-www-form-urlencoded")
      (.addEventListener "load" (partial generate-handler content-type))
      (.send (str "url=" url)))))

(defn generator 
  [app g]
  (let [{:keys [title content-type]} g]
    (if (:coming-soon? g)
      [:div.generator.coming {:id title :key title}
       [:img {:src "img/s.png" :width "80px" :height "80px"}]
       [:div (str title)]]
      [:div.generator {:id title :key title :on-click (partial generate g app)}
       [:img {:src "img/swagger.png" :width "80px" :height "80px"}]
       [:div
        (str title)]])))

(defn what-is-swagger
  []
  [:div#intro
   [:img.swagger {:src "img/swagger.png" :width "120px" :height "120px"}]
   [:h3 "What is Swagger?"]
   [:p "\"Swagger is an open source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful Web services.\""]
   [:p "\"While most users identify Swagger by the Swagger UI tool, the Swagger toolset includes support for automated documentation, code generation, and test case generation.\""]])

(defn how-it-works
  []
  [:div.outro.grey
   [:img#help {:src "img/s.png" :width "120px" :height "120px"}]
   [:h3 "How does it work?"]
   [:p "Swaggerdown parses your swagger.json and generates documentation in a static form, this is particularly useful when you want to distribute something physical to complement the Swagger UI."]
   [:p "You can see an exmaple for the Swagger Pet Store below, change the url to a swagger.json definition and click on one of the generators to preview the documentation. Click \"Download\" to download the documentation."]])

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
                            (println entered-url)
                            (println generators-visible)
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
      [:a {:name "swaggerdown" :href (str "data:" (:content-type downloadable) ";base64," (:data downloadable))} "Download"]]]))

(defn developer
  []
  [:div.outro.blue
   [:img#profile {:src "img/profile.jpeg" :width "120px" :height "120px"}]
   [:h3 "Who created this?"]
   [:p 
    [:a {:href "http://markw.xyz"} "Mark"] 
    " is a software developer living in Birmingham, England. During the day Mark develops software for a logistics and supply chain management company. In his spare time he enjoys running."]
   [:img#cog {:src "img/cog.png" :width "120px" :height "120px"}]
   [:h3 "Other projects!"] 
   [:p [:a {:href "http://github.com/markwoodhall/clova"} "clova"] " and "
    [:a {:href "http://github.com/markwoodhall/marge"} "marge"] " are validation and markdown libraries for Clojure and ClojureScript and are both used by this application."]])

(defn open-source
  []
  [:div.outro.light-grey
   [:svg {:aria-hidden "true"  :height "114" :view-box "0 0 16 16" :width "114"}
    [:path {:fill-rule "evenodd" :d "M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0 0 16 8c0-4.42-3.58-8-8-8z"}]]
   [:h3 "Open Source"]
   [:p "Swaggerdown is open source and available on " 
    [:a {:href "http://github.com/markwoodhall/swaggerdown"} "GitHub"] 
    ". It is made up of this client UI app and an API. It is developed using Clojure and ClojureScript."]
   [:p ""]
   [:a {:href "https://clojure.org/"} 
    [:img.clojure {:src "img/clojure.png" :title "Clojure" :alt "Clojure" :width "80px" :height "80px"}]]
   [:a {:href "https://clojurescript.org/"} 
    [:img.clojurescript {:src "img/cljs.png" :title "ClojureScript" :alt "ClojureScript" :width "82px" :height "82px"}]]
   [:a {:href "https://swagger.io"} 
    [:img.swagger {:src "img/swagger.png" :title "Swagger" :alt "Swagger" :width "82px" :height "82px"}]]])

(defn generators
  [app]
  (if (:generators-visible? app)
    (map (partial generator app) (:generators app))
    [:div.outro.grey [:h4 "Enter a valid swagger json url to generate documentation!"]]))

(defn start 
  [app]
  (let [{:keys [title tagline url loading? error? expanded? preview]} @app]
    [:div
     [:div#bar 
      [:a {:href "/"} 
       [:img#logo {:alt title :title title :src "img/s.png" :width "50px" :height "50px"}]]]
     [:div.header 
      [:h3 tagline]]
     (what-is-swagger)     
     (how-it-works)
     [:div#main
      (url-input @app)
      [:div#generators 
       (generators @app)]
      (preview-pane @app)]
     (developer)
     (open-source)]))

(reagent/render-component [start app-state]
                          (. js/document (getElementById "app")))

(generate (first (:generators @app-state)) @app-state nil)
