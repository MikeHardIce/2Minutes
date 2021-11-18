(defproject two-minutes "0.1.0-SNAPSHOT"
  :description ""
  :url "https://github.com/MikeHardIce/2Minutes"
  :license {:name "MIT License"
            :author "MikeHardIce"
            :url "none"
            :year 2021
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [strigui "0.0.1-alpha20"]
                 [org.clojure/core.async "1.4.627"]]
  :main ^:skip-aot two-minutes.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
