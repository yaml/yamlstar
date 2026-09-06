(ns yamlstar.plugin.shared-host-java
  "GraalVM dynamic shared-plugin host integration."
  (:require [yamlstar.plugin :as plugin]
            [yamlstar.plugin.shared :as shared]))

(defn- native-image?
  []
  (some? (System/getProperty "org.graalvm.nativeimage.imagecode")))

(defn- parse
  [api name input options]
  (let [[status output]
        (seq (yamlstar.plugin.SharedPluginHost/parse
              api name input options))]
    [(Long/parseLong status) output]))

(defn install!
  "Install the GraalVM shared-library loader in a native image."
  []
  (when (native-image?)
    (plugin/set-event-source-loader!
     (shared/make-loader
      (fn [api name install?]
        (yamlstar.plugin.SharedPluginHost/manifest api name install?))
      parse))))
