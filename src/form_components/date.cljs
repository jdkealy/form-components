(ns form-components.date
  (:require [reagent.core :as reagent :refer [atom]]
            [taoensso.timbre :as timbre
             :refer-macros (debug)]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]))

(defn makeDatePicker [node opts cb dont-close?]
  (set! (.-onSet opts) (fn [val]
                         (println "THIS IS VAL" val)
                         (cb val)
                         (this-as this
                                  (do
                                    (println "SELECTED!!!!!!!!!!!!!!!!")
                                    (if (.-select val)
                                      (when-not dont-close?
                                        (.close this))
                                      (.onSetCallback opts val))))))
  (.pickadate (js/$ node) opts))

(defn- make-datepicker [{:keys [callback dont-close?]} input]
  (reagent/create-class
   {:component-did-mount (fn [this]
                           (let [cb (fn [val]
                                      (let [val (js->clj val)
                                            ts (get val "select")]
                                        (cond
                                          ts
                                          (callback ts )
                                          (contains? val "clear")
                                          (callback nil))))
                                 opts (clj->js
                                       {:onSetCallback cb
                                        :autoclose false
                                        :selectYears 10
                                        :selectMonths true
                                        :formatSubmit "yyyy-mm-dd"
                                        :format "yyyy-mm-dd"})]
                             (makeDatePicker (reagent/dom-node this) opts cb dont-close?)))
    :reagent-render (fn []
                      input)}))

(defn date-with-cb [val callback]
  [make-datepicker {:callback callback}
   [:input.form-control.datepicker {:type "text"
                                    :default-value val}]])

(defn cmp [{:keys [dont-close? key callback] :as obj} data]
  (println "M2")
  [:div
   [make-datepicker {:dont-close? dont-close?
                     :callback (fn [formatted]
                                 (println "P2")
                                 (swap! data #(-> %
                                                  (assoc-in [key :changed] true)
                                                  (assoc-in [key :value] formatted)))
                                 (when callback (callback data)))}
    [:input.form-control.datepicker {:type "text"
                                     :id (when (:id obj)
                                           (:id obj))
                                     :placeholder (when (:jstyle obj)
                                                    (:label obj))
                                     :on-focus (fn [_]
                                                 (swap! data #(update-in % [key] dissoc :error)))
                                     :default-value (get-in @data [key :value])
                                     :on-key-up (fn [e]
                                                  false)
                                     :label (:label obj)}]]])
