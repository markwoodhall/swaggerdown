(ns swaggerdown.client
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [clojure.string :as s]
            [cljs.core.async :refer [<!]]
            [cljs.reader :refer [read-string]]
            [clova.core :as clova]
            [goog.crypt.base64 :as b64]
            [reagent.core :as reagent :refer [atom]]
            [swaggerdown.components :as c]
            [swaggerdown.generators :refer [by-content-type-and-template]]))

(enable-console-print!)

(defonce url-validation [:url clova/required? clova/url?])

(defn api-url
  []
  (if (s/includes? (.-href (.-location js/window)) "http://localhost:3449") ;; figwheel
    "http://localhost:3080/api"
    (str (.-origin (.-location js/window)) "/api")))

(defonce app-state 
  (atom {:api-url (api-url)
         :url "http://petstore.swagger.io/v2/swagger.json"
         :stats {:count 10000}
         :generators-visible? true
         :expanded? false}))

(defn update-stats [{:keys [content-type template] :as generator} state]
  (->> state
       (remove (partial by-content-type-and-template content-type template))
       (cons (update-in generator [:count] inc))))

(defn generate-handler
  [{:keys [ext content-type template] :as generator} ev] 
   (when (= ev.target.status 200)
     (let [data 
           (try 
             (b64/encodeString ev.currentTarget.responseText)
             (catch :default e))]
       (swap! app-state assoc :downloadable {:ext ext :template template :content-type content-type :data data}))
     (->> ev.currentTarget.responseText
          (swap! app-state assoc :preview)))
   (when (not= ev.target.status 200)
     (swap! app-state assoc :preview "There was a problem generating the documentation."))
   (swap! app-state assoc :error? (not= ev.target.status 200))
   (swap! app-state assoc :loading? false)
   (swap! app-state update-in [:stats :count] inc)
   (swap! app-state update :generators (partial update-stats generator)))

(defn generate 
  [generator app on-generated]
  (let [{:keys [api-url url]} app]
    (swap! app-state assoc :loading? true)
    (doto
      (new js/XMLHttpRequest)
      (.open "POST" (str api-url "/documentation"))
      (.setRequestHeader "Accept" (:content-type generator))
      (.setRequestHeader "Content-Type" "application/x-www-form-urlencoded")
      (.addEventListener "load" (comp on-generated (partial generate-handler generator)))
      (.addEventListener "error" (partial generate-handler generator))
      (.send (str "url=" url "&template=" (:template generator))))))

(defn url-input
  [{:keys [url loading?]}]
  [:input#url 
   (let [attributes (if loading? {:disabled true} {})]
     (merge attributes 
            {:type "text" 
             :placeholder "Enter url to swagger json or yaml"
             :value url
             :on-change (fn [ev] 
                          (let [entered-url (.-value (.-target ev))
                                generators-visible (clova/valid? url-validation {:url entered-url})]
                            (swap! app-state assoc :url entered-url)
                            (swap! app-state assoc :generators-visible? generators-visible)))}))])

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

(defn generators-handler [ev] 
  (swap! app-state assoc :generators (read-string ev.currentTarget.responseText)))

(defn generators [{:keys [api-url]} on-load]
  (doto
      (new js/XMLHttpRequest)
      (.open "GET" (str api-url "/generators"))
      (.addEventListener "load" (comp on-load generators-handler))
      (.send)))

(defn stats [{:keys [api-url] :as app} on-load]
  (doto
      (new js/XMLHttpRequest)
      (.open "GET" (str api-url "/stats"))
      (.addEventListener "load" (comp on-load (partial stats-handler app)))
      (.send)))

(defn highlight [_]
  (when (exists? js/hljs) 
    (js/setTimeout #(.highlightBlock js/hljs (.getElementById js/document "code")) 400)))
  
(defn start 
  [app]
  [:div
   [:div#main.blue
    (url-input @app)
    [:div#generators-container 
     [:div#generators 
      (c/generators @app)]]
    (c/preview-pane @app)
    (c/api-pane @app)
    (c/stats-pane @app)]])

(defn render []
  (reagent/render-component [start app-state]
                            (.getElementById js/document "app")))

(generators 
  @app-state
  (fn [_]
    (stats 
      @app-state 
      (fn [_] (-> (:generators @app-state)
                  first
                  (generate @app-state (comp highlight render)))))))

(go-loop 
  []
  (let [{:keys [event data]} (<! c/event-chan)]
    (case event
      :generator-clicked (generate data @app-state highlight)
      :preview-clicked (do
                         (swap! app-state update :expanded? not)
                         (highlight data))))
  (recur))
