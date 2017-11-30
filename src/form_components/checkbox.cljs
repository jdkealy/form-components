(ns form-components.checkbox)

(defn cmp [{:keys [key checked? callback] :as obj}  data]
  (let [base (get @data key)
        id (if-let [id (:id obj)]
             id
             (str (:label obj)))]
    [:div.form-input {:className (str
                                  (when (:className obj)
                                    (:className obj)
                                    )
                                  " "
                                  (when (:error base)
                                    "has-error"))}
     [:input {:id id
              :checked (if (or (get-in @data [key :value])
                               checked?)
                         true
                         false)
              :on-change (fn [e]
                           (let [tf (.. e -target -checked) ]
                             (swap! data (fn [item]
                                           (-> item
                                               (assoc-in [key :value ] tf)
                                               (assoc-in [key :changed ] true))))
                             (when callback (callback tf))))
              :type "checkbox"}]
     [:label {:for id}
      (when-not (:skip-label obj)
        (:label obj))]]))
