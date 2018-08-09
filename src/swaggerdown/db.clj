(ns swaggerdown.db
  (:require
   [clj-ravendb.client :as rdb]
   [com.stuartsierra.component :as component]
   [swaggerdown.logger :refer [debug error]]
   [tick.clock :refer [now]]))

(defprotocol Database (record-event! [_ e m]) (events [_ i]))

(defrecord RavenDb [url oauth-url database api-key client logger]
  component/Lifecycle
  Database
  (start [this]
    (debug logger "Starting ravendb client for database %s at url %s" database url)
    (let [config {:enable-oauth? true :oauth-url oauth-url :api-key api-key}
          client (rdb/client url database config)]
      (rdb/put-index! client {:index "CountDocumentGenerationsByType"
                              :from "DocumentationGenerated"
                              :select [[:contenttype [:contenttype]]
                                       [:template [:template]]
                                       [:count 1]]
                              :group [:contenttype :template]
                              :project [[:contenttype [:Key :contenttype]]
                                        [:template [:Key :template]]
                                        [:count [:Sum :count]]]})
      (assoc this :client client)))
  (stop [this]
    (debug logger "Stopped ravendb client for database %s at url %s" database url)
    (dissoc this :client))
  (record-event! [{:keys [client] :as this} e m]
    (debug logger "Recording event in ravendb %s - %s" e m)
    (try
      (rdb/put-document!
       client
       (str (now))
       (merge
        {:timestamp (str (now)) :metadata {:Raven-Entity-Name e}}
        m))
      (catch Exception e
        (error logger e)))
    this)
  (events [{:keys [client] :as this} i]
    (->> {:max-attempts 1}
         (rdb/query-index client {:index i})
         (:results)
         (map :document))))

(defn new-ravendb []
  (map->RavenDb {}))
