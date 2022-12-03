(defproject gym "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cambium/cambium.core           "1.1.1"]
                 [cambium/cambium.codec-cheshire "1.0.0"]
                 [cambium/cambium.logback.json   "0.4.5"]
                 [org.postgresql/postgresql "42.2.11"]
                 [com.github.seancorfield/honeysql "2.2.868"]
                 [ring-server "0.5.0"]
                 [environ "1.1.0"]
                 [reagent "1.1.0"]
                 [reagent-utils "0.3.4"]
                 [re-frame "1.2.0"]
                 [seancorfield/next.jdbc "1.0.409"]
                 [migratus "1.2.8"]
                 [buddy/buddy-core "1.6.0"]
                 [buddy/buddy-sign "3.1.0"]
                 [ring "1.8.0"]
                 [ring-cors "0.1.13"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [hiccup "1.0.5"]
                 [yogthos/config "1.1.7"]
                 [integrant "0.8.0"]
                 [integrant/repl "0.3.2"]
                 [org.clojure/clojurescript "1.10.597"
                  :scope "provided"]
                 [metosin/reitit "0.4.2"]
                 [metosin/reitit-spec "0.4.2"]
                 [pez/clerk "1.0.0"]
                 [clj-http "3.10.0"]
                 [cljs-http "0.1.46"]
                 [cljs-ajax "0.8.4"]
                 [clj-commons/cljss "1.6.4"]
                 [clojure-humanize "0.2.2"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [day8.re-frame/http-fx "0.2.4"]
                 [com.taoensso/carmine "3.0.1"]
                 [venantius/accountant "0.2.5"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-shell "0.5.0"]
            [lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [migratus-lein "0.7.3"]
            [lein-asset-minifier "0.4.6"
             :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.5.0"
  :uberjar-name "gym.jar"
  :main gym.backend.main
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  [[:css {:source "resources/public/css/site.css"
          :target "resources/public/css/site.min.css"}]]

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
              {:output-to "target/cljsbuild/public/js/app.js"
               :output-dir "target/cljsbuild/public/js"
               :source-map "target/cljsbuild/public/js/app.js.map"
               :externs ["resources/public/js/externs.js"]
               :optimizations :advanced
               :infer-externs true
               :pretty-print false
               :npm-deps false
               :foreign-libs [{:file "dist/bundle.js"
                               :provides ["react"
                                          "react-dom"
                                          "parse-color"
                                          "react-color"
                                          "emoji-picker-react"
                                          "react-contenteditable"
                                          "react-modal"
                                          "toastr"
                                          "recharts"
                                          "react-swipeable"
                                          "auth0spa"]
                               :global-exports {react React
                                                react-dom ReactDOM
                                                parse-color parseColor
                                                react-color reactColor
                                                emoji-picker-react EmojiPickerReact
                                                react-contenteditable ReactContenteditable
                                                react-modal ReactModal
                                                toastr toastr
                                                recharts recharts
                                                react-swipeable reactSwipeable
                                                auth0spa auth0spa}}]
               :closure-defines {gym.frontend.config/commit-sha ~(try
                                                                   (slurp "./.commit_sha")
                                                                   (catch Exception _ ""))}}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "gym.frontend.core/mount-root"
                        ;; I turned this on, because of reagent deprecation warnings
                        ;; which don't affect anything right now. TODO: update reagent?
                        :load-warninged-code true}
             :compiler
             {:main "gym.frontend.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true
              :npm-deps false
              :infer-externs true
              :foreign-libs [{:file "dist/bundle.js"
                              :provides ["react"
                                         "react-dom"
                                         "parse-color"
                                         "react-color"
                                         "emoji-picker-react"
                                         "react-contenteditable"
                                         "react-modal"
                                         "toastr"
                                         "recharts"
                                         "react-swipeable"
                                         "auth0spa"]
                              :global-exports {react React
                                               react-dom ReactDOM
                                               parse-color parseColor
                                               react-color reactColor
                                               emoji-picker-react EmojiPickerReact
                                               react-contenteditable ReactContenteditable
                                               react-modal ReactModal
                                               toastr toastr
                                               recharts recharts
                                               react-swipeable reactSwipeable
                                               auth0spa auth0spa}}]
              :closure-defines {gym.frontend.config/commit-sha ~(try
                                                                  (slurp "./.commit_sha")
                                                                  (catch Exception _ ""))}}}}}

  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]
   :css-dirs ["resources/public/css"]}



  :profiles {:dev {:repl-options {:init-ns gym.backend.repl}
                   :dependencies [[cider/piggieback "0.4.2"]
                                  [binaryage/devtools "0.9.11"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.8.0"]
                                  [prone "2019-07-08"]
                                  [figwheel-sidecar "0.5.19"]
                                  [nrepl "0.6.0"]
                                  [re-frisk "1.5.2"]
                                  [pjstadig/humane-test-output "0.10.0"]]

                   :jvm-opts ["-Dlogback.configurationFile=resources/logback.dev.xml"]
                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.19"]]

                   :compiler {:preloads [re-frisk.preload]}
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true
                         :commit-sha ~(try
                                        (slurp "./.commit_sha")
                                        (catch Exception _ ""))}}

             :uberjar {:source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"] ["minify-assets"]]
                       :env {:production true
                             :commit-sha ~(try
                                            (slurp "./.commit_sha")
                                            (catch Exception _ ""))}
                       :aot :all
                       :omit-source true}}
  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(or (System/getenv "PG_URL") "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres")})
