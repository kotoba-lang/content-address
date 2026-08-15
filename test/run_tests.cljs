(ns run-tests
  "nbb entry point: `nbb --classpath src:test test/run_tests.cljs`."
  (:require [cljs.test :refer [run-tests]]
            [content-address.core-test]))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (println "\ntests" (:test m) "assertions" (+ (:pass m) (:fail m) (:error m))
           "fail" (:fail m) "error" (:error m))
  (when-not (cljs.test/successful? m)
    (set! (.-exitCode js/process) 1)))

(run-tests 'content-address.core-test)
