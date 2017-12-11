(ns swaggerdown.selmer
  (:require [com.stuartsierra.component :as component]
            [selmer.parser :refer [cache-on! cache-off!]]
            [taoensso.timbre :refer [infof]]))

(defrecord Selmer [template-caching?]
  component/Lifecycle
  (start [{:keys [template-caching?]}]
    (infof "Starting selmer templating with template-caching? %s" template-caching?)
    (if template-caching? 
      (cache-on!)
      (cache-off!)))
  (stop [this]
      this))

(defn new-selmer []
  (map->Selmer {}))
