(defproject  jdkealy/forms-components "0.1.9-SNAPSHOT"
  :description "Reagent form and UI components"
  :url "https://github.com/jdkealy/form-components"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cljsjs/jquery "3.2.1-0"]
                 [org.clojure/clojure "1.8.0"]]
  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.18"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]
  :repositories [["clojars" {:creds :gpg}]]
  :deploy-repositories [["releases" :clojars]]

  :profiles {:dev {:repl-options {:init-ns testing.repl}
                   :dependencies [[cider/piggieback "0.4.0"]
                                  [org.clojure/clojurescript "1.10.520"]
                                  [binaryage/devtools "0.9.10"]
                                  [reagent "0.8.1"]
                                  [ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.7.1"]
                                  [prone "1.6.1"]
                                  [figwheel-sidecar "0.5.18"  :exclusions [com.google.guava/guava org.clojure/tools.reader]]
                                  [nrepl "0.6.0"]
                                  [pjstadig/humane-test-output "0.9.0"]

                                  ]
                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.18"]
]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}}
  :cljsbuild {
              :builds {:app
                       {:source-paths ["devcards" "src" ]
                        :figwheel {:on-jsload "testing.dev/mount-root"}
                        :compiler
                        {:main "testing.dev"
                         :npm-deps {"jquery" "3.4.0"}
                         :asset-path "js/out"
                         :output-to "target/cljsbuild/public/js/app.js"
                         :output-dir "target/cljsbuild/public/js/out"
                         :source-map true
                         :optimizations :none
                         :pretty-print  true}}}}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
