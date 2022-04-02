(ns gym.backend.logger
  (:require [cambium.codec :as codec]
            [cambium.core  :refer [log with-logging-context wrap-logging-context]]
            [cambium.logback.json.flat-layout :as flat]))

(flat/set-decoder! codec/destringify-val)

;; Copy-pasted from cambium.core.
;; Argument orders are changed.
(defmacro ^:private deflevel
  [level-sym]
  (let [level-key (keyword level-sym)
        level-doc (str "Similar to clojure.tools.logging/" level-sym ".")
        arglists  ''([msg-or-throwable] [msg mdc-or-throwable] [msg throwable mdc])]
    `(defmacro ~level-sym
       ~level-doc
       {:arglists ~arglists}
       ([msg-or-throwable#]
        (with-meta `(log ~~level-key ~msg-or-throwable#)           ~'(meta &form)))
       ([msg# mdc-or-throwable#]
        (with-meta `(log ~~level-key ~mdc-or-throwable# nil ~msg#) ~'(meta &form)))
       ([msg# throwable# mdc#]
        (with-meta `(log ~~level-key ~mdc# ~throwable# ~msg#)      ~'(meta &form))))))

(deflevel trace)
(deflevel debug)
(deflevel info)
(deflevel warn)
(deflevel error)
(deflevel fatal)

(def ^:macro with-context #'with-logging-context)
(def wrap-context wrap-logging-context)

(def *ctx (atom {}))

(defn update-context [f]
  (swap! *ctx f)
  nil)

(alter-var-root #'cambium.core/transform-context
                (fn [_]
                  (fn [context]
                    (merge @*ctx context))))
