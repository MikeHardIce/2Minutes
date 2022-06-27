(ns two-minutes.core
  (:require [strigui.core :as gui]
            [clojure.core.async :refer [go-loop timeout <!]]
            [two-minutes.game :as game])
  (:gen-class))

(defn create-back-button 
  [widgets group fn-back]
  (-> widgets
      (gui/add-button "back" "Back" {:x 500 :y 500 :width 350 :color [:white :black] :can-tab? true :group group})
      (gui/attach-event "back" :mouse-clicked (fn [wdgs _]
                                                (-> wdgs
                                                    (gui/remove-widget-group group)
                                                    (fn-back wdgs))))))

(defn game-screen
  [widgets fn-back difficulty]
  (let [current-exercise (atom (game/generate-exercise difficulty))
        height 300
        widgets (-> widgets
                    (gui/add-label "timer" "2:00" {:x 800 :y 50 :width 200 :font-size 24 :font-style [:bold] :group "game-screen"})
                    (gui/add-button "avion" "(  0^0)" {:x 50 :y height :width 50 :height 25 :color [:red :white] :group "game-screen"})
                    (gui/add-label "exercise" (:representation @current-exercise) {:x 375 :y 50 :width 300 :font-size 24 :font-style [:bold] :group "game-screen"})
                    (gui/add-input "result" "" {:x 375 :y 800 :width 300 :color [:white :black] :selected? true :font-size 16 :group "game-screen"})
                    (create-back-button "game-screen" fn-back)
                    (gui/attach-event "result" :key-pressed (fn [wdgs wdg key-code]
                                                              (if (not= key-code 10)
                                                                wdgs
                                                                (let [result (-> wdg :value Integer/parseInt)
                                                                      [diff-height new-exercise] (if (= result (:result @current-exercise))
                                                                                                   [5 (reset! current-exercise (game/generate-exercise difficulty))]
                                                                                                   [-5 @current-exercise])]
                                                                  (-> wdgs
                                                                      (update-in ["avion" :args :y] (partial inc diff-height))
                                                                      (update-in ["exercise" :value] :representation new-exercise)
                                                                      (assoc-in ["result" :value] "")))))))]
    (go-loop [minutes 2
              seconds 0]
      (<! (timeout 1000))
      (gui/swap-widgets! (fn [wdgs]
                           (-> wdgs 
                               (assoc-in ["timer" :value] (str (format "%2d" minutes)
                                                               ":"
                                                               (format "%02d" seconds)))
                               (assoc-in ["timer" :args :color] (if (and (= minutes 0) (= seconds 30))
                                                                  [:red]
                                                                  [:black]))
                               (update-in ["avion" :args :y] (partial + 4)))))
      (when (> (+ minutes seconds) 0)
        (recur (if (= seconds 0) (dec minutes) minutes)
               (if (= seconds 0) 59 (dec seconds)))))
    widgets))

(defn difficulties
  [widgets fn-back]
  (-> widgets
      (gui/add-button "easy" "I just woke up" {:x 375 :y 200 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
      (gui/add-button "medium" "I had my coffee" {:x 375 :y 275 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
      (gui/add-button "hard" "I can move mountains!!" {:x 375 :y 350 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
      (create-back-button "difficulties" fn-back)
      )
  
  (letfn [(create-game-screen-link [widget-name difficulty]
            (gui/update! widget-name [:events :mouse-clicked] (fn [_] (game-screen #(gui/remove-group! "difficulties") back difficulty))))]
    (gui/button! "easy" "I just woke up" {:x 375 :y 200 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
    (gui/button! "medium" "I had my coffee" {:x 375 :y 275 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
    (gui/button! "hard" "I can move mountains!!" {:x 375 :y 350 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})

    (create-game-screen-link "easy" 1)
    (create-game-screen-link "medium" 2)
    (create-game-screen-link "hard" 3)

    (create-back-button "difficulties" back)))
  
(defn info 
  [widgets fn-back]
  (-> widgets
      (gui/add-label "info" "Once upon a time 
                      in a far far universe ..." {:x 375 :y 200 :width 500 :font-size 20 :font-style [:bold] :group "info"})
      (create-back-button "info" fn-back)))

(defn main-menu
  [widgets]
  (-> widgets
      (gui/add-button "start" "Engine start!" {:x 100 :y 200 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
      (gui/add-button "info" "What is this?" {:x 100 :y 275 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
      (gui/add-button "exit" "I don't want anymore ..." {:x 100 :y 350 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
      (gui/attach-event "exit" :mouse-clicked (fn [_ _] (gui/close-window!)))
      (gui/attach-event "info" :mouse-clicked (fn [wdgs _] 
                                                (-> wdgs 
                                                    (gui/remove-widget-group "main-menu")
                                                    (info main-menu))))
      (gui/attach-event "start" :mouse-clicked (fn [wdgs _]
                                                 (-> wdgs
                                                     (gui/remove-widget-group "main-menu")
                                                     (difficulties main-menu)))))

  (gui/button! "start" "Engine start!" {:x 100 :y 200 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
  (gui/button! "info" "What is this?" {:x 100 :y 275 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
  (gui/button! "exit" "I don't want anymore ..." {:x 100 :y 350 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})

  (gui/update! "exit" [:events :mouse-clicked] (fn [_] (gui/close-window)))
  
  (gui/update! "info" [:events :mouse-clicked] (fn [_]
                                                  (info #(gui/remove-group! "main-menu") main-menu)))

  (gui/update! "start" [:events :mouse-clicked] (fn [_]
                                                  (difficulties #(gui/remove-group! "main-menu") main-menu))))

(defn -main
  ""
  [& args]
  (gui/window! 0 0 1000 1000 "2Minutes")
  (main-menu (fn [])))
