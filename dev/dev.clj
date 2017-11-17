(ns dev
  (:require [com.stuartsierra.component :as component]
            [swaggerdown.app :refer [new-system]]
            [swaggerdown.swagger :refer [swagger]]
            [swaggerdown.coercion :refer [->markdown markdown->str]]
            [reloaded.repl :refer [system init start stop go reset reset-all]]))

(reloaded.repl/set-init! #(new-system {:port 3080}))
