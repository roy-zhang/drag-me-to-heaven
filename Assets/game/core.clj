(ns game.core
  (:use arcadia.core hard.core)
  (:require [hard.input :as input]
            [arcadia.linear :as l]
            [game.obstacles :as obs])
  (:import [UnityEngine Vector3 Transform Time]))

(defn move! [go v3]
  (set! (.. go transform position)
        (l/v3+ (.. go transform position)
               v3)))

(def stage (atom :start))

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
(defn constrained-movement [current-val going-further? other-axis-val]
  (if going-further? ; meaning outside and going further
    (* (- 2 (abs current-val)) (if (pos? current-val) 0.05 -0.05))
    (if (pos? current-val)
      -0.1
      0.1)))

(cos 3.14)

(defn handle-input [obj k]
  (when (input/key? "1")
    (do
      (obs/stop-dropping-everything)
      (reset! stage :start)))
  (when (input/key? "2")
    (do
      (obs/start-dropping-rings! 1)
      (reset! stage :fall)))
  (when (input/key? "3")
    (reset! state :jump))

  (when (input/key? "q")
    (hard.core/rotate! obj (l/v3 0 -6 0)))
  (when (input/key? "e")
    (hard.core/rotate! obj (l/v3 0 6 0)))

  (when (input/key? "a")
    (move! obj (l/v3 (constrained-movement (X obj)
                                           (neg? (X obj))
                                           (Z obj)) 0 0)))
  (when (input/key? "d")
    (move! obj (l/v3 (constrained-movement (X obj)
                                           (pos? (X obj))
                                           (Z obj)) 0 0)))
  (when (input/key? "w")
    (move! obj (l/v3 0 0 (constrained-movement (Z obj)
                                               (neg? (Z obj))
                                               (X obj)))))
  (when (input/key? "s")
    (move! obj (l/v3 0 0 (constrained-movement (Z obj)
                                               (pos? (Z obj))
                                               (X obj))))))

(defn player-collision-fn [obj role-key collision]
  (log collision))

(defn start-title [o])


(defn start-fall [o])

(defn start-game [o]
  (reset! stage :start)
  (obs/stop-dropping-everything)

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

(def t1 (clone! :tube))

(obs/stop-dropping-everything)
(start-game nil)