(defproject two-minutes "0.1.0-SNAPSHOT"
  :description ""
  :url "https://github.com/MikeHardIce/2Minutes"
  :license {:name "MIT License"
            :author "MikeHardIce"
            :url "none"
            :year 2021
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [strigui "0.0.1-alpha31"]
                 [com.github.mikehardice/capra "0.0.3"]
                 [org.clojure/core.async "1.5.648"]]
  ;;:resource-paths ["resources/strigui-0.0.1-alpha30.jar"]
  :main two-minutes.core
  :aot [two-minutes.core]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
