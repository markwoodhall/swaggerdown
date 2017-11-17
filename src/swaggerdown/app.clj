(ns swaggerdown.app
  (:require [com.stuartsierra.component :as component]
            [swaggerdown.resources :refer [documentation access-control]]
            [yada.yada :refer [listener resource as-resource redirect]]
            [yada.resources.classpath-resource :refer [new-classpath-resource]])
  (:gen-class))

(defrecord Server [port]
  component/Lifecycle
  (start [{:keys [port] :as this}]
    (println "Starting server on port" port)
    (let [srv (listener
                ["/"
                 [ 
                  ["api/" 
                   [
                    ["documentation" 
                     (-> ["http://petstore.swagger.io/v2/swagger.json"] 
                         documentation
                         (merge access-control)
                         resource)]]]
                  ["js" (new-classpath-resource "public/js")]
                  ["img" (new-classpath-resource "public/img")]
                  ["css" (new-classpath-resource "public/css")]
                  ["" (as-resource (clojure.java.io/resource "public/index.html"))]
                 ]]
                 {:port port})]
      (assoc this :server srv)))
  (stop [{:keys [port server] :as this}]
    (println "Stopping server on port" port)
    (if-let [close (:close server)]
      (close))
    (assoc this :server nil)))

(defn new-system
  [config]
  (let [{:keys [port]} config]
    (component/system-map
      :server (map->Server {:port port}))))

(defn -main
  [& args]
  (let [system (new-system {:port (Integer/parseInt (System/getenv "PORT"))})]
    (component/start system)
    (loop [input (read-line)]
      (if (= input "Q")
        (component/stop-system system)
        (recur (read-line))))))
