# Swaggerdown

[![Logo](https://raw.githubusercontent.com/markwoodhall/swaggerdown/master/resources/public/img/s.png)](https://swaggerdown.io)

## Overview

Swaggerdown is an open source tool for converting OpenAPI specifications to static 
documentation formats such as markdown. 

You can use it for free at [swaggerdown.io](https://swaggerdown.io). Common use cases include:

1. Generate static markdown from a OpenAPI v2 specification. [Example](https://github.com/markwoodhall/swaggerdown/blob/master/samples/markdown.md).
2. Generate static html from a OpenAPI v2 specification. [Example](http://www.swaggerdown.io/api/documentation?url=http%3A%2F%2Fpetstore.swagger.io%2Fv2%2Fswagger.json&content-type=text%2Fhtml&template=fractal)
3. Generate yaml from a from OpenAPI v2 specification. [Example](https://github.com/markwoodhall/swaggerdown/blob/master/samples/yaml.yml).
  + It is easy to import yaml into other api documentation tools such as [apiary](http://apiary.io).

## Thanks

Swaggerdown is built on top of a number of Clojure and ClojureScript open source projects.

1. [yada](https://github.com/juxt/yada)
2. [Reagent]()
3. [clova](http://github.com/markwoodhall/clova)
4. [marge](http://github.com/markwoodhall/marge)
5. [io.forward/yaml](https://github.com/owainlewis/yaml)
6. [markdown-clj](https://github.com/yogthos/markdown-clj)


## License

Copyright © 2017 Mark Woodhall

https://opensource.org/licenses/MIT
