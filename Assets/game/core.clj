(ns game.core
  (:require [arcadia.core :as arc]
            [arcadia.linear :as l]
            [UnityEngine :as unity]))

(defonce c1 (arc/create-primitive :cube))

(defn def-move! [go _]
  (let [ offset (unity/Vector3. 0.00 0 0)]
    (move! go offset)))


(hook+ c1 :update :move def-move!)

(defn move! [go v3]
  (set! (.. go transform position)
        (l/v3+ (.. go transform position)
               v3)))

(move! c1 (Vector3. 1 0 0))