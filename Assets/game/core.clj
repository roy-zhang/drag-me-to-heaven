(ns game.core
  (:use arcadia.core hard.core)
  (:require [hard.input :as input]
            [tween.core :as tween]
            [arcadia.linear :as l]
            [game.obstacles :as obs]
            [game.util :as util])
  (:import [UnityEngine Vector3 Transform Time Mathf Screen SpringJoint LineRenderer Camera Skybox]))

(def stage (atom :start))
(def drop-x (atom 0))
(def drop-y (atom 0))
(def player-obj (atom (clone! :player)))
(def camera-obj (atom (clone! :start-camera)))


(defn log-collision [obj role-key collision]
  (log "just bumped into" (.. collision obj name)))

(declare start-stage!)

(def player-speed 0.075)
(def -player-speed (* -1 player-speed))

(defn handle-stage-select-input []
  (cond
    (input/key? "0") (start-stage! :reset)
    (input/key? "1") (start-stage! :start)
    (input/key? "2") (start-stage! :fall)
    :else nil))

(defn handle-input [obj k]
  (handle-stage-select-input)

  (when (input/key? "1")
    (do
      (obs/stop-dropping-everything)
      (reset! stage :start)))
  (when (input/key? "2")
    (do
      (reset! stage :fall)
      (start-stage! :fall)))
  (when (input/key? "3")
    (reset! state :jump))

  (when (input/key? "q")
    (hard.core/rotate! obj (l/v3 0 -6 0)))
  (when (input/key? "e")
    (hard.core/rotate! obj (l/v3 0 6 0)))

  (when (input/key? "w")
    (util/move! obj (l/v3 player-speed 0 0)))
  (when (input/key? "a")
    (util/move! obj (l/v3 0 0 -player-speed)))
  (when (input/key? "s")
    (util/move! obj (l/v3 -player-speed 0 0)))
  (when (input/key? "d")
    (util/move! obj (l/v3 0 0 player-speed))))



;(defmutable dropper-x [^float x])
;(defmutable dropper-y [^float y])

(defn rand-adj [i]
  (Mathf/Clamp
    (+ i (* 0.3 (- (rand) 0.5)))
    -20 20))

(defn move-drop-point! [_ _]
  (swap! drop-x rand-adj)
  (swap! drop-y rand-adj))

;(println @drop-x " " @drop-y)
;(println (local-position (object-named "chain-top")))

(defn camera-chase-player [obj role]
  (util/move! obj (util/move-towards-vec (local-position obj)
                                         (local-position @player-obj)
                                         0.15))) ; percent of distance

(defn chain-chase-drop [obj role]
  (local-position! obj (l/v3 @drop-x 50 @drop-y)))

(defn update-chain [chain-top _]
  (let [line-renderer (cmpt chain-top LineRenderer)
        top-pos (local-position chain-top)
        bottom-pos (-> @player-obj local-position)]
    (-> line-renderer
        (util/set-line-renderer-verts [top-pos, bottom-pos])
        (.SetWidth 0.1, 0.1))))

(defn start-falling [o]
  (obs/stop-dropping-everything)
  (reset! drop-x 0)
  (reset! drop-y 0)
  (let [game-state (clone! :game-state)]
    (hook+ game-state :fixed-update :moving-drop #'move-drop-point!)
    (obs/start-dropping-mountains! 5 drop-x drop-y)
    (obs/start-dropping-rings! 1 drop-x drop-y)))

(obs/start-dropping-mountains! 5 drop-x drop-y)

(obs/start-dropping-rings! 1 drop-x drop-y)

;; ------------ start stage

(defn slow-pan [camera-obj _]
  (.. camera-obj transform (LookAt (local-position @player-obj)))
  (local-position! camera-obj (l/v3 (* 7 (Mathf/Cos (/ Time/realtimeSinceStartup 5)))
                                    (Y camera-obj)
                                    (* 7 (Mathf/Sin (/ Time/realtimeSinceStartup 5))))))

(defn start-going-up! [speed]
  (fn [obj _]
    (util/move! obj (l/v3 0 speed 0))))

(defn transition-to-fall-on-input [_ _]
  (handle-stage-select-input)
  (when (and (= @stage :start)
             (or (input/key? "w") (input/key? "w") (input/key? "w") (input/key? "w")))
    (tween/timeline
      [#(do (log "start")
            (reset! stage :transitioning-to-fall) nil)
       (tween/wait 1)
       #(do
          (let [chain-top (clone! :chain-top)]
            (hook+ chain-top :update :follow-drop-point #'chain-chase-drop)
            (hook+ chain-top :update :update-chain  #'update-chain))
          nil);; play little rise animation, then transition to :fall
       (tween/wait 2)
       #(do (hook+ @player-obj :fixed-update :start-going-up (start-going-up! 0.45))
            (hook+ @camera-obj :fixed-update :start-going-up (start-going-up! 0.3))
            nil)
       (tween/wait 2.0)
       #(do
          (start-stage! :fall) nil)])))


(defn start-stage! [new-stage]
  (reset! stage new-stage)
  (hard.core/clear-cloned!)
  (obs/stop-dropping-everything)

  (when (= new-stage :start)
    (reset! camera-obj (clone! :start-camera))
    (reset! player-obj (clone! :player))
    (clone! :title-text)
    (-> (clone! :dunes)
      (material-color! (color 0 0 0)))
    (hook+ @camera-obj :fixed-update :slow-pan #'slow-pan)
    (hook+ @player-obj :update :transition-to-fall-on-input #'transition-to-fall-on-input))

  (when (= new-stage :start-to-fall))

  (when (= new-stage :fall)
    (reset! player-obj (clone! :player))
    (reset! camera-obj (clone! :fall-camera))
    (reset! drop-x 0)
    (reset! drop-y 0)

    (let [head (first (children @player-obj))]
      (hook+ @player-obj :update :handle-input  #'handle-input)
      (hook+ head :on-trigger-enter :player-collision  #'log-collision)
      (hook+ @camera-obj :update :chase-player  #'camera-chase-player)
      (let [chain-top (clone! :chain-top)]
        (hook+ chain-top :fixed-update :move-drop-point #'move-drop-point!)
        (hook+ chain-top :fixed-update :follow-drop-point #'chain-chase-drop)
        (hook+ chain-top :fixed-update :update-chain  #'update-chain)))))

(start-stage! :reset)

(start-stage! :fall)

(start-stage! :start)
