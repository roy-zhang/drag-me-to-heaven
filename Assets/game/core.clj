(ns game.core
  (:use arcadia.core hard.core tween.core)
  (:require [hard.input :as input]
            [arcadia.linear :as l]
            [game.obstacles :as obs]
            [game.util :as util])
  (:import [UnityEngine Vector3 Transform Time Mathf Screen]))

(def stage (atom :start))

(defn move! ^GameObject [^GameObject go ^Vector3 v3]
  (set! (.. go transform position)
        (l/v3+ (.. go transform position)
               v3)))
;(defn def-move! [go _]
;  (let [offset (Vector3. 0.01 0 0)]
;    (move! go offset)))
;
;;(hook+ c1 :update :move def-move!)
;;(hook- c1 :update :move)

;(defn log-collision [obj role-key collision]
;  (log "just bumped into" (.. collision obj name)))
;(hook+ c1 :on-collision-enter :log-collision log-collision)


;(defn rotate [obj role-key]
;  (.. obj transform (Rotate 0 1 0)))
;
;(hook+ (object-named "camera") :update :rotation rotate)
;(hook- (object-named "camera") :update :rotation)

;; same as Camera.main.transform.LookAt(p)
;(defn point-camera [p]
;  (.. Camera/main transform (LookAt p)))

;(obs/make-random-ring nil nil)
;(hook+ (object-named "player") :update :random-ring-update obs/make-random-ring)
;(hook- (object-named "player") :update :random-ring-update)

;(.. (local-position (object-named "empty")) y)

;; the closer to 2, the less it gets advanced
;; other-axis-value lowers the ceiling of movement to
;(defn constrained-movement [current-val going-further? other-axis-val]
;  (if going-further? ; meaning going distal
;    (* (- 4
;          (+ (pow current-val 2) (pow other-axis-val 2)))
;       (if (pos? current-val) 0.05 -0.05))
;    (if (pos? current-val)
;      -0.1
;      0.1)))
;(when (input/key? "w")
;  (move! obj (l/v3 (constrained-movement (X obj)
;                                         (pos? (X obj))
;                                         (Z obj)) 0 0)))
;(when (input/key? "a")
;  (move! obj (l/v3 0 0 (constrained-movement (Z obj)
;                                             (neg? (Z obj))
;                                             (X obj)))))
;(when (input/key? "s")
;  (move! obj (l/v3 (constrained-movement (X obj)
;                                         (neg? (X obj))
;                                         (Z obj)) 0 0)))
;(when (input/key? "d")
;  (move! obj (l/v3 0 0 (constrained-movement (Z obj)
;                                             (pos? (Z obj))
;                                             (X obj)))))

(declare start-game)

(def player-speed 0.075)
(def -player-speed (* -1 player-speed))

(defn handle-input [obj k]
  (when (input/key? "1")
    (do
      (obs/stop-dropping-everything)
      (reset! stage :start)))
  (when (input/key? "2")
    (do
      (reset! stage :fall)
      (start-game :fall)))
  (when (input/key? "3")
    (reset! state :jump))

  (when (input/key? "q")
    (hard.core/rotate! obj (l/v3 0 -6 0)))
  (when (input/key? "e")
    (hard.core/rotate! obj (l/v3 0 6 0)))

  (when (input/key? "w")
    (move! obj (l/v3 player-speed 0 0)))
  (when (input/key? "a")
    (move! obj (l/v3 0 0 -player-speed)))
  (when (input/key? "s")
    (move! obj (l/v3 -player-speed 0 0)))
  (when (input/key? "d")
    (move! obj (l/v3 0 0 player-speed))))


(defn player-collision-fn [obj role-key collision]
  (log collision))

(defn start-title [o])

(defn start-moving-drop! [delay drop-x drop-y]
  (timeline* :loop
             (wait delay)
             #(do (reset! drop-x (Mathf/Clamp
                                   (+ @drop-x (* 0.4 (- (rand) 0.5)))
                                   -1 1))
                  (reset! drop-y (Mathf/Clamp
                                   (+ @drop-y (* 0.4 (- (rand) 0.5)))
                                   -1 1))
                  nil)))

;; mutating state
(def drop-x (atom 0))
(def drop-y (atom 0))

(defn start-falling-objs [o]
  (obs/stop-dropping-everything)
  (start-moving-drop! 0.5 drop-x drop-y)
  (obs/start-dropping-tube! 0.5 drop-x drop-y)
  (obs/start-dropping-rings! 1 drop-x drop-y))

(def player-obj (atom (clone! :player)))
(def camera-obj (atom (hard.core/clone! :camera)))

(def camera-speed 0.15) ;; percent of distance
(defn camera-chase-player [obj role]
  (move! @camera-obj (util/move-towards-vec (local-position @camera-obj)
                                            (local-position @player-obj)
                                            camera-speed)))

(defn start-game [new-stage]
  (reset! stage new-stage)
  (hard.core/clear-cloned!)
  (reset! player-obj (clone! :player))
  (reset! camera-obj (clone! :camera))
  (obs/stop-dropping-everything)

  (when (= :fall new-stage)
    (hard.core/clone! :sun)
    ;(hard.core/clone! :hell-sun)
    (hard.core/clone! :tube)
    (let [head (first (children @player-obj))]
      (hook+ @player-obj :update :handle-input handle-input)
      (hook+ head :on-trigger-enter :player-collision player-collision-fn)
      (hook+ @camera-obj :update :chase-player camera-chase-player))))

(start-game :fall)