(ns two-minutes.game
  (:require [clojure.string :as str]))

(defn- get-str 
  [func]
  (cond 
    (= func +) "+"
    (= func -) "-"
    (= func *) "*"
    (= func /) ":"
    :else ":D"))

(defn generate-exercise
  [difficulty]
  (let [operators [+ - *]
        operators (if (> difficulty 2) (conj operators /) operators)
        operator (rand-nth operators)
        args (take (+ 2 (rand-int 2)) (repeatedly #(rand-int (* 5 difficulty))))
        representation (str (str/join (str " " (get-str operator) " ") args) " = ?")
        result (apply operator args)]
    {:representation representation :result result}))