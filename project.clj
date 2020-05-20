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
                 [clj-commons/cljss "1.6.4"]
                 [clojure-humanize "0.2.2"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [day8.re-frame/http-fx "v0.2.0"]
                 [venantius/accountant "0.2.5"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-shell "0.5.0"]
            [lein-environ "1.1.0"]
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
          :target "site/css/site.min.css"}]
   [:css {:source "resources/public/css/emoji-mart.css"
          :target "site/css/emoji-mart.min.css"}]]

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to        "site/js/app.js"
              :output-dir       "site/js"
              :source-map       "site/js/app.js.map"
            ;;   :output-to        "target/cljsbuild/public/js/app.js"
            ;;   :output-dir       "target/cljsbuild/public/js"
            ;;   :source-map       "target/cljsbuild/public/js/app.js.map"
              :externs ["resources/public/js/externs.js"]
              :optimizations :advanced
              :infer-externs true
              :pretty-print  false
              :npm-deps false
              :foreign-libs [{:file "dist/bundle.js"
                              :provides ["parse-color"
                                         "react-color"
                                         "smileParser"
                                         "emojiMart"
                                         "react-contenteditable"
                                         "react-modal"
                                         "toastr"
                                         "auth0spa"]
                              :global-exports {parse-color parseColor
                                               react-color reactColor
                                               smileParser smileParser
                                               emojiMart emojiMart
                                               react-contenteditable ReactContenteditable
                                               react-modal ReactModal
                                               toastr toastr
                                               auth0spa auth0spa}}]
              :closure-defines {gym.config/api-url ~(or (System/getenv "API_URL") "")
                                gym.config/auth0-client-id ~(or (System/getenv "AUTH0_CLIENT_ID") "")
                                gym.config/sentry-dsn ~(or (System/getenv "SENTRY_DSN") "")
                                ;; this comes from netlify in prod
                                gym.config/commit-sha ~(or (System/getenv "COMMIT_REF") "")}}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "gym.core/mount-root"
                        ;; I turned this on, because of reagent deprecation warnings
                        ;; which don't affect anything right now. TODO: update reagent?
                        :load-warninged-code true}
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
                              :provides ["parse-color"
                                         "react-color"
                                         "smileParser"
                                         "emojiMart"
                                         "react-contenteditable"
                                         "react-modal"
                                         "toastr"
                                         "auth0spa"]
                              :global-exports {parse-color parseColor
                                               react-color reactColor
                                               smileParser smileParser
                                               emojiMart emojiMart
                                               react-contenteditable ReactContenteditable
                                               react-modal ReactModal
                                               toastr toastr
                                               auth0spa auth0spa}}]
              :closure-defines {gym.config/api-url ~(or (System/getenv "API_URL") "http://localhost:3001")
                                gym.config/auth0-client-id ~(or (System/getenv "AUTH0_CLIENT_ID") "")
                                gym.config/sentry-dsn ~(or (System/getenv "SENTRY_DSN") "")
                                gym.config/commit-sha ~(or (System/getenv "COMMIT_REF") "")}}}}}

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
                                  [re-frisk "1.1.0"]
                                  [pjstadig/humane-test-output "0.10.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.19"]]

                   :compiler {:preloads [re-frisk.preload]}
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true
                         ;; jdbc connection uri
                         :jdbc-database-url "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"
                         :frontend-urls "http://localhost:3449"
                         :port "3001"
                         :host-url "http://localhost:3001"
                         :public-key ~(try
                                        (slurp "./certs/auth0-public-key.pem")
                                        (catch Exception _ ""))
                         :commit-sha ~(System/getenv "COMMIT_REF")}}

             :uberjar {:source-paths ["env/prod/clj"]
                  ;;      :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :prep-tasks ["compile"]
                       :env {:production true
                             ;; jdbc connection uri supplied by Heroku
                             :jdbc-database-url ~(System/getenv "JDBC_DATABASE_URL")
                             :frontend-urls ~(System/getenv "FRONTEND_URLS")
                             :port ~(System/getenv "PORT")
                             :host-url ~(System/getenv "HOST_URL")
                             :public-key ~(System/getenv "AUTH0_PUBLIC_KEY")
                             :commit-sha ~(System/getenv "COMMIT_REF")}
                       :aot :all
                       :omit-source true}}
    :migratus {:store :database
               :migration-dir "migrations"
               :db ~(or (System/getenv "PG_URL") "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres")})
