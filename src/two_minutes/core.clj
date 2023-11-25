(ns two-minutes.core
  (:require [strigui.core :as gui]
            [clojure.core.async :refer [go-loop timeout <!]]
            [two-minutes.game :as game])
  (:import [java.awt Color])
  (:gen-class))

(defonce color {:background Color/white
                :text Color/black
                :border Color/black})

(defn create-back-button 
  ([widgets group fn-back] (create-back-button widgets group fn-back 500))
  ([widgets group fn-back y]
   (-> widgets
       (gui/add-button "main-window" "back" "Back" {:x 500 :y y :width 350 :color color :can-tab? true :group group})
       (gui/attach-event "back" :mouse-clicked (fn [wdgs _]
                                                 (-> wdgs
                                                     (gui/remove-widgets-by-group group)
                                                     fn-back))))))

(defn game-screen
  [widgets fn-back difficulty]
  (let [{:keys [result representation]} (game/generate-exercise difficulty)
        canceled (atom false)
        height 300
        widgets (-> widgets
                    (gui/add-label "main-window" "timer" "2:00" {:x 800 :y 200  :width 200 :font-size 24 :font-style [:bold] :color {:text Color/black} :group "game-screen"})
                    (gui/add-button "main-window" "avion" "( = 0^0)" {:x 50 :y height  :width 50 :height 25 :color {:background Color/green :text Color/white} :group "game-screen"})
                    (gui/add-label "main-window" "exercise" representation {:x 375 :y 200  :width 300 :font-size 24 :font-style [:bold] :color {:text Color/black} :group "game-screen" :result result})
                    (gui/add-input "main-window" "result" "" {:x 375 :y 800  :width 300 :color color :selected? true :can-tab? true :font-size 16 :group "game-screen"})
                    (create-back-button "game-screen" (fn [wdgs] 
                                                        (reset! canceled true)
                                                        (fn-back wdgs)) 900)
                    (gui/attach-event "result" :key-pressed (fn [wdgs {:keys [widget code]}]
                                                              (if (not= code 10)
                                                                wdgs
                                                                (let [name (:name widget)
                                                                      result (-> wdgs (get name) :value parse-long)
                                                                      current-exercise {:result (-> wdgs (get "exercise") :props :result) :representation (-> wdgs (get "exercise") :value)}
                                                                      [diff-height new-exercise] (if (= result (:result current-exercise))
                                                                                                   [-10 (game/generate-exercise difficulty)]
                                                                                                   [10 current-exercise])]
                                                                  (-> wdgs
                                                                      (update-in ["avion" :props :y] (partial + diff-height))
                                                                      (assoc-in ["avion" :props :color :background] (if (pos? diff-height)
                                                                                                                      Color/red
                                                                                                                      Color/green))
                                                                      (assoc-in ["exercise" :value] (:representation new-exercise))
                                                                      (assoc-in ["exercise" :props :result] (:result new-exercise))
                                                                      (assoc-in ["result" :value] "")))))))]
    (go-loop [minutes 2
              seconds 0]
      (<! (timeout 1000))
      (when (not @canceled)
        (gui/swap-widgets! (fn [wdgs]
                             (-> wdgs
                                 (assoc-in ["timer" :value] (str (format "%2d" minutes)
                                                                 ":"
                                                                 (format "%02d" seconds)))
                                 (assoc-in ["timer" :props :color :text] (if (and (= minutes 0) (< seconds 30))
                                                                           Color/red
                                                                           Color/black))
                                 (update-in ["avion" :props :y] (partial + 4))
                                 (update-in ["avion" :props :x] (partial + 7))
                                 (assoc-in ["avion" :props :color :background] (if (and (= minutes 0) (< seconds 30))
                                                                                 Color/red
                                                                                 Color/green)))))
        (when (and (> (+ minutes seconds) 0) (not @canceled))
          (recur (if (= seconds 0) (dec minutes) minutes)
                 (if (= seconds 0) 59 (dec seconds))))))
    widgets))

(defn difficulties
  [widgets fn-back]
  (-> widgets
      (gui/add-button "main-window" "easy" "I just woke up" {:x 375 :y 200 :width 300 :color color :can-tab? true :group "difficulties"})
      (gui/add-button "main-window" "medium" "I had my coffee" {:x 375 :y 275 :width 300 :color color :can-tab? true :group "difficulties"})
      (gui/add-button "main-window" "hard" "I can move mountains!!" {:x 375 :y 350 :width 300 :color color :can-tab? true :group "difficulties"})
      (gui/attach-event "easy" :mouse-clicked (fn [wdgs _]
                                                (-> wdgs 
                                                    (gui/remove-widgets-by-group "difficulties")
                                                    (game-screen fn-back 1))))
      (gui/attach-event "medium" :mouse-clicked (fn [wdgs _]
                                                (-> wdgs
                                                    (gui/remove-widgets-by-group "difficulties")
                                                    (game-screen #(difficulties % fn-back) 2))))
      (gui/attach-event "hard" :mouse-clicked (fn [wdgs _]
                                                (-> wdgs
                                                    (gui/remove-widgets-by-group "difficulties")
                                                    (game-screen #(difficulties % fn-back) 3))))
      (create-back-button "difficulties" fn-back)))
  
(defn info-view 
  [widgets fn-back]
  (-> widgets 
      (gui/add-label "main-window" "info" "Once upon a time
                      in a far far universe ..." {:x 375 :y 200  :width 500 :font-size 20 :font-style [:bold] :group "info"})
      (create-back-button "info" fn-back)))

(defn main-menu
  [widgets]
  (-> widgets
      (gui/add-button "main-window" "start" "Engine start!" {:x 375 :y 200  :width 350 :color color :can-tab? true :group "main"})
      (gui/add-button "main-window" "info" "What is this?" {:x 375 :y 275  :width 350 :color color :can-tab? true :group "main"})
      (gui/add-button "main-window" "exit" "I don't want anymore ..." {:x 375 :y 350  :width 350 :color color :can-tab? true :group "main"})
      (gui/attach-event "exit" :mouse-clicked (fn [wdgs _] (gui/close-window! wdgs "main-window")))
      (gui/attach-event "info" :mouse-clicked (fn [wdgs _] 
                                                (-> wdgs 
                                                    (gui/remove-widgets-by-group "main")
                                                    (info-view main-menu))))
      (gui/attach-event "start" :mouse-clicked (fn [wdgs _]
                                                 (-> wdgs
                                                     (gui/remove-widgets-by-group "main")
                                                     (difficulties main-menu))))))

(defn -main
  ""
  [& args]
  (gui/swap-widgets! #(-> % 
                          (gui/add-window "main-window" 0 0 1000 1000 "2Minutes" {})
                          main-menu)))
