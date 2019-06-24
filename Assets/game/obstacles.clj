(ns game.obstacles
  (:use arcadia.core arcadia.linear hard.core tween.core))

(def y-to-retire -3)

(defn ring-update [obj k]
  (when (> y-to-retire (Y obj))
    (retire obj)))

(defn make-ring [pat]
  (dorun
    (map-indexed
      (fn [i s]
        (if (= s true)
          (let [segment (clone! :segment)]
            (rotate! segment (v3 0 (* i 60) 0))
            (hook+ segment :update :ring-update ring-update))))
      pat)))

(defn make-random-ring [obj k]
  (let [pat (repeatedly 6 #(< 5 (rand-int 10)))]
    (make-ring pat)))

(make-random-ring nil nil)

(defn start-dropping-rings! [intensity]
  (timeline* :loop
             (wait intensity)
             #(do (make-random-ring nil nil) nil)))

;; secs between drops
(start-dropping-rings! 0.5)

(defn make-random-wall [pat]
  (dorun
    (map-indexed
      (fn [i s]
        (if (= s true)
          (let [segment (clone! :wall-plank)]
            (rotate! segment (v3 0 (* i 60) 0))
            (hook+ segment :update :ring-update ring-update))))
      pat)))


;; stop dropping stuff
(map retire (objects-named "tween.core/-mono-obj"))