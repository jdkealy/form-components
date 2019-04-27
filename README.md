# Materialize Form Components


## Usage

Make an atom and a form structure. Pass the atom and the inputs config in as an array and get helper methods to initialize the form, place errors in appropriate places, make material selects/checkboxes, etc. 
# Materialize Form Components


## Usage

Make an atom and a form structure. Pass the atom and the inputs config in as an array and get helper methods to initialize the form, place errors in appropriate places, make material selects/checkboxes, etc. 

# FORM CONFIG:

`
(def fields [{:label "* EMAIL" :key :user/email :auto-focus true
                     :validator :email :error false}
                    {:type "password" :label "* PASSWORD" :key :user/password :validator :not-blank :error false}])
`


`(def _atom (atom {})`


`
[:form 
{:on-submit (fn [e]
                                             (form-input/validate-components fields auth/sign-ins)
                                             (when (zero? (count (filter identity (map :error (vals @auth/sign-ins)))))
                                               (reset! submitting? true)
                                               (let [params (form-input/inputs-to-key-val @auth/sign-ins)]
                                                 (auth/sign-in (select-keys params [:user/email :user/password]))))
                                             (.preventDefault e))}
]
`
