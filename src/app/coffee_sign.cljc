(ns app.coffee-sign
  (:require
   #?(:clj [datascript.core :as d])
   [contrib.electric-goog-history :as ghistory]
   [clojure.string :as str]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

#?(:clj (defonce !conn (d/create-conn {})))
(e/def db)

#?(:cljs (defn today->str []
           (-> (js/Date.) 
               (.toLocaleDateString "en-US" 
                                    (clj->js {:day "numeric" :weekday "long" :month "long" :year "numeric"})))))

(defn rand-str [len]
  (str/lower-case (apply str (take len (repeatedly #(char (+ (rand 26) 65)))))))

#?(:clj
   (defn find-contract [db ?url]
     (some-> (d/q '[:find ?e
                    :in $ ?url
                    :where
                    [?e :contract/url ?url]]
                  db ?url) 
             (ffirst))))

(e/defn GenerateButton [inputs url-from-path]
  (let [url (if (str/blank? url-from-path) (rand-str 15) url-from-path)
        contract (assoc inputs
                        :contract/today (today->str)
                        :contract/url url)
        disabled? (->> (map (partial keyword "contract") [:date :time :location :penalty :party-a :party-b])
                       (every? inputs)
                       (not))]
    (dom/div
     (e/server
      (let [id (find-contract db url)
            !contract (d/entity db id)]
        (if (not !contract)
          (e/client
           (dom/button
            (dom/props {:disabled disabled?})
            (dom/text "GENERATE CONTRACT")
            (dom/on "click"
                    (e/fn [_e]
                      (e/server
                       (d/transact! !conn [contract])
                       nil)))))
          (if (str/blank? url-from-path)
            (e/client
             (dom/a (dom/props {:href (str js/location.origin "/" (e/server (:contract/url !contract)))
                                :target "_blank"})
                    (dom/h2 (dom/text "GO TO CONTRACT"))))
            (if (not (:contract/signed? !contract))
              (e/client
               (dom/h2 (dom/text "Party B has to sign")))
              (e/client (dom/div (dom/style {:text-align "center" :font-size "4rem"}) (dom/text "🤝")))))))))))

(e/defn Agreement [url]
  (let [!inputs (atom {})
        inputs (e/watch !inputs)]
    (e/server
     (let [id (find-contract db url)
           !contract (d/entity db id)]
       (e/client
        (dom/div
         (dom/props {:class "agreement" :style {:font-family "Helvetica"}})
         (dom/text "BINDING AGREEMENT FOR COFFEE MEETING")
         (dom/br) (dom/br) (dom/br)
         (dom/text
          "This Binding Agreement (\"Agreement\") is made and entered into on this ")
         (dom/strong (dom/props {:class "handwritten"}) (dom/text (today->str)))
         (dom/text
          " (the \"Effective Date\") between the undersigned parties, hereinafter referred to individually as \"Party A\" and \"Party B\" and collectively as the \"Parties\".")
         (dom/br) (dom/br)
         (dom/text "WHEREAS Party A and Party B desire to meet for a coffee at a mutually agreed-upon date and time; and")
         (dom/br) (dom/br)
         (dom/text "WHEREAS both Parties acknowledge the importance of adhering to the agreed meeting time and ensuring a friendly and punctual encounter;")
         (dom/br) (dom/br)
         (dom/text "NOW, THEREFORE, in consideration of the mutual promises and covenants set forth herein, the Parties agree as follows:")
         (dom/ol
          (dom/li
           (dom/text "Meeting Details:")
           (dom/ol (dom/props {:type "i"})
                   (e/server
                    (if !contract
                      (e/client
                       (dom/li (dom/text "Date:")
                               (ui/input (e/server (:contract/date !contract)) (e/fn [_v])
                                         (dom/props {:class "handwritten" :type "text" :disabled true})))
                       (dom/li (dom/text "Time:")
                               (ui/input (e/server (:contract/time !contract)) (e/fn [_v])
                                         (dom/props {:class "handwritten" :type "text" :disabled true})))
                       (dom/li (dom/text "Location:")
                               (ui/input (e/server (:contract/location !contract)) (e/fn [_v])
                                         (dom/props {:class "handwritten" :type "text" :disabled true}))))
                      (e/client
                       (dom/li (dom/text "Date:")
                               (ui/input (:contract/date inputs) (e/fn [v] (swap! !inputs assoc :contract/date v))
                                         (dom/props {:class "handwritten" :type "text" :placeholder "17/05/2024"})))
                       (dom/li (dom/text "Time:")
                               (ui/input (:contract/time inputs) (e/fn [v] (swap! !inputs assoc :contract/time v))
                                         (dom/props {:class "handwritten" :type "text" :placeholder "2pm"})))
                       (dom/li (dom/text "Location:")
                               (ui/input (:contract/location inputs) (e/fn [v] (swap! !inputs assoc :contract/location v))
                                         (dom/props {:class "handwritten" :type "text" :placeholder "Les Deux Magots"}))))))))
          (dom/li
           (dom/text "Responsibilities:")
           (dom/ol (dom/props {:type "i"})
                   (dom/li (dom/text "Party A and Party B shall arrive promptly at the designated meeting location at the agreed-upon time."))
                   (dom/li (dom/text "Each Party shall bear their own expenses related to the coffee meeting unless otherwise mutually agreed upon."))))
          (dom/li
           (dom/text "Cancellation and Rescheduling:")
           (dom/ol (dom/props {:type "i"})
                   (dom/li (dom/text "In the event that either Party anticipates a delay or inability to attend the coffee meeting at the agreed time, they shall promptly notify the other Party as soon as possible."))
                   (dom/li (dom/text "In case of unforeseen circumstances or a legitimate reason for rescheduling, the Parties may mutually agree to a new date and time for the coffee meeting."))))
          (dom/li
           (dom/text "Confidentiality:")
           (dom/ol (dom/props {:type "i"})
                   (dom/li (dom/text "Both Parties acknowledge and agree that any information disclosed during the coffee meeting shall be treated as confidential unless otherwise agreed upon."))
                   (dom/li (dom/text "Each Party shall exercise reasonable care to protect the confidentiality of any sensitive information shared during the meeting."))))
          (dom/li
           (dom/text "Penalties:")
           (dom/ol (dom/props {:type "i"})
                   (dom/li
                    (dom/text "In the event of a failure by either Party to attend the coffee meeting at the agreed-upon time without providing a valid and timely notice of cancellation or rescheduling, the defaulting Party shall be liable to pay a penalty of ")
                    (e/server 
                     (if !contract 
                       (e/client
                        (ui/input (e/server (:contract/penalty !contract)) (e/fn [_v])
                                  (dom/props {:class "handwritten" :type "text" :disabled true})))
                       (e/client
                        (ui/input (:contract/penalty inputs) (e/fn [v] (swap! !inputs assoc :contract/penalty v))
                                  (dom/props {:class "handwritten" :type "text" :placeholder "1 euro"})))))
                    
                    (dom/text "to the non-defaulting Party."))))
          (dom/li
           (dom/text "Termination:")
           (dom/ol (dom/props {:type "i"})
                   (dom/li (dom/text "This Agreement may be terminated by mutual agreement of the Parties."))))
          (dom/li
           (dom/text "Entire Agreement:")
           (dom/ol (dom/props {:type "i"})
                   (dom/li (dom/text "This Agreement contains the entire understanding and agreement between the Parties with respect to the subject matter hereof and supersedes all prior agreements, written or oral, relating thereto.")))))
         (dom/br)
         (dom/text "IN WITNESS WHEREOF, the Parties hereto have executed this Binding Agreement as of the Effective Date first above written.")
         (dom/br)
         (e/server
          (if !contract
            (e/client
             (dom/div
              (dom/text "Party A: ") 
              (ui/input (e/server (:contract/party-a !contract)) (e/fn [_v])
                        (dom/props {:class "handwritten" :type "text" :disabled true}))
              (dom/span (dom/props {:class "signed"}) (dom/text "√ signed")))
             (dom/div
              (dom/text "Party B: ")
              (ui/input (e/server (:contract/party-b !contract)) (e/fn [_v])
                        (dom/props {:class "handwritten" :type "text" :disabled true}))
              (e/server
               (if (:contract/signed? !contract)
                 (e/client
                  (dom/span (dom/props {:class "signed"}) (dom/text "√ signed")))
                 (e/client
                  (dom/span (dom/props {:class "signed"})
                            (ui/checkbox false 
                                         (e/fn [_v]
                                           (e/server 
                                            (d/transact! !conn [{:db/id (:db/id !contract)
                                                                 :contract/signed? true}])
                                            nil)) 
                                         (dom/props {:id "sign-here"}))
                            (dom/label (dom/props {:for "sign-here"})
                                       (dom/text "click to sign"))))))))
            (e/client
             (dom/div
              (dom/text "Party A: ")
              (ui/input (:contract/party-a inputs) (e/fn [v] (swap! !inputs assoc :contract/party-a v))
                        (dom/props {:class "handwritten" :type "text" :placeholder "your name"})))
             (dom/div
              (dom/text "Party B: ")
              (ui/input (:contract/party-b inputs) (e/fn [v] (swap! !inputs assoc :contract/party-b v))
                        (dom/props {:class "handwritten" :type "text" :placeholder "the other person"}))))))
         (GenerateButton. inputs url)))))))

#?(:clj
   (defn contracts [db]
     (sort
       (d/q '[:find [?e ...]
              :where
              [?e :contract/time]]
            db))))
   
(e/defn TableView []
  (dom/table
   (dom/props {:class "hyperfiddle"})
   (e/server
    (e/for [id (contracts db)]
      (let [!e (d/entity db id)]
        (e/client
         (dom/tr
          (dom/td (dom/text id))
          (dom/td (dom/text (e/server (:contract/url !e))))
          (dom/td (dom/text (e/server (:contract/today !e))))
          (dom/td (dom/text (e/server (:contract/date !e))))
          (dom/td (dom/text (e/server (:contract/time !e))))
          (dom/td (dom/text (e/server (:contract/location !e))))
          (dom/td (dom/text (e/server (:contract/penalty !e))))
          (dom/td (dom/text (e/server (:contract/party-a !e)))))))))))

(e/defn CoffeeSign []
  (e/server
   (binding [db (e/watch !conn)]
     (e/client 
      (let [route ghistory/path]
        ;; (dom/link (dom/props {:rel :stylesheet :href "/todo-list.css"}))
        (dom/h1 (ghistory/Link. "/" "CoffeeSign"))
        (dom/h2 (dom/text "Meeting for coffee? Sign a contract."))
        ;; (TableView.)
        (Agreement. (subs route 1))
        (dom/h4 (dom/text "Made by " )
                (dom/a (dom/props {:href "https://twitter.com/teawaterwire"
                                   :target "_blank"}) 
                       (dom/text "teawaterwire"))
                (dom/text " • ")
                (dom/text "Built with ")
                (dom/a (dom/props {:href "https://github.com/hyperfiddle/electric"
                                   :target "_blank"}) 
                       (dom/text "Electric"))
                (dom/text " • ")
                (dom/text "Open ")
                (dom/a (dom/props {:href "https://github.com/teawaterwire/CoffeeSign/blob/main/src/app/coffee_sign.cljc"
                                   :target "_blank"}) 
                       (dom/text "source"))))))))