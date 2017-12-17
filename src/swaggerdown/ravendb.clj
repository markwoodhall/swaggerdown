(ns swaggerdown.ravendb
  (:require [com.stuartsierra.component :as component]
            [swaggerdown.logger :refer [debug]]
            [clj-ravendb.client :as rdb]))

(defrecord RavenDb [url oauth-url database api-key client logger]
  component/Lifecycle
  (start [this]
    (debug logger "Starting ravendb client for database %s at url %s" database url)
    (let [config {:enable-oauth? true :oauth-url oauth-url :api-key api-key}
          client (rdb/client url database config)]
      (rdb/put-index! client {:index "CountDocumentGenerations"
                              :from "DocumentationGenerated"
                              :select [[:count 1]]
                              :project [[:count [:Sum :count]]]})
      (assoc this :client client)))
  (stop [this]
    (debug logger "Stopped ravendb client for database %s at url %s" database url)
    (dissoc this :client)))

(defn new-ravendb []
  (map->RavenDb {}))
