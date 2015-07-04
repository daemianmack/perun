(ns io.perun.core
  "Utilies which can be used in base JVM and pods."
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def +perun-meta-key+ :io.perun)

(defn get-perun-meta [fileset]
  (-> fileset meta +perun-meta-key+))

(defn with-perun-meta [fileset data]
  (vary-meta fileset assoc +perun-meta-key+ data))

(def +global-meta-key+ :io.perun.global)

(defn get-global-meta [fileset]
  (-> fileset meta +global-meta-key+))

(defn set-global-meta [fileset data]
  (vary-meta fileset assoc +global-meta-key+ data))

(defn write-to-file [out-file content]
  (doto out-file
    io/make-parents
    (spit content)))

(defn create-file [tmp filepath content]
  (let [file (io/file tmp filepath)]
    (write-to-file file content)))

(defn absolutize-url
  "Makes sure the url starts with slash."
  [url]
  (if (.startsWith url "/")
    url
    (str "/" url)))

(defn relativize-url
  "Remodes slashes url start of the string."
  [url]
  (string/replace url #"^[\/]*" ""))

(defn create-filepath
  "Creates a filepath using system path separator."
  [& args]
  (.getPath (apply io/file args)))

(defn url-to-path
  "Converts a url to filepath."
  [url]
  (apply create-filepath (string/split (relativize-url url) #"\/")))

;;;; map for kv collections

;; These are like ones in medley

;; borrowed from https://github.com/metosin/potpuri/blob/master/src/potpuri/core.cljx#L203-L240

(defn- editable? [coll]
  (instance? clojure.lang.IEditableCollection coll))

(defn- reduce-map [f coll]
  (if (editable? coll)
    (persistent! (reduce-kv (f assoc!) (transient (empty coll)) coll))
    (reduce-kv (f assoc) (empty coll) coll)))

(defn map-keys
  "Map the keys of given associative collection using function."
  {:added "0.2.0"}
  [f coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (xf m (f k) v)))
              coll))

(defn map-vals
  "Map the values of given associative collection using function."
  {:added "0.2.0"}
  [f coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (xf m k (f v))))
              coll))

(defn filter-keys
  {:added "0.2.2"}
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (if (pred k) (xf m k v) m)))
              coll))

(defn filter-vals
  {:added "0.2.2"}
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (if (pred v) (xf m k v) m)))
              coll))
