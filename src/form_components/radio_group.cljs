(ns form-components.radio-group)

(defn cmp [{:keys [className key] :as obj} data]
  [:div {:className className}
    [:section {:className "im-options options"}
    [:ul
     (doall
      (map (fn [_obj]
             [:li {:key (str _obj)}
              [:input {:type "radio"
                       :value (:value _obj)
                       :id (:value _obj)
                       :checked (if (= (get-in @data [key :value])
                                       (:value _obj))
                                  true
                                  false)
                       :on-change (fn [e]
                                    (swap! data
                                           (fn [item]
                                             (-> item
                                                 (assoc-in  [key :changed] true)
                                                 (assoc-in  [key :value ] (.. e -target -value) )))))
                       :name (:key obj)
                       }]
               [:label.fake-point {:for (:value _obj)}]
               [:span (:label _obj)]]
             ) (:fields obj)))]]])
