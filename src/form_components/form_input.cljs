(ns form-components.form-input
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [goog.events :as events]
            [form-components.text-input :as text]
            [form-components.date :as date]
            [form-components.checkbox :as checkbox]
            [form-components.selector :as select]
            [taoensso.timbre :as timbre
             :refer-macros (debug)]
            [ajax.core :refer [POST GET DELETE PUT]]
            [goog.history.EventType :as EventType]))

(defn input-wrap [{:keys [icon label-class input-wrapper-class key with-checkbox skip-decorator-label] :as obj} data cmp]
  (let [key (:key obj)
        input-wrapper-class (or input-wrapper-class "")
        base (get @data key)
        type (:type obj "text")
        err (:error base)]
    [:div
     {:key key
      :className (str (:form-group-class obj) " "(when-not (= :select type)  "input-field"))}
     cmp
     [:label
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
         " "
         ]

        )
      (when-not skip-decorator-label
        (:label obj))]
     (if (:error base)
       [:span.helper-text (:error base)]
       (if-let [hb  (:help-block obj)]
         [:span.helper-text hb ]
         [:div]))
     ]))


(defn append-nested [params item _field]
  (let [_key (:key _field)

        fields (doall (->> _field
                           :fields
                           (map :field)
                           vec))
        contact (->> fields
                     (reduce (fn [memo field]
                               (if-let [p (get-in item [_key field :value])]
                                 (assoc memo field p)
                                 memo)) {}))
        _vals (->> contact
                   vals
                   (filter #(and % (not= "" %)))
                   count)]
    (if (= 0 _vals)
      params
      (assoc params _key contact))))

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

(defn contact-validator [key data]
  (let [val (get-in @data [key :website :value])
        regexp #"https://\S+\.\S+"]
    (when
        (and  val
              (not= "" val)
              (not (.test regexp (clj->js val))))
      (swap! data (fn [d] (assoc-in d [key :error] "Website: not a valid URL.  Must start with https://"))))))

(defn validate-unique-email [email key data callback]
  (POST (str "/check-email.edn")
        {:keywords? true
         :format :edn
         :params {:user/email email}
         :handler (fn [response]
                    (when (:exist? response)
                      (swap! data (fn [d]
                                    (assoc-in d [key :error]
                                              (str "user " email " already exists")))))
                    (callback))}))

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

(defn city-validator [key data]
  (let [val (get-in @data [key :value]) ]
    (do
      (when
          (or (not val)
              (= "" val))
        (swap! data (fn [d] (assoc-in d [key :error] "TBD? We at least need the city.")))))))

(defn validate-components [keys data]
  (doall
   (map (fn [{:keys [key validator]}]
          (swap! data update-in [key] dissoc :error)
          (swap! data update-in [key] dissoc :valid)
          (case validator
            :has-option (has-option key data)
            :not-blank (not-blank key data)
            :email (email-validator key data)
            identity)
          (when-not (get-in @data [key :error])
            (swap! data update-in [key] assoc :valid true)))
        keys)))

(defn inputs-to-key-val [inputs]
  (reduce (fn [out in-key]
            (assoc out in-key (:value (get inputs in-key)))
            ) {}  (keys inputs)))

(defn changed-inputs-to-key-val [inputs]
  (reduce (fn [out in-key]
            (if (:changed (get inputs in-key))
              (assoc out in-key (:value (get inputs in-key)))
              out)
            ) {}  (keys inputs)))

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



(defn form [state fields {:keys [header submit-label callback action-class]  :as params}]
  (let [cb-fn (fn [e]
                (validate-components fields state)
                (when (= 0 (count (filter identity (map :error (vals @state)))))
                  (callback (inputs-to-key-val @state) ))
                (.preventDefault e))]
    [:div
     (when header
       [:h4 header])
     (when-let [error (:error @state)]
       [:div.form-error.red-text
        error])
     [:form {:on-submit cb-fn}
      (map (fn [field]
             ^{:key field} [input field state])
           fields)
      [:div {:className (or action-class "")}

       [:span.btn {:className (when (:disabled @state) " disabled ")
                   :on-click cb-fn} (or submit-label "submit")]]]]))
