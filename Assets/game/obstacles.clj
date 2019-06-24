(ns game.obstacles
  (:use arcadia.core arcadia.linear hard.core tween.core)
  (:import [UnityEngine Vector3 Mathf]))

(def y-to-retire -3)
(def y-drop-height 45)

(defn retire-update [obj k]
  (when (> y-to-retire (Y obj))
    (retire obj)))

(defn make-ring [pat x y]
  (dorun
    (map-indexed
      (fn [i s]
        (if (= s true)
          (let [segment (clone! :segment)]
            (position! segment (v3 x y-drop-height (* -1 y)))
            (rotate! segment (v3 0 (* i 60) 0))
            (hook+ segment :update :retire-update retire-update))))
      pat)))

(defn start-dropping-rings! [delay tube-drop-x tube-drop-y]
  (timeline* :loop
             (wait delay)
             (fn []
               (let [pat (repeatedly 6 #(< 5 (rand-int 10)))]
                 (make-ring pat @tube-drop-x @tube-drop-y)))))

;; secs between dropsd

(defn make-tube [x y] ;; absolute x y spawn location
  (let [tube (clone! :tube)]
    (position! tube (v3 x y-drop-height (* -1 y)))
    (hook+ tube :update :retire-update retire-update)))

(defn start-dropping-tube! [delay tube-drop-x tube-drop-y]
  (timeline* :loop
             (wait delay)
             #(do (make-tube @tube-drop-x @tube-drop-y)
                  (reset! tube-drop-x (Mathf/Clamp
                                        (+ @tube-drop-x (* 0.4 (- (rand) 0.5)))
                                        -1 1))
                  (reset! tube-drop-y (Mathf/Clamp
                                        (+ @tube-drop-y (* 0.4 (- (rand) 0.5)))
                                        -1 1))
                  nil)))

;; stop dropping stuff
(defn stop-dropping-everything []
  (map retire (objects-named "tween.core/-mono-obj")))

(stop-dropping-everything)