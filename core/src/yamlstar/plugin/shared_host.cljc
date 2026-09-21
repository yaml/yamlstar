(ns yamlstar.plugin.shared-host
  "Glojure dynamic shared-plugin host integration."
  (:require [yamlstar.plugin :as plugin]
            [yamlstar.plugin.shared :as shared]))

(defn- require-glojure-runtime
  []
  #?(:glj nil
     :clj (throw (ex-info
                  "Shared plugins require a native YAMLStar host"
                  {}))))

(defn- manifest
  [api name install?]
  (require-glojure-runtime)
  #?(:glj
     (let [[text error]
           (github.com:yaml:yamlstar:internal:goyamlparser:pluginloader.Manifest
            api name install?)]
       (if (nil? error) text (throw error)))
     :clj nil))

(defn- transform
  [api name input options]
  (require-glojure-runtime)
  #?(:glj
     (let [[text status error]
           (github.com:yaml:yamlstar:internal:goyamlparser:pluginloader.Transform
            api name input options)]
       (if (nil? error)
         [status text]
         (throw error)))
     :clj nil))

(defn install!
  "Install the Glojure shared-library JSON-comments loader."
  []
  (require-glojure-runtime)
  (plugin/set-json-comments-loader!
   (shared/make-loader manifest transform)))
