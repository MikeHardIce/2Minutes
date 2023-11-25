(defproject two-minutes "0.1.0-SNAPSHOT"
  :description ""
  :url "https://github.com/MikeHardIce/2Minutes"
  :license {:name "MIT"
            :author "MikeHardIce"
            :url "https://choosealicense.com/licenses/mit"
            :year 2021
            :key "mit"
            :comment "MIT License"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.github.mikehardice/capra "0.0.10"]
                 [org.clojure/core.async "1.6.681"]]
  :resource-paths ["resources/strigui-0.0.1-alpha32.jar"]
  :main two-minutes.core
  :aot [two-minutes.core]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
