(ns swaggerdown.app
  (:require [aero.core :refer [read-config]]
            [com.stuartsierra.component :as component]
            [swaggerdown.logger :refer [new-logger info]]
            [swaggerdown.selmer :refer [new-selmer]]
            [swaggerdown.resources :refer [documentation home access-control]]
            [swaggerdown.ravendb :refer [new-ravendb]]
            [yada.yada :refer [listener resource as-resource redirect]]
            [yada.resources.classpath-resource :refer [new-classpath-resource]])
  (:gen-class))

(defrecord Server [port features logger ravendb]
  component/Lifecycle
  (start [this]
    (info logger "Starting server on port %s" port)
    (assoc
     this
     :listener
     (listener
      ["/"
       [["api/"
         [["documentation"
           (-> ["http://petstore.swagger.io/v2/swagger.json" "default"]
               (documentation ravendb logger)
               (merge access-control)
               resource)]]]
        ["ping" (as-resource {:status :ok})]
        ["js" (new-classpath-resource "public/js")]
        ["img" (new-classpath-resource "public/img")]
        ["css" (new-classpath-resource "public/css")]
        ["index.html" (resource (home features))]
        ["" (resource (home features))]]]
      {:port port})))
  (stop [{:keys [port listener] :as this}]
    (info logger "Stopping server on port %s" port)
    (if-let [close (:close listener)]
      (close))
    (assoc this :listener nil)))

(defn new-server []
  (component/using (map->Server {}) [:features :logger :ravendb]))

(defn new-system []
  (component/system-map
   :server (new-server)
   :selmer (component/using (new-selmer) [:logger])
   :ravendb (component/using (new-ravendb) [:logger])
   :logger  (new-logger)
   :features {}))

(defn configure
  [system profile]
  (let [config (read-config (clojure.java.io/resource "config.edn") {:profile profile})]
    (merge-with merge system config)))

(defn -main
  [& args]
  (let [system (configure (new-system) :prod)]
    (component/start system)
    (loop [input (read-line)]
      (if (= input "Q")
        (component/stop-system system)
        (recur (read-line))))))
