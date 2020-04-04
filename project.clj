(defproject gym "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.postgresql/postgresql "42.2.11"]
                 [ring-server "0.5.0"]
                 [environ "1.1.0"]
                 [reagent "0.10.0"]
                 [reagent-utils "0.3.3"]
                 [re-frame "0.11.0"]
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
                 [org.clojure/clojurescript "1.10.597"
                  :scope "provided"]
                 [metosin/reitit "0.4.2"]
                 [metosin/reitit-spec "0.4.2"]
                 [pez/clerk "1.0.0"]
                 [clj-http "3.10.0"]
                 [cljs-http "0.1.46"]
                 [cljs-ajax "0.7.3"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [day8.re-frame/http-fx "v0.2.0"]
                 [venantius/accountant "0.2.5"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [migratus-lein "0.7.3"]
            [lein-asset-minifier "0.4.6"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler gym.handler/web-handler
         :uberwar-name "gym.jar"}

  :min-lein-version "2.5.0"
  :uberjar-name "gym.jar"
  :main gym.server
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
             {:output-to        "target/cljsbuild/public/js/app.js"
              :output-dir       "target/cljsbuild/public/js"
              :source-map       "target/cljsbuild/public/js/app.js.map"
              :optimizations :advanced
              :infer-externs true
              :pretty-print  false
              :npm-deps false
              :foreign-libs [{:file "dist/bundle.js"
                              :provides ["react-modal"
                                         "toastr"
                                         "firebase"
                                         "firebaseui"]
                              :global-exports {react-modal ReactModal
                                               toastr toastr
                                               firebase firebase
                                               firebaseui firebaseui}}]
              :closure-defines {gym.config/frontend-url ~(or (System/getenv "FRONTEND_URL") "")
                                gym.config/api-url ~(or (System/getenv "API_URL") "")}}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "gym.core/mount-root"}
             :compiler
             {:main "gym.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true
              :npm-deps false
              :infer-externs true
              :foreign-libs [{:file "dist/bundle.js"
                              :provides ["react-modal"
                                         "toastr"
                                         "firebase"
                                         "firebaseui"]
                              :global-exports {react-modal ReactModal
                                               toastr toastr
                                               firebase firebase
                                               firebaseui firebaseui}}]
              :closure-defines {gym.config/frontend-url "http://localhost:3449"
                                gym.config/api-url "http://localhost:3001/api"}}}}}

  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]
   :css-dirs ["resources/public/css"]
   :ring-handler gym.handler/web-handler}



  :profiles {:dev {:repl-options {:init-ns gym.repl}
                   :dependencies [[cider/piggieback "0.4.2"]
                                  [binaryage/devtools "0.9.11"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.8.0"]
                                  [prone "2019-07-08"]
                                  [figwheel-sidecar "0.5.19"]
                                  [nrepl "0.6.0"]
                                  [re-frisk "0.5.3"]
                                  [pjstadig/humane-test-output "0.10.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.19"]]

                   :compiler {:preloads [re-frisk.preload]}
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true
                         :pg-host "localhost"
                         :pg-port 5432
                         :pg-db "postgres"
                         :pg-user "postgres"
                         :pg-password "postgres"
                         :frontend-url "http://localhost:3449"}}

             :uberjar {:hooks [minify-assets.plugin/hooks leiningen.cljsbuild]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true
                             :pg-host ~(System/getenv "PG_HOST")
                             :pg-port ~(System/getenv "PG_PORT")
                             :pg-db ~(System/getenv "PG_DB")
                             :pg-user ~(System/getenv "PG_USER")
                             :pg-password ~(System/getenv "PG_PASSWORD")
                             :frontend-url ~(System/getenv "FRONTEND_URL")}
                       :aot :all
                       :omit-source true}}
  ;; Currently only doing (convenient) migrations in dev so I'm not bothering with
  ;; figuring out the setup for different environments yet
  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost:5432/postgres"
                  :user "postgres"
                  :password "postgres"}})
