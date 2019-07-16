(ns game.chain
  (:use arcadia.core hard.core)
  (:require [hard.input :as input]
            [tween.core :as tween]
            [arcadia.linear :as l]
            [game.obstacles :as obs]
            [game.util :as util])
  (:import [UnityEngine Vector3 Transform Time Mathf Screen SpringJoint LineRenderer Camera Skybox]))

(defn chain-chase-drop [obj role]
  (local-position! obj (l/v3 @drop-x 50 @drop-y)))

(defn update-chain [chain-top _]
  (let [line-renderer (cmpt chain-top LineRenderer)
        top-pos (local-position chain-top)
        bottom-pos (-> @player-obj local-position)]
    (-> line-renderer
        (util/set-line-renderer-verts [top-pos, bottom-pos])
        (.SetWidth 0.1, 0.1))))

(defn clone-chain! []
  (let [chain-top (clone! :chain-top)]
    (hook+ chain-top :fixed-update :move-drop-point #'move-drop-point!)
    (hook+ chain-top :fixed-update :follow-drop-point #'chain-chase-drop)
    (hook+ chain-top :fixed-update :update-chain  #'update-chain)))
