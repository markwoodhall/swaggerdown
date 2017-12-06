(ns dev
  (:require [com.stuartsierra.component :as component]
            [swaggerdown.app :refer [new-system configure]]
            [reloaded.repl :refer [system init start stop go reset reset-all]]))

(reloaded.repl/set-init! #(configure (new-system) :dev))
