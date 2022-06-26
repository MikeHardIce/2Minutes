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

(defn update-avion!
  [avion-name height]
  (when-let [avion (gui/find-by-name "avion")]
    (when (not= (-> avion :y) height)
      (gui/update! avion-name [:args :y] height))))

(defn game-screen
  [clear-screen back difficulty]
  (clear-screen)
  (let [current-exercise (atom (game/generate-exercise difficulty))
        height (atom 300)]
    (gui/label! "timer" "2:00" {:x 800 :y 50 :width 200 :font-size 24 :font-style [:bold] :group "game-screen"})
    (gui/button! "avion" "(  0^0)" {:x 50 :y @height :width 50 :height 25 :color [:red :white] :group "game-screen"})
    (gui/label! "exercise" (:representation @current-exercise) {:x 375 :y 50 :width 300 :font-size 24 :font-style [:bold] :group "game-screen"})
    (gui/input! "result" "" {:x 375 :y 800 :width 300 :color [:white :black] :selected? true :font-size 16 :group "game-screen"})
    (create-back-button "game-screen" back 900)
    (gui/update! "result" [:events :key-pressed] (fn [wdg key-code]
                                                   (when (= key-code :enter)
                                                     (try
                                                       (let [result (-> wdg :value Integer/parseInt)
                                                             new-height (if (= result (:result @current-exercise))
                                                                          (do
                                                                            (reset! current-exercise (game/generate-exercise difficulty))
                                                                            (gui/update! "exercise" :value (:representation @current-exercise))
                                                                            (swap! height - 5))
                                                                          (swap! height + 5))]
                                                         (update-avion! "avion" new-height)
                                                         (gui/update! "result" :value ""))
                                                       (catch Exception e
                                                         (println (.getMessage e)))))))
    
    (go-loop [minutes 2
              seconds 0]
      (<! (timeout 1000))
      (gui/update! "timer" :value (str (format "%2d" minutes)
                                       ":"
                                       (format "%02d" seconds)))
      (when (update-avion! "avion" (swap! height + 4))
        (when (and (= minutes 0) (= seconds 30))
          (gui/update! "timer" [:args :color] [:red]))
        (when (> (+ minutes seconds) 0)
          (recur (if (= seconds 0) (dec minutes) minutes)
                 (if (= seconds 0) 59 (dec seconds))))))))

(defn difficulties
  [widgets fn-back]
  (-> widgets
      (gui/add-button "easy" "I just woke up" {:x 375 :y 200 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
      (gui/add-button "medium" "I had my coffee" {:x 375 :y 275 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
      (gui/add-button "hard" "I can move mountains!!" {:x 375 :y 350 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
      (create-back-button "difficulties" fn-back))
  
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
  (gui/window! 1000 1000 "2Minutes")
  (main-menu (fn [])))
