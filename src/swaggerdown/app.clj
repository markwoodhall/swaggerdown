(ns swaggerdown.app
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [com.stuartsierra.component :as component]
            [swaggerdown.db :refer [new-ravendb]]
            [swaggerdown.logger :refer [info new-logger]]
            [swaggerdown.resources
             :refer
             [access-control documentation generators home stats]]
            [swaggerdown.selmer :refer [new-selmer]]
            [yada.resources.classpath-resource :refer [new-classpath-resource]]
            [yada.yada :refer [as-resource listener resource]]))

(defrecord Server [port features logger db]
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
               (documentation db logger)
               (merge access-control)
               resource)]
          ["stats" (-> (stats db logger)
                       (merge access-control)
                       resource)]
          ["generators" (-> (:generators features)
                            (generators logger)
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

(defn new-system []
  (component/system-map
   :server (component/using (map->Server {}) [:features :logger :db])
   :selmer (component/using (new-selmer) [:logger])
   :db (component/using (new-ravendb) [:logger])
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
