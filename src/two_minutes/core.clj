(ns two-minutes.core
  (:require [strigui.core :as gui])
  (:gen-class))

(defn difficulties
  [clear-screen back]
  (clear-screen)
  (gui/button! "easy" "I just woke up" {:x 375 :y 200 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
  (gui/button! "medium" "I had my coffee" {:x 375 :y 275 :width 300 :color [:white :black] :can-tab? true :group "difficulties"})
  (gui/button! "hard" "I can move mountains!!" {:x 375 :y 350 :width 300 :color [:white :black] :can-tab? true :group "difficulties"}))


(defn main-menu
  [clear-screen]
  (clear-screen)
  (gui/button! "start" "Engine start!" {:x 100 :y 200 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
  (gui/button! "info" "What is this?" {:x 100 :y 275 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
  (gui/button! "exit" "I don't want anymore ..." {:x 100 :y 350 :width 350 :color [:white :black] :can-tab? true :group "main-menu"})
  
  (gui/update! "start" [:events :mouse-clicked] (fn [wdg]
                                                  (difficulties #(gui/remove-group! "main-menu") main-menu))))

(defn -main
  ""
  [& args]
  (gui/window! 1000 1000 "2Minutes")
  (main-menu (fn [])))
