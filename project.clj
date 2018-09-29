(defproject  jdkealy/forms-components "0.1.9-SNAPSHOT"
  :description "Reagent form and UI components"
  :url "https://github.com/jdkealy/form-components"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]

  :repositories [["clojars" {:creds :gpg}]]
  :deploy-repositories [["releases" :clojars]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
