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
            (position! segment (v3 x y-drop-height y))
            (rotate! segment (v3 0 (* i 60) 0))
            (material-color! segment (color 1 0 0))
            (hook+ segment :update :retire-update  #'retire-update))))
      pat)))

(defn start-dropping-rings! [delay drop-x drop-y]
  (timeline* :loop
             (wait delay)
             (fn []
               (let [pat (repeatedly 6 #(< 5 (rand-int 10)))]
                 (make-ring pat @drop-x @drop-y)))))


(def mountain-y-drop-height 240)

(defn drop-obj [drop-me x y]
  (position! drop-me (v3 x mountain-y-drop-height y))
  (rotate! drop-me (v3 0 (rand 360) 0))
  (hook+ drop-me :update :retire-update  #'retire-update)
  nil)


(defn start-dropping-mountains! [delay drop-x drop-y]
  (timeline* :loop
             (wait delay)
             (fn []
               (drop-obj (clone! :mountain1) @drop-x @drop-y))))

;; stop dropping stuff
(defn stop-dropping-everything []
  (map retire (objects-named "tween.core/-mono-obj")))