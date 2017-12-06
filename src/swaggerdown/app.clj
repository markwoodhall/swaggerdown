(ns swaggerdown.app
  (:require [aero.core :refer [read-config]]
            [com.stuartsierra.component :as component]
            [swaggerdown.resources :refer [documentation home access-control]]
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
                     (-> ["http://petstore.swagger.io/v2/swagger.json" "default"] 
                         documentation
                         (merge access-control)
                         resource)]]]
                  ["ping" (as-resource {:status :ok})]
                  ["js" (new-classpath-resource "public/js")]
                  ["img" (new-classpath-resource "public/img")]
                  ["css" (new-classpath-resource "public/css")]
                  ["index.html" (resource (home this))]
                  ["" (resource (home this))]
                 ]]
                 {:port port})]
      (assoc this :server srv)))
  (stop [{:keys [port server] :as this}]
    (println "Stopping server on port" port)
    (if-let [close (:close server)]
      (close))
    (assoc this :server nil)))

(defn new-system
  []
  (component/system-map
    :server (map->Server {})))

(defn configure
  [system profile]
  (let [config (read-config (clojure.java.io/resource "config.edn") {:profile profile})]
    (merge-with merge system config)))

(defn -main
  [& args]
  (let [system (component/start 
                 (-> (new-system)
                     (configure :prod)))]
  (loop [input (read-line)]
    (if (= input "Q")
      (component/stop-system system)
      (recur (read-line))))))
