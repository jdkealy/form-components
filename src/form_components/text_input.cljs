(ns form-components.text-input)


(defn cmp [{:keys [key type trim-fn input-hashmap] :as obj} data]

  (let [input-hashmap (or input-hashmap {})]
    [:input.form-control
     (merge input-hashmap
            {:type (if type (name type) "text")
             :class (cond
                      (get-in @data [key :error]) "invalid"
                      (get-in @data [key :valid]) "valid"
                      :else "")
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
                                              (assoc-in [key :changed] true))))))})]))
