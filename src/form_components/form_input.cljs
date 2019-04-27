(ns form-components.form-input
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.events :as events]

            [form-components.text-input :as text]
            [form-components.date :as date]
            [form-components.checkbox :as checkbox]
            [form-components.selector :as select]


            [goog.history.EventType :as EventType]))

(defn input-wrap [{:keys [icon label-above? label-class skip-input-class input-wrapper-class key with-checkbox skip-decorator-label] :as obj} data cmp]
  (let [key (:key obj)
        input-wrapper-class (or input-wrapper-class "")
        base (get @data key)
        type (:type obj "text")
        err (:error base)
        label [:label
               {:class (str
                        (when label-class
                          label-class))
                :data-error err
                :for (:id obj)}
               (when with-checkbox
                 [:input {:on-change
                          (fn [e]
                            (swap! data assoc-in [key :changed]
                                   (if (.-checked (.-currentTarget e))
                                     true
                                     false)))
                          :checked (if (get-in @data [key :changed])
                                     true
                                     false)
                          :type "checkbox"}])
               (when with-checkbox
                 [:span " "])
               (when icon
                 [:span
                  [:i {:class icon}]
                  " "])
               (when-not skip-decorator-label
                 (:label obj))]
        label-below? (not label-above?)]
    [:div
     {:key key
      :className (str (:form-group-class obj) " input-field " )}
     (when label-above? label)
     cmp
     (when label-below? label )

     (if (:error base)
       [:span.helper-text (:error base)]
       (if-let [hb  (:help-block obj)]
         [:span.helper-text hb ]
         [:div]))
     ]))

(defn image-cmp [{:keys [key type] :as obj} data]
  [:div.file-field.input-field
   [:div.btn
    [:span "File"]
    [:input {:on-change (fn [e]
                          (let [tar (.. e -target)
                                fcount (.-length  (.-files tar)) ]
                            (dotimes [n 1]
                              (let [file (aget (.-files tar) n)
                                    url (.createObjectURL (.-URL js/window)
                                                          file)]
                                (swap! data (fn [item]
                                              (assoc-in item [key :tmp-value] {:file file
                                                                               :test file
                                                                               :url url})))))))
             :type "file"}]]
   [:div.file-path-wrapper
    [:input.file-path.validate {:type "text"}]]])

(defn key-to-cmp [{:keys [type key] :as obj} data ]
  (case type
    :select [select/cmp obj data]
    :checkbox [checkbox/cmp obj data]
    :date [date/cmp obj data]
    [text/cmp obj data nil nil nil ]))


(defn input [{:keys [val-path className custom-cmp  text-in-line skip-decorator custom_inputs key type] :as obj} data ]
  (if (:custom_inputs obj)
    (:inputs obj)
    (let [cmp (if (and
                   text-in-line
                   (get-in @data val-path)
                   (not (get-in @data [key :editing])))
                [:span {:className className
                        :on-click (fn [_]
                                    (swap! data (fn [e]
                                                  (assoc-in e [key :editing] true)))
                                    )} (get-in @data val-path) ]
                (if custom-cmp
                  [custom-cmp obj data ]
                  (key-to-cmp obj data )))]
      (if skip-decorator
        cmp
        (input-wrap obj data cmp)))))

(defn email-validator [key data]
  (let [val (get-in @data [key :value])
        regexp #"\S+@\S+\.\S+"]
    (when
        (or (not val)
            (= "" val)
            (not (.test regexp (clj->js val))))
      (swap! data (fn [d] (assoc-in d [key :error] "Not a valid email"))))))

(defn validate-pw-long [key data callback]
  (let [val (get-in @data [key :value]) ]
    (when
        (or (not val)
            (> 7 (count val)))
      (swap! data (fn [d] (assoc-in d [key :error] "Cant be less than 7 characters"))))))

(defn validate-equal-fields [key-1 key-2 data]
  (let [val-1 (get-in @data [key-1 :value]) val-2 (get-in @data [key-2 :value])]
    (when (not= val-1 val-2)
      (swap! data (fn [d] (assoc-in d [key-2 :error] "Must be equal"))) )))

(defn not-blank [key data]
  (let [val (get-in @data [key :value]) ]
    (when
        (or (not val)
            (= "" val))
      (swap! data (fn [d] (assoc-in d [key :error] "Cant be empty"))))))

(defn has-option [key data]
  (let [val (get-in @data [key :value]) ]
    (when
        (or (not val)
            (= "" val))
      (swap! data (fn [d] (assoc-in d [key :error] "You must choose one option"))))))

(defn validate-components [fields data]
  (doall
   (->> fields
        flatten
        (map (fn [{:keys [key validators validators]}]
               (swap! data update-in [key] dissoc :error)
               (swap! data update-in [key] dissoc :valid)
               (doall  (->> validators
                            (map (fn [validator]
                                   (case validator
                                     :has-option (has-option key data)
                                     :not-blank (not-blank key data)
                                     :email (email-validator key data)
                                     (if (= js/Function  (type validator))
                                       (validator key data)
                                       identity))))))
               (when-not (get-in @data [key :error])
                 (swap! data update-in [key] assoc :valid true)))))))

(defn inputs-to-key-val [inputs]
  (reduce (fn [out in-key]
            (assoc out in-key (:value (get inputs in-key)))) {}  (keys inputs)))

(defn changed-inputs-to-key-val [inputs]
  (reduce (fn [out in-key]
            (if (:changed (get inputs in-key))
              (assoc out in-key (:value (get inputs in-key)))
              out)) {}  (keys inputs)))

(defn init-state [state from fields]
  (let [_state (reduce (fn [memo item]
                         (let [val (get from item)]
                           (if val
                             (assoc-in memo [item :value] val)
                             memo))
                         ) {} (->>  fields
                                    (map :key)))]
    (reset! state _state)))



(defn reset-form-fields-from-query [_atom fields search]
  (let [_keys (->> fields
                   (map :key)
                   vec)
        kv (select-keys search _keys)
        re-init (reduce (fn [memo item]
                          (assoc-in memo [item :value] (get kv item))
                          ) {} (keys kv))]
    (reset! _atom re-init)))


(defn wrap-submit [_fields _atom submit ]
  (validate-components _fields _atom)
  (when (zero? (count (filter identity (map :error (vals @_atom)))))
    (swap! _atom #(assoc % :submitting true))
    (let [params (inputs-to-key-val @_atom)]
      (submit params (fn []
                       (swap! _atom #(assoc % :submitting false))) ))))


(defn form [state fields {:keys [header submit-label callback action-class]  :as params}]
  (let [cb-fn (fn [e]
                (wrap-submit fields state callback)
                (.preventDefault e))]
    [:div
     (str @state )
     (when header
       [:div.row
        [:div.col.s12
         [:h4 header]]])
     (when-let [error (:error @state)]
       [:div.form-error.red-text
        error])
     [:form {:on-submit cb-fn}
      [:div.row
       (map-indexed (fn [idx field]
              (if (= cljs.core/PersistentVector  (type  field))
                ^{:key (str idx )} [:div.row
                     (->> field
                          (map (fn [field]
                                 ^{:key field} [input field state])))]
                ^{:key field} [input field state]))
            fields)]
      [:div.row {:className (or action-class "")}
       [:div.col.s12
        [:span {:className (str "btn " (when (or (:submitting @state)
                                                 (:disabled @state)) " disabled "))
                :on-click cb-fn} (or submit-label "submit")]]]]]))
