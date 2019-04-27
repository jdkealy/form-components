(ns form-components.date
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]))

(defn makeDatePicker [node opts cb ]
  (set! (.-onSelect opts) (fn [val]
                            (cb val)))
  (.datepicker (js/$ node) opts))

(defn- make-datepicker [{:keys [callback select-opts]} input]

  (reagent/create-class
   {:component-did-mount (fn [this]
                           (let [cb (fn [val]
                                      (let [val (js->clj val)]
                                        (callback val)))
                                 opts (clj->js
                                       (merge  {:autoClose true
                                                :selectYears 10
                                                :selectMonths true
                                                :formatSubmit "yyyy-mm-dd"
                                                :format "yyyy-mm-dd"} (or select-opts {})))]
                             (makeDatePicker (reagent/dom-node this) opts cb )))
    :reagent-render (fn []
                      input)}))

(defn cmp [{:keys [key select-opts ] :as obj} data]
  [make-datepicker {:select-opts select-opts
                    :callback (fn [formatted]
                                (swap! data #(-> %
                                                 (assoc-in [key :changed] true)
                                                 (assoc-in [key :value] formatted))))}
   [:input.form-control.datepicker {:type "text"
                                    :id (when (:id obj)
                                          (:id obj))
                                    :on-focus (fn [_]
                                                (swap! data #(update-in % [key] dissoc :error)))
                                    :default-value (get-in @data [key :value])
                                    :on-key-up (fn [e]
                                                 false)
                                    :label (:label obj)}]])
