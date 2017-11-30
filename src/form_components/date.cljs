(ns form-components.date
  (:require [reagent.core :as reagent :refer [atom]]
            [taoensso.timbre :as timbre
             :refer-macros (debug)]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]))

(defn- make-datepicker [{:keys [callback]} input]
  (reagent/create-class
   {:component-did-mount (fn [this]
                           (let [opts (clj->js
                                       {:onSetCallback (fn [val]
                                                         (let [val (js->clj val)
                                                               ts (get val "select")]
                                                           (cond
                                                             ts
                                                             (callback (.toNiceDate js/window ts))

                                                             (contains? val "clear")
                                                             (callback nil))))
                                        :autoclose true
                                        :selectYears 10
                                        :selectMonths true
                                        :formatSubmit "yyyy-mm-dd"
                                        :format "yyyy-mm-dd"})]
                             (.makeDatepicker js/window (reagent/dom-node this) opts)))
    :reagent-render (fn []
                      input)}))

(defn date-with-cb [val callback]
  [make-datepicker {:callback callback}
   [:input.form-control.datepicker {:type "text"
                                    :default-value val}]])

(defn cmp [{:keys [key] :as obj} data]
  [make-datepicker {:callback (fn [formatted]
                                (swap! data #(-> %
                                                 (assoc-in [key :changed] true)
                                                 (assoc-in [key :value] formatted))))}
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
                                    :label (:label obj)}]])
