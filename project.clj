(defproject metaq "0.1.0"
  :description "a set of metaq client APIs in favour of clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.4"]
                 [com.taobao.metamorphosis/metamorphosis-client "1.4.2"
                  :exclusions [jline/jline
                               junit/junit
                               javax.servlet/servlet-api]]])

