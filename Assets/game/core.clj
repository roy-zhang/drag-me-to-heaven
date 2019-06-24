(ns game.core
  (:use arcadia.core hard.core)
  (:require [hard.input :as input]
            [arcadia.linear :as l]
            [game.obstacles :as obs])
  (:import [UnityEngine Vector3 Transform Time]))

(log "abc")
(defonce c1 (create-primitive :sphere "player-body"))

(defn move! [go v3]
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

(obs/make-random-ring nil nil)

(hook+ (object-named "player") :update :random-ring-update obs/make-random-ring)
(hook- (object-named "player") :update :random-ring-update)

(.. (local-position (object-named "empty")) y)

;; the closer to 2, the less it gets advanced
(defn constrained-movement [current-val going-further?]
  (if going-further? ; meaning outside and going further
    (* (- 2 (abs current-val)) (if (pos? current-val) 0.05 -0.05))
    (if (pos? current-val)
      -0.1
      0.1)))

(defn handle-input [obj k]
  (when (input/key? "q")
    (hard.core/rotate! obj (l/v3 0 -6 0)))
  (when (input/key? "e")
    (hard.core/rotate! obj (l/v3 0 6 0)))

  (when (input/key? "a")
    (move! obj (l/v3 (constrained-movement (X obj) (neg? (X obj))) 0 0)))
  (when (input/key? "d")
    (move! obj (l/v3 (constrained-movement (X obj) (pos? (X obj))) 0 0)))
  (when (input/key? "w")
    (move! obj (l/v3 0 0 (constrained-movement (Z obj) (neg? (Z obj))))))
  (when (input/key? "s")
    (move! obj (l/v3 0 0 (constrained-movement (Z obj) (pos? (Z obj)))))))

(defn player-collision-fn [obj role-key collision]
  (log collision))

(defn start-game [o]
  (hard.core/clear-cloned!)
  (hard.core/clone! :camera)
  (hard.core/clone! :sun)
  ;(hard.core/clone! :hell-sun)
  (hard.core/clone! :tube)

  (let [player (hard.core/clone! :player)
        head (first (children player))]
    (hook- head :on-trigger-enter :player-collision)
    (hook- player :update :handle-input)
    (hook+ head :on-trigger-enter :player-collision player-collision-fn)
    (hook+ player :update :handle-input handle-input)))

(start-game nil)