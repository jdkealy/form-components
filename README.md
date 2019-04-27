# Materialize Form Components


## Usage

Make an atom and a form structure. Pass the atom and the inputs config in as an array and get helper methods to initialize the form, place errors in appropriate places, make material selects/checkboxes, etc. 
# Materialize Form Components


## Usage

Make an atom and a form structure. Pass the atom and the inputs config in as an array and get helper methods to initialize the form, place errors in appropriate places, make material selects/checkboxes, etc. 

# FORM CONFIG:

```
(ns forms.core
  (:require
   [create-react-class :as create-react-class]
   [reagent.core :as reagent :refer [atom]]
   [form-components.form-input :as form-input]

   [reagent.core :as reagent]))

(enable-console-print!)

(def _atom (atom {}))

(def fields
  [[{:label "Start Date"
      :icon "fa fa-calendar"
      :validators [:not-blank]
      :select-opts {:autoClose false
                    :selectYears 2
                    :selectMonths false
                    :formatSubmit "yyyy-mm-dd"
                    :format "yyyy-mm-dd"}
      :form-group-class "col s10 m6"

      :id "start_date"
     :type :date
     :key :start_date }
    {:label "End Date"
     :icon "fa fa-calendar"
     :form-group-class "col s10 m6"
     :id "end_date"
     :type :date
     :key :end_date }]
   [{:label "Other Field"
     :form-group-class "col s10 m6"
     :id "wildcard"
     :type :text
     :key :_all }]])

(defn search-form
  []
  (reagent/create-class
   {:reagent-render (fn []
                      [form-input/form  _atom fields {:header "TEST FORM"
                                                      :callback (fn [params done]
                                                                  (.setTimeout js/window
                                                                               done
                                                                               1000))}])}))


```
