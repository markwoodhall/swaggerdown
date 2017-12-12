(ns swaggerdown.logger
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]))

(defprotocol Logger (debug [_ s] [_ s a]) (info [_ s] [_ s a]))
(defrecord TimbreLogger [log-level]
  component/Lifecycle Logger
  (start [this]
    this)
  (stop [this]
    this)
  (debug 
    [this s]
    (debug this s nil))
  (debug [_ s a]
    (when (some #{log-level} [:debug])
      (timbre/debug (format s a))))
  (info 
    [this s]
    (info this s nil))
  (info
    [_ s a]
    (when (some #{log-level} [:debug :info])
      (timbre/info (format s a)))))

(defn new-logger []
  (map->TimbreLogger {}))

(info (new-logger) "Hello")
