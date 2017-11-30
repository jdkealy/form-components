(ns form-components.text
  (:require
   [reagent.core :as reagent :refer [atom]]))


(defn cmp [{:keys [key type trim-fn input-hashmap] :as obj} data]
  (reagent/create-class

   {:component-did-mount (fn []
                           (when (and
                                  (:default-value obj)
                                  (not (get-in @data [key :value])))
                             (swap! data #(assoc-in % [key :value] (:default-value obj)))))
    :reagent-render (fn []
                      (let [input-hashmap (or input-hashmap {})]
                        [:input.form-control
                         (merge  {:type (if type (name type) "text")
                                  :class (str
                                          (:input-class obj)
                                          " "
                                          (cond
                                            (get-in @data [key :error]) "invalid"
                                            (get-in @data [key :valid]) "valid"
                                            :else ""))
                                  :placeholder (if-let [ph (get-in @data [key :placeholder])]
                                                 ph)
                                  :on-focus (fn [_]
                                              (swap! data (fn [e]
                                                            (-> e
                                                                (assoc :focus-key key)
                                                                (update-in [key] dissoc :error )))))
                                  :id (when (:id obj)
                                        (:id obj))
                                  :value (get-in @data [key :value])
                                  :on-change (fn [e]
                                               (swap! data (fn [item]
                                                             (let [_val (.. e -target -value)
                                                                   _val (if  trim-fn (trim-fn _val) _val)
                                                                   val (case type
                                                                         "number" (cljs.reader/read-string _val)
                                                                         _val)]
                                                               (-> item
                                                                   (assoc-in [key :value ] val)
                                                                   (assoc-in [key :changed] true))))))}
                                 input-hashmap)]))}))
