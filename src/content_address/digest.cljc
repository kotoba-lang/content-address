(ns content-address.digest
  "sha2-256 and byte↔octet conversion — the only platform-specific arithmetic.

  `content-address.core` works on octet vectors so both runtimes run the same
  addressing code. This namespace is the narrow place where a JVM byte array
  or a Node Buffer becomes one."
  #?(:cljs (:require ["node:crypto" :as crypto]))
  #?(:clj (:import [java.nio.charset StandardCharsets]
                   [java.security MessageDigest])))

(defn ->octets
  "Platform bytes → vector of unsigned ints. Accepts a string as UTF-8."
  [x]
  #?(:clj (cond
            (nil? x) []
            (string? x) (->octets (.getBytes ^String x StandardCharsets/UTF_8))
            (bytes? x) (mapv #(bit-and % 0xff) x)
            (sequential? x) (mapv #(bit-and % 0xff) x)
            :else (throw (ex-info "not bytes" {:class (class x)})))
     :cljs (cond
             (nil? x) []
             (string? x) (->octets (js/Buffer.from x "utf8"))
             (sequential? x) (mapv #(bit-and % 0xff) x)
             :else (vec (js/Uint8Array.from x)))))

(defn ->bytes
  "Octets → the platform's byte container (JVM byte[] / Node Buffer)."
  [octets]
  #?(:clj (byte-array (map unchecked-byte octets))
     :cljs (js/Buffer.from (clj->js (vec octets)))))

(defn sha256
  "sha2-256 of `x` as a 32-element octet vector."
  [x]
  #?(:clj (->octets (.digest (MessageDigest/getInstance "SHA-256")
                             ^bytes (->bytes (->octets x))))
     :cljs (->octets (-> (crypto/createHash "sha256")
                         (.update (->bytes (->octets x)))
                         (.digest)))))

(defn size
  "Byte length of `x` — what the archive cap is measured against."
  [x]
  (count (->octets x)))
