(ns game.util
  (:require arcadia.core
            arcadia.linear
            clojure.string
            [arcadia.linear :as l])
  (:import [UnityEngine Vector3 LineRenderer]))

(defn move-towards-vec ^UnityEngine.Vector3 [^UnityEngine.Vector3 v3a ^UnityEngine.Vector3 v3b magnitude-adjustment]
  (let [nv (l/v3- v3b v3a)
        nv (l/v3 (. nv x) 0 (. nv z))]
    (l/v3* nv magnitude-adjustment)))

(defn move! ^GameObject [^GameObject go ^Vector3 v3]
  (set! (.. go transform position)
        (l/v3+ (.. go transform position)
               v3)))

(defn- cludfn
  ([] nil)
  ([_] nil)
  ([_ _] nil)
  ([_ _ _] nil))

(defn bezier-verts [start end sectionsCount])

(defn set-line-renderer-verts ^LineRenderer [^LineRenderer lr, verts]
  (.SetVertexCount lr (count verts))
  (transduce
    (keep-indexed ;; should be map-indexed, transducer not in ClojureCLR yet
      (fn [i ^Vector3 v]
        (.SetPosition lr i v)))
    cludfn
    verts)
  lr)