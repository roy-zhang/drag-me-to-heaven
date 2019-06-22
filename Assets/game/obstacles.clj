(ns game.obstacles
  (:use arcadia.core arcadia.linear hard.core))

(defn ring-update [obj k]
  (if (> -25 (Y obj))
    (retire obj)
    (local-position! obj (v3+ (local-position obj)
                              (v3 0 -0.1 0)))))

(defn make-ring [pat]
  (let [ring (clone! :empty)]
    (dorun
      (map-indexed
        (fn [i s]
          (if (= s true)
            (parent!
              (gobj (rotate! (clone! :segment)
                             (v3 0 (* i 60) 0)))
              ring)))
        pat))
    (hook+ ring :update :ring-update ring-update)))

(defn make-random-ring [obj k]
  (let [pat (repeatedly 6 #(< 5 (rand-int 10)))]
    (make-ring pat)))