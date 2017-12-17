(ns swaggerdown.logger
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]))

(defprotocol Logger
  (error [_ e])
  (debug [_ s] [_ s a] [_ s a a2])
  (info [_ s] [_ s a])
  (wrap [_ f]))

(defrecord TimbreLogger [log-level]
  component/Lifecycle Logger
  (start [this]
    this)
  (stop [this]
    this)
  (wrap
    [this f]
    (fn [a]
      (try
        (f a)
        (catch Exception e
          (error this e)
          (throw e)))))
  (error
    [this e]
    (timbre/error e))
  (debug
    [this s]
    (debug this s nil))
  (debug [this s a]
    (debug this s a nil))
  (debug [_ s a a2]
    (when (some #{log-level} [:debug])
      (timbre/debug (format s a a2))))
  (info
    [this s]
    (info this s nil))
  (info
    [_ s a]
    (when (some #{log-level} [:debug :info])
      (timbre/info (format s a)))))

(defn new-logger []
  (map->TimbreLogger {}))
