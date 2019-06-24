(ns game.util
  (:require arcadia.core
            arcadia.linear
            clojure.string
            [arcadia.linear :as l])
  (:import [UnityEngine Vector3]))

(defn move-towards-vec ^UnityEngine.Vector3 [^UnityEngine.Vector3 v3a ^UnityEngine.Vector3 v3b magnitude-adjustment]
  (let [nv (l/v3- v3b v3a)
        nv (l/v3 (. nv x) 0 (. nv z))]
    (l/v3* nv magnitude-adjustment)))