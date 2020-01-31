(ns clj.ping-pong
  (:import [java.awt.event KeyEvent])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  (:use [quil.core :as q]))


(defn draw-rect [r]
  (rect (:x r) (:y r) (:w r) (:h r)))
(def r-left (atom {:x 10 :y 65 :w 10 :h 70}))
(def r-right (atom {:x 430 :y 65 :w 10 :h 70}))
(def ball (atom {:x 225 :y 100 :w 10 :h 10}))
(def ball-dir (atom [1 0]))
(def frame-r (atom 1000))
(def movement-rate 20)
(def left-side-score (atom 0))
(def right-side-score (atom 0))
(def game-limit 10)

(defn hitfactor [r b]
  (- (/ (- (:y b) (:y r))
        (:h r))
     0.5))

(defn rect-intersects [racket position ball]
  (let [left-position {
                       :l1 {  :x (@racket :x) :y (@racket :y) }
                       :r1 {  :x (+ (@racket :w) (@racket :x)) :y (+ (@racket :y) (@racket :h) ) }
                       :l2 {  :x (@ball :x)  :y (@ball :y) }
                       :r2 {  :x (+ (@ball :w)  (@ball :x)) :y (+ (@ball :y) (@ball :h)) }
                      }
        right-position {
                       :l1 {  :x (@ball :x)  :y (@ball :y) }
                       :r1 {  :x (+(@ball :w)   (@ball :x) )   :y  (+ (@ball :y) (@ball :h) ) }
                       :l2 {  :x (@racket :x) :y (@racket :y) }
                       :r2 {  :x (+ (@racket :w)  (@racket :x) ) :y (+ (@racket :h) (@racket :y)) }
                       }
        positions (if (= position :left) left-position right-position)
        ]
    (cond
      (or (> (->> positions :l1 :x) (->> positions :r2 :x)) (> (->> positions :l2 :x) (->> positions :r1 :x)))   false
      (or (> (->> positions :l1 :y) (->> positions :r2 :y))  (> (->> positions :l2 :y) (->> positions :r1 :y))) false
       :else true)
    )
  )
(defn player1-up []
  (swap! r-left update-in [:y] (fn [x] (- x movement-rate))))
(defn player1-down []
  (swap! r-left update-in [:y] (fn [x] (+ x movement-rate))))

(defn player2-up []
  (swap! r-right update-in [:y] (fn [x] (- x movement-rate))))
(defn player2-down []
  (swap! r-right update-in [:y] (fn [x] (+ x movement-rate))))

(defn key-pressed []
  (cond
    ; left
    (= (key-code) KeyEvent/VK_W)
    (swap! r-left update-in [:y] (fn [x] (- x movement-rate)))
    (= (key-code) KeyEvent/VK_S)
    (swap! r-left update-in [:y] (fn [x] (+ x movement-rate)))
    ; right
    (= (key-code) KeyEvent/VK_UP)
    (swap! r-right update-in [:y] (fn [x] (- x movement-rate)))
    (= (key-code) KeyEvent/VK_DOWN)
    (swap! r-right update-in [:y] (fn [x] (+ x movement-rate)))))

; the more elegant way is by using destructuring:
(defn next-ball [b [dx dy]]
  (assoc b :x (+ (:x b) dx)
           :y (+ (:y b) dy)))

; usage:
(next-ball {:x 0 :y 0 :w 10 :h 10} [1 -1])
; => {:y -1, :x 1, :h 10, :w 10}

;; a nice shade of grey.
(defn draw []
  (background 0x20)
  (fill 0xff)
  ; draw rackets
  (draw-rect @r-left)
  (draw-rect @r-right)
  (draw-rect @ball)
  (get-pixel)
)

(defn update []
  ; move the ball into its direction
  (swap! ball next-ball @ball-dir)
  ; ball hit top or bottom border?
  (when (or (> (:y @ball) 200) (< (:y @ball) 0))
    ; invert y direction
    (swap! ball-dir (fn [[x y]] [x (- y)])))

  ; ball hit the left racket?
  (when (rect-intersects r-left :left ball)
    (let [t (hitfactor @r-left @ball)]
      ; invert x direction, set y direction to hitfactor
      (swap! ball-dir (fn [[x _]] [(- x) t]))))

  ; ball hit the right racket?
  (when (rect-intersects r-right :right ball)
    (let [t (hitfactor @r-right @ball)]
      ; invert x direction, set y direction to hitfactor
      (swap! ball-dir (fn [[x _]] [(- x) t]))))

  (when (< (:x @ball) (:x @r-left))
    (swap! right-side-score inc)
    (reset! ball {:x 225 :y 100 :w 10 :h 10}))

  (when (> (:x @ball) (:x @r-right))
    (swap! left-side-score inc)
    (reset! ball {:x 225 :y 100 :w 10 :h 10}))

  (when (or (= game-limit left-side-score) (= game-limit right-side-score))
    (q/exit))
  )

(defn record []
  ;; Send form params as a json encoded body (POST or PUT)
  (client/post "http://localhost:8080/history" {  :content-type :json
                                                :insecure?          true
                                                :socket-timeout     1000      ;; in milliseconds
                                                :connection-timeout 1000  ;; in milliseconds

                                                  :query-params {
                                                                 :ball-x (@ball :x)
                                                                 :ball-y (@ball :y)
                                                                 :ball-w (@ball :w)
                                                                 :ball-h (@ball :h)
                                                                 :r-left-x (@r-left :x)
                                                                 :r-left-y (@r-left :y)
                                                                 :r-left-w (@r-left :w)
                                                                 :r-left-h (@r-left :h)
                                                                 :r-right-x (@r-right :x)
                                                                 :r-right-y (@r-right :y)
                                                                 :r-right-w (@r-right :w)
                                                                 :r-right-h (@r-right :h)
                                                                 }})
  )

(defn execute []
  (q/defsketch pong
               :title "2d pong game"
               :size [450 200]
               :setup (fn [] (smooth) (no-stroke) (frame-rate @frame-r))
               :draw (fn [] (update) (draw) #_(record))
               :key-pressed key-pressed)
  )