(ns yamlstar.parser.parser
  (:require [clojure.string :as str]
            [yamlstar.parser.prelude :refer :all]))

;; Forward declarations
(declare auto-detect auto-detect-indent trace-start trace-flush)

;; TRACE flag from environment
(def TRACE (Boolean/parseBoolean (or (env "TRACE") "false")))

;; Default state when stack is empty
(def default-state
  {:name nil
   :doc false
   :lvl 0
   :beg 0
   :end nil
   :m nil
   :t nil})

;; Parser state - uses atoms for mutable state
(defn make-parser [receiver]
  (let [parser {:receiver (atom receiver)
                :input (atom "")
                :pos (atom 0)
                :end (atom 0)
                :state (atom [])
                :trace-num (atom 0)
                :trace-line (atom 0)
                :trace-on (atom true)
                :trace-off (atom 0)
                :trace-info (atom ["" "" "" 0])}]
    ;; Link parser to receiver (stores parser directly in the receiver copy, not as atom)
    (swap! (:receiver parser) assoc :parser parser)
    parser))

;; State management
(defn state-curr [parser]
  (let [state @(:state parser)]
    (if (empty? state)
      default-state
      (peek state))))

(defn state-prev [parser]
  (let [state @(:state parser)]
    (when (>= (count state) 2)
      (nth state (- (count state) 2)))))

(defn state-push [parser name]
  (let [curr (state-curr parser)]
    (swap! (:state parser) conj
           {:name name
            :doc (:doc curr)
            :lvl (inc (:lvl curr))
            :beg @(:pos parser)
            :end nil
            :m (:m curr)
            :t (:t curr)})))

(defn state-pop [parser]
  (let [child (peek @(:state parser))]
    (swap! (:state parser) pop)
    (let [curr-state @(:state parser)]
      (when (seq curr-state)
        (swap! (:state parser)
               (fn [s]
                 (let [curr (peek s)]
                   (conj (pop s)
                         (assoc curr
                                :beg (:beg child)
                                :end @(:pos parser))))))))))

;; Receiver callback routing
(defn make-receivers [parser]
  (let [state @(:state parser)
        names (atom [])
        i (atom (count state))]
    (while (and (> @i 0)
                (let [n (:name (nth state (dec @i)))]
                  (not (str/includes? (str n) "_"))))
      (swap! i dec)
      (let [n (:name (nth state @i))]
        (let [n (if-let [[_ c] (re-matches #"chr\((.)\)" (str n))]
                  (str "x" (hex-char c))
                  (str/replace (str n) #"\(.*" ""))]
          (swap! names #(cons n %)))))
    ;; Decrement i to get actual index (i was count, need count-1)
    (swap! i dec)
    (if (or (neg? @i) (empty? state))
      {:try nil :got nil :not nil}
      (let [n (:name (nth state @i))
            name (str/join "__" (cons n @names))
            receiver @(:receiver parser)]
        {:try (get-in receiver [:callbacks (str "try__" name)])
         :got (get-in receiver [:callbacks (str "got__" name)])
         :not (get-in receiver [:callbacks (str "not__" name)])}))))

(defn receive [parser func type pos]
  (let [receivers (make-receivers parser)
        receiver-fn (get receivers type)]
    (when (env "DEBUG_RECEIVE")
      (let [state @(:state parser)
            name (when (seq state) (:name (peek state)))]
        (println "receive:" type name "=>" (if receiver-fn "FOUND" "nil"))))
    (when (and (env "DEBUG_MATCH") receiver-fn)
      (let [state @(:state parser)
            name (when (seq state) (:name (peek state)))]
        (println "MATCHED:" type name)))
    (when receiver-fn
      (let [curr-pos @(:pos parser)
            input @(:input parser)
            ;; Handle case where pos > curr-pos (like JS slice does)
            text (if (<= pos curr-pos)
                   (subs input pos curr-pos)
                   "")]
        (receiver-fn @(:receiver parser)
                     {:text text
                      :state (state-curr parser)
                      :start pos})))))

;; Forward declarations for grammar functions
(declare call)

;; The central call mechanism
(defn call
  ([parser func] (call parser func "boolean"))
  ([parser func type]
   (let [[func & args] (if (vector? func) func [func])]
     ;; If func is a number or string, return it directly
     (cond
       (number? func) func
       (string? func) func
       :else
       (do
         (when-not (fn? func)
           (FAIL (str "Bad call type '" (typeof* func) "' for '" func "'")))

         (let [trace (or (:trace (meta func))
                         (str func))]
           (state-push parser trace)

           (swap! (:trace-num parser) inc)
           ;; TODO: trace '?' trace args when TRACE

           ;; Set doc flag for l_bare_document
           (when (= (:name (meta func)) "l_bare_document")
             (swap! (:state parser)
                    (fn [s]
                      (let [curr (peek s)]
                        (conj (pop s) (assoc curr :doc true))))))

           ;; Evaluate arguments
           (let [args (mapv (fn [a]
                              (cond
                                (vector? a) (call parser a "any")
                                (fn? a) (call parser a "any")
                                :else a))
                            args)
                 pos @(:pos parser)
                 _ (receive parser func :try pos)

                 ;; Call the function
                 value (loop [v (apply func parser args)]
                         (if (or (fn? v) (vector? v))
                           (recur (call parser v))
                           v))]

             ;; Type checking - nil is treated as false for boolean type
             (when (and (not= type "any")
                        (not= (typeof* value) type)
                        (not (and (= type "boolean") (nil? value))))
               (FAIL (str "Calling '" trace "' returned '" (typeof* value) "' instead of '" type "'")))

             (swap! (:trace-num parser) inc)

             ;; Handle result
             (if (not= type "boolean")
               nil ;; TODO: trace '>' value when TRACE
               (if value
                 (do
                   ;; TODO: trace '+' trace when TRACE
                   (receive parser func :got pos))
                 (do
                   ;; TODO: trace 'x' trace when TRACE
                   (receive parser func :not pos))))

             (state-pop parser)
             value)))))))

;; Special functions - internal versions
(defn start-of-line* [parser]
  (let [pos @(:pos parser)
        input @(:input parser)]
    (or (= pos 0)
        (>= pos @(:end parser))
        (= (nth input (dec pos)) \newline))))

(defn end-of-stream* [parser]
  (>= @(:pos parser) @(:end parser)))

(defn the-end [parser]
  (or (end-of-stream* parser)
      (and (:doc (state-curr parser))
           (start-of-line* parser)
           (let [remaining (subs @(:input parser) @(:pos parser))]
             (re-find #"^(?:---|\.\.\.)(?=\s|$)" remaining)))))

;; Grammar-callable versions (return functions)
(defn start-of-line [parser]
  (with-meta
    (fn [p] (start-of-line* p))
    {:trace "start_of_line"}))

(defn end-of-stream [parser]
  (with-meta
    (fn [p] (end-of-stream* p))
    {:trace "end_of_stream"}))

(defn empty-rule [parser]
  (with-meta
    (fn [p] true)
    {:trace "empty"}))

;; Character matching primitives
(defn chr [parser char]
  (with-meta
    (fn chr-fn [p]
      (when-not (the-end p)
        (when (= (nth @(:input p) @(:pos p)) (first char))
          (swap! (:pos p) inc)
          true)))
    {:trace (str "chr(" (stringify char) ")")}))

(defn rng [parser low high]
  (with-meta
    (fn rng-fn [p]
      (when-not (the-end p)
        (let [input @(:input p)
              pos @(:pos p)
              remaining (subs input pos)
              pattern (re-pattern (str "^[" low "-" high "]"))]
          (when (re-find pattern remaining)
            ;; Handle surrogate pairs
            (let [cp (.codePointAt remaining 0)]
              (when (> cp 65535)
                (swap! (:pos p) inc)))
            (swap! (:pos p) inc)
            true))))
    {:trace (str "rng(" (stringify low) "," (stringify high) ")")}))

;; Combinators
(defn all [parser & funcs]
  (with-meta
    (fn all-fn [p]
      (let [pos @(:pos p)]
        (loop [fs funcs]
          (if (empty? fs)
            true
            (let [f (first fs)]
              (when-not f
                (FAIL "*** Missing function in all group:" funcs))
              (if-not (call p f)
                (do
                  (reset! (:pos p) pos)
                  false)
                (recur (rest fs))))))))
    {:trace "all"}))

(defn any [parser & funcs]
  (with-meta
    (fn any-fn [p]
      (loop [fs funcs]
        (if (empty? fs)
          false
          (if (call p (first fs))
            true
            (recur (rest fs))))))
    {:trace "any"}))

(defn may [parser func]
  (with-meta
    (fn may-fn [p]
      (call p func)
      true)
    {:trace "may"}))

(defn rep [parser min max func]
  (with-meta
    (fn rep-fn [p]
      (if (and max (< max 0))
        false
        (let [pos-start @(:pos p)]
          (loop [count 0
                 pos @(:pos p)]
            (if (and max (>= count max))
              (if (and (>= count min) (or (nil? max) (<= count max)))
                true
                (do
                  (reset! (:pos p) pos-start)
                  false))
              (if-not (call p func)
                (if (and (>= count min) (or (nil? max) (<= count max)))
                  true
                  (do
                    (reset! (:pos p) pos-start)
                    false))
                (if (= @(:pos p) pos)
                  (if (and (>= count min) (or (nil? max) (<= count max)))
                    true
                    (do
                      (reset! (:pos p) pos-start)
                      false))
                  (recur (inc count) @(:pos p)))))))))
    {:trace (str "rep(" min "," max ")")}))

(defn rep2 [parser min max func]
  (with-meta
    (fn rep2-fn [p]
      (if (and max (< max 0))
        false
        (let [pos-start @(:pos p)]
          (loop [count 0
                 pos @(:pos p)]
            (if (and max (>= count max))
              (if (and (>= count min) (or (nil? max) (<= count max)))
                true
                (do
                  (reset! (:pos p) pos-start)
                  false))
              (if-not (call p func)
                (if (and (>= count min) (or (nil? max) (<= count max)))
                  true
                  (do
                    (reset! (:pos p) pos-start)
                    false))
                (if (= @(:pos p) pos)
                  (if (and (>= count min) (or (nil? max) (<= count max)))
                    true
                    (do
                      (reset! (:pos p) pos-start)
                      false))
                  (recur (inc count) @(:pos p)))))))))
    {:trace (str "rep2(" min "," max ")")}))

(defn but [parser & funcs]
  (with-meta
    (fn but-fn [p]
      (when-not (the-end p)
        (let [pos1 @(:pos p)]
          (when (call p (first funcs))
            (let [pos2 @(:pos p)]
              (reset! (:pos p) pos1)
              (loop [fs (rest funcs)]
                (if (empty? fs)
                  (do
                    (reset! (:pos p) pos2)
                    true)
                  (if (call p (first fs))
                    (do
                      (reset! (:pos p) pos1)
                      false)
                    (recur (rest fs))))))))))
    {:trace "but"}))

(defn chk [parser type expr]
  (with-meta
    (fn chk-fn [p]
      (let [pos @(:pos p)]
        (when (= type "<=")
          (swap! (:pos p) dec))
        (let [ok (call p expr)]
          (reset! (:pos p) pos)
          (if (= type "!")
            (not ok)
            ok))))
    {:trace (str "chk(" type "," (stringify expr) ")")}))

(defn case* [parser var map]
  (with-meta
    (fn case-fn [p]
      (let [rule (get map var)]
        (when-not rule
          (FAIL (str "Can't find '" var "' in:") map))
        (call p rule)))
    {:trace (str "case(" var "," (stringify map) ")")}))

(defn flip [parser var map]
  (let [value (get map var)]
    (when-not value
      (FAIL (str "Can't find '" var "' in:") map))
    (if (string? value)
      value
      (call parser value "number"))))

(defn set* [parser var expr]
  (with-meta
    (fn set-fn [p]
      (let [value (call p expr "any")]
        (if (= value -1)
          false
          (let [value (if (= value "auto-detect")
                        (auto-detect p)
                        value)]
            ;; Update state-prev
            (swap! (:state p)
                   (fn [s]
                     (if (< (count s) 2)
                       s
                       (let [state-prev (nth s (- (count s) 2))]
                         (assoc s (- (count s) 2)
                                (assoc state-prev (keyword var) value))))))
            ;; Propagate to parent scopes
            (let [state @(:state p)
                  size (count state)]
              (when (not= (:name (nth state (- size 2))) "all")
                (loop [i 3]
                  (when (< i size)
                    (let [idx (- size i 1)
                          st (nth state idx)]
                      (swap! (:state p)
                             (fn [s]
                               (assoc s idx (assoc st (keyword var) value))))
                      (when-not (= (:name st) "s_l_block_scalar")
                        (recur (inc i))))))))
            true))))
    {:trace (str "set('" var "'," (stringify expr) ")")}))

(defn max* [parser max-val]
  (with-meta
    (fn max-fn [p] true)
    {:trace (str "max(" max-val ")")}))

(defn exclude [parser rule]
  (with-meta
    (fn exclude-fn [p] true)
    {:trace "exclude"}))

(defn add [parser x y]
  (with-meta
    (fn add-fn [p]
      (let [y-val (if (fn? y) (call p y "number") y)]
        (when-not (number? y-val)
          (FAIL (str "y is '" (stringify y-val) "', not number in 'add'")))
        (+ x y-val)))
    {:trace (str "add(" x "," (stringify y) ")")}))

(defn sub [parser x y]
  (with-meta
    (fn sub-fn [p]
      (- x y))
    {:trace (str "sub(" x "," y ")")}))

(defn match [parser]
  (with-meta
    (fn match-fn [p]
      (let [state @(:state p)]
        (loop [i (dec (count state))]
          (when (> i 0)
            (if (:end (nth state i))
              (let [{:keys [beg end]} (nth state i)
                    input @(:input p)]
                ;; Handle case where beg > end (return empty string like JS)
                (if (<= beg end)
                  (subs input beg end)
                  ""))
              (do
                (when (= i 1)
                  (FAIL "Can't find match"))
                (recur (dec i))))))))
    {:trace "match"}))

(defn len [parser str-val]
  (with-meta
    (fn len-fn [p]
      (let [s (if (string? str-val) str-val (call p str-val "string"))]
        (count s)))
    {:trace "len"}))

(defn ord [parser str-val]
  (with-meta
    (fn ord-fn [p]
      (let [s (if (string? str-val) str-val (call p str-val "string"))]
        (- (int (first s)) 48)))
    {:trace "ord"}))

(defn if* [parser test do-if-true]
  (with-meta
    (fn if-fn [p]
      (let [test-val (if (instance? Boolean test) test (call p test "boolean"))]
        (if test-val
          (do
            (call p do-if-true)
            true)
          false)))
    {:trace "if"}))

(defn lt [parser x y]
  (with-meta
    (fn lt-fn [p]
      (let [x-val (if (number? x) x (call p x "number"))
            y-val (if (number? y) y (call p y "number"))]
        (< x-val y-val)))
    {:trace (str "lt(" (stringify x) "," (stringify y) ")")}))

(defn le [parser x y]
  (with-meta
    (fn le-fn [p]
      (let [x-val (if (number? x) x (call p x "number"))
            y-val (if (number? y) y (call p y "number"))]
        (<= x-val y-val)))
    {:trace (str "le(" (stringify x) "," (stringify y) ")")}))

(defn m [parser]
  (with-meta
    (fn m-fn [p]
      (:m (state-curr p)))
    {:trace "m"}))

(defn t [parser]
  (with-meta
    (fn t-fn [p]
      (:t (state-curr p)))
    {:trace "t"}))

;; Auto-detect indent
(defn auto-detect-indent [parser n]
  (let [pos @(:pos parser)
        input @(:input parser)
        in-seq (and (> pos 0)
                    (re-find #"^[-?:]$" (str (nth input (dec pos)))))
        pattern #"^((?:\ *(?:\#.*)?\n)*)(\ *)"
        match-result (re-find pattern (subs input pos))]
    (when-not match-result
      (FAIL "auto_detect_indent"))
    (let [pre (nth match-result 1)
          m-raw (count (nth match-result 2))
          m (if (and in-seq (zero? (count pre)))
              (if (= n -1) (inc m-raw) m-raw)
              (- m-raw n))
          m (if (< m 0) 0 m)]
      m)))

(defn auto-detect
  "Auto-detect indentation. Can take n as parameter or get it from state."
  ([parser] (auto-detect parser (:m (state-curr parser))))
  ([parser n]
   (let [input @(:input parser)
         pos @(:pos parser)
         pattern #"^.*\n((?:\ *\n)*)(\ *)(.?)"
         match-result (re-find pattern (subs input pos))
         pre (or (nth match-result 1) "")
         m (if (and (nth match-result 3)
                    (pos? (count (nth match-result 3))))
             (- (count (or (nth match-result 2) "")) (or n 0))
             (loop [m 0]
               (if (re-find (re-pattern (str " {" m "}")) pre)
                 (recur (inc m))
                 (- m (or n 0) 1))))]
     (when (and (> m 0)
                (re-find (re-pattern (str "(?m)^.{" (+ m (or n 0)) "} ")) pre))
       (die "Spaces found after indent in auto-detect (5LLU)"))
     (if (zero? m) 1 m))))

;; Trace support (stubs for now)
(defn trace-start [parser]
  (or (env "TRACE_START") ""))

(defn trace-flush [parser]
  ;; TODO: implement full trace support
  nil)
