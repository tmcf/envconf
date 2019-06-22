; envconf is distributed under the Eclipse Public License, the same as Clojure.
;
; The core of this code is strongly based on the environ project:
; https://github.com/weavejester/environ
; with the following license:
; Copyright © 2016 James Reeves
; Distributed under the Eclipse Public License, the same as Clojure.


(ns errigal751.envconf.core
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (import [java.lang.System]))


(defn- keywordize [s]
  (-> (str/lower-case s)
      (str/replace "_" "-")
      (str/replace "." "-")
      (keyword)))

(defn- sanitize-key [k]
  (let [s (keywordize (name k))]
    s))

(defn- sanitize-val [k v]
  (if (string? v)
    v
    (str v)))

(defn- read-system-env []
  (->> (System/getenv)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-system-props []
  (->> (System/getProperties)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-env-file [f]
  (if-let [env-file (io/file f)]
    (if (.exists env-file)
      (into {} (for [[k v] (edn/read-string (slurp env-file))]
                 [(sanitize-key k) (sanitize-val k v)])))))


(defn- get-sys-prop-env
  "Look up a key by:
   - System/getProperty
   - System/getenv

   returning default if neither found. Property has priority over env
   "
  [k & [dvalue]]
  (or (System/getProperty k)
      (System/getenv k)
      dvalue))

(defn read-env-files
  "Read from a list of edn files merging the results."
  [flist]
  (reduce (fn [r file]
            (merge r (read-env-file file)))
          {} flist))

(defn read-envconf
  "Read environment merging edn files specified by:
   - ENVCONF_PATH (default: '.envconf:.envconf-instance')
   - system environment (getProperty)
   - system props (getEnv)
   - ENVCONF_PATH_FINAL (default: '.envconf-final:.envconf-final-instance')
   "
  []
  (let [pathsplit #";|:"
        default-epath ".envconf:.envconf-instance"
        default-final-epath ".envconf-final:.envconf-final-instance"
        env-files (clojure.string/split (get-sys-prop-env "ENVCONF_PATH" default-epath) pathsplit)
        env-post-files (clojure.string/split (get-sys-prop-env "ENVCONF_PATH_FINAL" default-final-epath) pathsplit)]
    (merge (read-env-files env-files)
           (read-system-env)
           (read-system-props)
           (read-env-files env-post-files))))


(defonce ^{:doc "Setup env based on read-envconf function"}
         env (read-envconf))


;;Java interop define an interface with a get(String key) method
;;
(gen-interface
  :name errigal751.envconf.IEnv
  :methods [[get [String] String]
            [get [String String] String]
            [getDouble [String] double]
            [getDouble [String double] double]
            [getLong [String] long]
            [getLong [String long] long]
            [getInt [String] int]
            [getInt [String int] int]
            [getBoolean [String] boolean]
            [getBoolean [String boolean] boolean]
            ])

;; Singleton of Java Accessor to env
;;

(defn getk [env k & [default]] (get env (keywordize k) default))

(defonce ienv (reify errigal751.envconf.IEnv

                (get [this key] (getk env key))
                (get [this key default] (getk env key default))

                (getDouble [this key] (Double/parseDouble (getk env key)))
                (getDouble [this key default] (Double/parseDouble (getk env key default)))

                (getLong [this key] (Long/parseLong (getk env key)))
                (getLong [this key default] (Long/parseLong(getk env key default)))

                (getInt [this key] (Integer/parseInt(getk env key)))
                (getInt [this key default] (Integer/parseInt(getk env key default)))

                (getBoolean [this key] (Boolean/parseBoolean(getk env key)))
                (getBoolean [this key default] (Boolean/parseBoolean (getk env key default)))
                ))


;; Java Class with instance() method to get the default IEnv
;;
;;  import errigal751.envconf.EnvConf;
;;  IEnv env = EnvConf.instance();
;;  env.get("some-property");
;;
(gen-class
  :name errigal751.envconf.EnvConf
  ;:implements [errigal751.envconf.IEnv]
  :prefix "envconf-"
  :methods [^:static [instance [] errigal751.envconf.IEnv]])


(defn envconf-instance [] ienv)













