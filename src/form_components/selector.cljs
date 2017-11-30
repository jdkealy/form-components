(ns form-components.selector
  (:require
   [reagent.core :as reagent :refer [atom]]
   [taoensso.timbre :as timbre
    :refer-macros (debug)]))

(defn cmp [{:keys [key on-change placeholder disabled] :as obj} data ]
  (let [current (get-in @data [key :value])
        base (get @data key)
        change (fn [e]
                 (swap! data (fn [item]
                               (let [val (.. e -target -value)
                                     val (if (:parse-int obj)
                                           (cljs.reader/read-string val)
                                           (if (:parse-keyword obj)
                                             (keyword val)
                                             val))]
                                 (if (= "-1" val)
                                   (-> item
                                       (assoc-in  [key :changed] true)
                                       (update-in  [key] dissoc :value))
                                   (-> item
                                       (assoc-in  [key :changed] true)
                                       (assoc-in  [key :value ] val)
                                       (update-in [key] dissoc :error ))))))
                 (when on-change (on-change)))]

    (comment

      )
    (reagent/create-class
     {:component-will-unmount (fn [this]
                                (let [el (reagent/dom-node this) ]
                                  (->  el
                                       js/$
                                       (.material_select "destroy"))
                                  (->  el
                                       js/$
                                       (.material_select))
                                  (->  el
                                       js/$
                                       (.on "change" change)))

                                )
      :component-did-mount (fn [this]
                             (let [el (reagent/dom-node this) ]
                                  (->  el
                                       js/$
                                       (.material_select "destroy"))
                                  (->  el
                                       js/$
                                       (.material_select))
                                  (->  el
                                       js/$
                                       (.on "change" change))
                                  (let [value (get-in @data [key :value])]
                                    (when value
                                      (-> el
                                          js/$
                                          (.val value))))))
      :reagent-render (fn []
                        [:select (merge  {:key (str (:fields obj)  (get-in @data [key :value]))
                                          :value (get-in @data [key :value])
                                          :placeholder (when placeholder placeholder)
                                          :on-focus (fn [e] (swap! data (fn [ee] (assoc ee :focus-key key))))
                                          :className (str
                                                      (when (:classNameFunc obj)
                                                        ((:classNameFunc obj)  base)
                                                        )
                                                      (when (:className obj)
                                                        (:className obj)
                                                        )
                                                      " "
                                                      (when (:error base)
                                                        "has-error"))
                                          :on-change (fn [e]

                                                       )}
                                         (if (:disabled obj)
                                           {:disabled true}
                                           {})
                                         (if current
                                           {:defaultValue current}
                                           (if (:placeholder obj)
                                             {:defaultValue "-1"}
                                             {})))
                         (when-let [p (:placeholder obj)]
                           [:option {:value "-1" } p])
                         (map (fn [opt]
                                (let [selected? (=
                                                 (:value opt)
                                                 (get-in @data [key :value]))
                                      options {:key (str opt)
                                               :value (:value opt)}
                                      options (if-let [pic (:pic opt)]
                                                (assoc options :data-icon pic)
                                                options)]
                                  (if (:value opt)
                                    [:option options

                                     (:display opt)]
                                    [:option {:key opt
                                              :selected selected?
                                              :value opt}
                                     opt])))
                              (:fields obj))])})))
