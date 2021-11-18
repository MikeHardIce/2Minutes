(ns two-minutes.core
  (:require [strigui.core :as gui]
            [clojure.core.async :refer [go-loop timeout <!]]
            [two-minutes.game :as game])
  (:gen-class))

(defn create-back-button
  ([group back-handler] (create-back-button group back-handler 550))
  ([group back-handler y]
  (gui/button! "back" "Nope" {:x 100 :y y :width 200 :color [:white :black] :can-tab? true :group group})
  (gui/update! "back" [:events :mouse-clicked] (fn [_]
                                                 (back-handler #(gui/remove-group! group))))))

(defn game-screen
  [clear-screen back difficulty]
  (clear-screen)
  (let [current-exercise (atom (game/generate-exercise difficulty))
        height (atom 300)]
    (gui/label! "timer" "2:00" {:x 800 :y 50 :width 200 :font-size 24 :font-style [:bold] :group "game-screen"})
    (gui/button! "avion" "(  0^0)" {:x 50 :y @height :width 50 :height 25 :color [:red :white] :group "game-screen"})
    (gui/label! "exercise" (:representation @current-exercise) {:x 375 :y 200 :width 300 :font-size 24 :font-style [:bold] :group "game-screen"})
    (gui/input! "result" "" {:x 375 :y 800 :width 300 :color [:white :black] :selected? true :font-size 16 :group "game-screen"})
    (create-back-button "game-screen" back 900)
    (gui/update! "result" [:events :key-pressed] (fn [wdg key-code]
                                                   (when (= key-code :enter)
                                                     (try
                                                       (when-let [result (-> wdg :value Integer/parseInt)]
                                                         (if (= result (:result @current-exercise))
                                                           (do
                                                             (reset! current-exercise (game/generate-exercise difficulty))
                                                             (gui/update! "exercise" :value (:representation @current-exercise))
                                                             (gui/update! "avion" [:args :y] (swap! height - 5)))
                                                           (gui/update! "avion" [:args :y] (swap! height + 5)))
                                                         (gui/update! "result" :value ""))
                                                       (catch Exception e
                                                         (println (.getMessage e))))))))
  
  (go-loop [minutes 2
            seconds 0]
    (<! (timeout 1000))
    (gui/update! "timer" :value (str (format "%2d" minutes)
                                     ":"
                                     (format "%02d" seconds)))
    (when (and (= minutes 0) (= seconds 30))
      (gui/update! "timer" [:args :color] [:red]))
    (when (> (+ minutes seconds) 0)
      (recur (if (= seconds 0) (dec minutes) minutes)
             (if (= seconds 0) 59 (dec seconds))))))

(defn difficulties
  [clear-screen back]
  (clear-screen)
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
  [clear-screen back]
  (clear-screen)
  (gui/label! "info" "Once upon a time 
                      in a far far universe ..." {:x 375 :y 200 :width 500 :font-size 20 :font-style [:bold] :group "info"})
  
  (create-back-button "info" back))

(defn main-menu
  [clear-screen]
  (clear-screen)
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
