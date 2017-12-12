(ns swaggerdown.selmer
  (:require [com.stuartsierra.component :as component]
            [selmer.parser :refer [cache-on! cache-off!]]
            [swaggerdown.logger :refer [debug]]))

(defrecord Selmer [template-caching? logger]
  component/Lifecycle
  (start [this]
    (debug logger "Starting selmer templating with template-caching? %s" template-caching?)
    (if template-caching? 
      (cache-on!)
      (cache-off!))
    this)
  (stop [this]
      this))

(defn new-selmer []
  (map->Selmer {}))
