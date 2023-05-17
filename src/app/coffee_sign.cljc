(ns app.coffee-sign
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui]))

#?(:cljs (defn today->str []
           (-> (js/Date.) 
               (.toLocaleDateString "en-US" 
                                    (clj->js {:day "numeric" :weekday "long" :month "long" :year "numeric"})))))

(e/defn Signatures [!inputs]
  (dom/div
   (dom/text "Party A: ")
   (ui/input (:party-a !inputs) (e/fn [v] (swap! !inputs assoc :party-a v))
                          (dom/props {:class "handwritten" :type "text" :placeholder "your name"})))
  (dom/div
   (dom/text "Party B: ")
   (ui/input (:party-a !inputs) (e/fn [v] (swap! !inputs assoc :party-a v))
                          (dom/props {:class "handwritten" :type "text" :placeholder "the other person"}))))

(e/defn Agreement []
  (let [!inputs (atom {})
        inputs (e/watch !inputs)]
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
               (dom/li (dom/text "Date:")
                       (ui/input (:date inputs) (e/fn [v] (swap! !inputs assoc :date v))
                                 (dom/props {:class "handwritten" :type "text" :placeholder "17/05/2024"})))
               (dom/li (dom/text "Time:")
                       (ui/input (:time inputs) (e/fn [v] (swap! !inputs assoc :time v))
                                 (dom/props {:class "handwritten" :type "text" :placeholder "2pm"})))
               (dom/li (dom/text "Location:")
                       (ui/input (:location inputs) (e/fn [v] (swap! !inputs assoc :location v))
                                 (dom/props {:class "handwritten" :type "text" :placeholder "Les Deux Magots"})))))
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
                (ui/input (:penalty inputs) (e/fn [v] (swap! !inputs assoc :penalty v))
                          (dom/props {:class "handwritten" :type "text" :placeholder "1 eth"}))
                (dom/text "to the non-defaulting Party."))))
      (dom/li
       (dom/text "Termination:")
       (dom/ol (dom/props {:type "i"})
               (dom/li (dom/text "This Agreement shall terminate upon the completion of the coffee meeting at the agreed-upon time or upon mutual agreement of the Parties to terminate the meeting earlier."))))
      (dom/li
       (dom/text "Entire Agreement:")
       (dom/ol (dom/props {:type "i"})
               (dom/li (dom/text "This Agreement contains the entire understanding and agreement between the Parties with respect to the subject matter hereof and supersedes all prior agreements, written or oral, relating thereto.")))))
     (dom/br)
     (dom/text "IN WITNESS WHEREOF, the Parties hereto have executed this Binding Agreement as of the Effective Date first above written.")
     (dom/br)
     (Signatures. !inputs))))
   

(e/defn CoffeeSign []
  (e/client 
   (dom/link (dom/props {:rel :stylesheet :href "/todo-list.css"}))
   (dom/h1 (dom/text "CoffeeSign"))
   (dom/h2 (dom/text "Meeting for coffee? Send them a contract."))
   (Agreement.)
   (dom/h4 (dom/text "Made by " )
           (dom/a (dom/props {:href "https://twitter.com/teawaterwire"}) 
                                       (dom/text "teawaterwire")))))