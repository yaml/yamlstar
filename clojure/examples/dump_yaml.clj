(require '[clojure.data.json :as json])
(require '[yamlstar.core :as yaml])

(print (yaml/dump (json/read-str (slurp *in*))))
