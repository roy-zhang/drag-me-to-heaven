(ns game.core
  (:use arcadia.core arcadia.linear hard.core)
  (:require [hard.input :as input])
  (:import [UnityEngine Vector3 Transform Time]))

(log "abc")
(defonce c1 (create-primitive :sphere "player-body"))

(defn move! [go v3]
  (set! (.. go transform position)
        (v3+ (.. go transform position)
               v3)))

(defn def-move! [go _]
  (let [offset (Vector3. 0.01 0 0)]
    (move! go offset)))

(hook+ c1 :update :move def-move!)
(hook- c1 :update :move)

(defn log-collision [obj role-key collision]
  (log "just bumped into" (.. collision gameObject name)))
(hook+ c1 :on-collision-enter :log-collision log-collision)

(move! c1 (Vector3. 1 0 0))



(defn orbit [^GameObject obj, k]         ; Takes the GameObject and the key this function was attached with
  (let [{:keys [:radius]} (state obj k)] ; Looks up the piece of state corresponding to the key `k`
    (with-cmpt obj [tr Transform]
               (set! (. tr position)
                     (l/v3 (* radius (Mathf/Cos Time/realtimeSinceStartup))
                         0
                         (* radius (Mathf/Sin Time/realtimeSinceStartup)))))))

(defn add-orbiter []
  (let [gobj (create-primitive :cube "Orbiter")]
    (state+ gobj :orbit {:radius 5})          ; set up state
    (hook+ gobj :fixed-update :orbit orbit))) ; set up message callback (hook)

(hook- (object-named "Orbiter") :fixed-update :orbit)


(defn rotate [obj role-key]
  (.. obj transform (Rotate 0 1 0)))

(hook+ (object-named "camera") :update :rotation rotate)
(hook- (object-named "camera") :update :rotation)



; same as Camera.main.transform.LookAt(p)
(defn point-camera [p]
  (.. Camera/main transform (LookAt p)))


;; repl commands here

;(move! c2 (Vector3. 1 0 0))
;(objects-named "cyl")

(defn handle-input [obj k]
  (when (hard.input/key? "a")
    (hard.core/rotate! obj (v3 0 -6 0)))
  (when (hard.input/key? "d")
    (hard.core/rotate! obj (v3 0 6 0)))
  (when (hard.input/key? "w")
    (move! obj (v3 -1 0 0)))
  (when (hard.input/key? "s")
    (hard.core/rotate! obj (v3 1 0 0))))

(hard.core/clone! :segment)


(defn start-game [o]
  (hard.core/clear-cloned!)
  (hard.core/clone! :camera)
  (hard.core/clone! :sun)
  (hard.core/clone! :segment)
  (let [player (hard.core/clone! :player)]
    (hook+ player :update :handle-input handle-input)))

(hook- (object-named "player") :update :handle-input)

(start-game nil)



;; run instructions:
;; start unity, hit play button
;; lein run in the tools.cursive-arcadia-repl
;; start the connect build config on port 7889
;; meta-shift-n switch namespace meta-shift-l load-file meta-enter load top-level

;; install instructions:
;; clone tools.cursive-arcadia-repl into assets
;; clone notabug.com hard.core in the assets folder