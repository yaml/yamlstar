;; This grammar was generated from https://yaml.org/spec/1.2/spec.html

(ns yamlstar.parser.grammar
  (:require [yamlstar.parser.parser :as p]
            [yamlstar.parser.prelude :refer [debug-rule]]))

(declare l_yaml_stream)

;; TOP returns the top-level grammar rule as a vector for call to process
(def TOP
  (with-meta
    (fn TOP-fn [parser]
      ;; Return the top rule as a value that call can process
      [l_yaml_stream])
    {:trace "TOP" :name "TOP"}))

;; Helper function for auto-detect
(defn auto_detect [parser n]
  (p/auto-detect parser n))

(defn auto_detect_indent [parser n]
  (p/auto-detect-indent parser n))

(defn empty-parser [parser]
  (p/empty-rule parser))

(declare
  c_printable
  nb_json
  c_byte_order_mark
  c_sequence_entry
  c_mapping_key
  c_mapping_value
  c_collect_entry
  c_sequence_start
  c_sequence_end
  c_mapping_start
  c_mapping_end
  c_comment
  c_anchor
  c_alias
  c_tag
  c_literal
  c_folded
  c_single_quote
  c_double_quote
  c_directive
  c_reserved
  c_indicator
  c_flow_indicator
  b_line_feed
  b_carriage_return
  b_char
  nb_char
  b_break
  b_as_line_feed
  b_non_content
  s_space
  s_tab
  s_white
  ns_char
  ns_dec_digit
  ns_hex_digit
  ns_ascii_letter
  ns_word_char
  ns_uri_char
  ns_tag_char
  c_escape
  ns_esc_null
  ns_esc_bell
  ns_esc_backspace
  ns_esc_horizontal_tab
  ns_esc_line_feed
  ns_esc_vertical_tab
  ns_esc_form_feed
  ns_esc_carriage_return
  ns_esc_escape
  ns_esc_space
  ns_esc_double_quote
  ns_esc_slash
  ns_esc_backslash
  ns_esc_next_line
  ns_esc_non_breaking_space
  ns_esc_line_separator
  ns_esc_paragraph_separator
  ns_esc_8_bit
  ns_esc_16_bit
  ns_esc_32_bit
  c_ns_esc_char
  s_indent
  s_indent_lt
  s_indent_le
  s_separate_in_line
  s_line_prefix
  s_block_line_prefix
  s_flow_line_prefix
  l_empty
  b_l_trimmed
  b_as_space
  b_l_folded
  s_flow_folded
  c_nb_comment_text
  b_comment
  s_b_comment
  l_comment
  s_l_comments
  s_separate
  s_separate_lines
  l_directive
  ns_reserved_directive
  ns_directive_name
  ns_directive_parameter
  ns_yaml_directive
  ns_yaml_version
  ns_tag_directive
  c_tag_handle
  c_primary_tag_handle
  c_secondary_tag_handle
  c_named_tag_handle
  ns_tag_prefix
  c_ns_local_tag_prefix
  ns_global_tag_prefix
  c_ns_properties
  c_ns_tag_property
  c_verbatim_tag
  c_ns_shorthand_tag
  c_non_specific_tag
  c_ns_anchor_property
  ns_anchor_char
  ns_anchor_name
  c_ns_alias_node
  e_scalar
  e_node
  nb_double_char
  ns_double_char
  c_double_quoted
  nb_double_text
  nb_double_one_line
  s_double_escaped
  s_double_break
  nb_ns_double_in_line
  s_double_next_line
  nb_double_multi_line
  c_quoted_quote
  nb_single_char
  ns_single_char
  c_single_quoted
  nb_single_text
  nb_single_one_line
  nb_ns_single_in_line
  s_single_next_line
  nb_single_multi_line
  ns_plain_first
  ns_plain_safe
  ns_plain_safe_out
  ns_plain_safe_in
  ns_plain_char
  ns_plain
  nb_ns_plain_in_line
  ns_plain_one_line
  s_ns_plain_next_line
  ns_plain_multi_line
  in_flow
  c_flow_sequence
  ns_s_flow_seq_entries
  ns_flow_seq_entry
  c_flow_mapping
  ns_s_flow_map_entries
  ns_flow_map_entry
  ns_flow_map_explicit_entry
  ns_flow_map_implicit_entry
  ns_flow_map_yaml_key_entry
  c_ns_flow_map_empty_key_entry
  c_ns_flow_map_separate_value
  c_ns_flow_map_json_key_entry
  c_ns_flow_map_adjacent_value
  ns_flow_pair
  ns_flow_pair_entry
  ns_flow_pair_yaml_key_entry
  c_ns_flow_pair_json_key_entry
  ns_s_implicit_yaml_key
  c_s_implicit_json_key
  ns_flow_yaml_content
  c_flow_json_content
  ns_flow_content
  ns_flow_yaml_node
  c_flow_json_node
  ns_flow_node
  c_b_block_header
  c_indentation_indicator
  c_chomping_indicator
  b_chomped_last
  l_chomped_empty
  l_strip_empty
  l_keep_empty
  l_trail_comments
  c_l_literal
  l_nb_literal_text
  b_nb_literal_next
  l_literal_content
  c_l_folded
  s_nb_folded_text
  l_nb_folded_lines
  s_nb_spaced_text
  b_l_spaced
  l_nb_spaced_lines
  l_nb_same_lines
  l_nb_diff_lines
  l_folded_content
  l_block_sequence
  c_l_block_seq_entry
  s_l_block_indented
  ns_l_compact_sequence
  l_block_mapping
  ns_l_block_map_entry
  c_l_block_map_explicit_entry
  c_l_block_map_explicit_key
  l_block_map_explicit_value
  ns_l_block_map_implicit_entry
  ns_s_block_map_implicit_key
  c_l_block_map_implicit_value
  ns_l_compact_mapping
  s_l_block_node
  s_l_flow_in_block
  s_l_block_in_block
  s_l_block_scalar
  s_l_block_collection
  seq_spaces
  l_document_prefix
  c_directives_end
  c_document_end
  l_document_suffix
  c_forbidden
  l_bare_document
  l_explicit_document
  l_directive_document
  l_any_document
  l_yaml_stream)

;; [001]
;; c-printable ::=
;;   x:9 | x:A | x:D | [x:20-x:7E]
;;   | x:85 | [x:A0-x:D7FF] | [x:E000-x:FFFD]
;;   | [x:10000-x:10FFFF]
(def c_printable
  (with-meta
    (fn c_printable-fn [parser]
      (debug-rule "c_printable")
    (p/any parser
     (p/chr parser
       "\u0009"
    ),
    (p/chr parser
       "\u000A"
    ),
    (p/chr parser
       "\u000D"
    ),
    (p/rng parser
       "\u0020",
      "\u007E"
    ),
    (p/chr parser
       "\u0085"
    ),
    (p/rng parser
       "\u00A0",
      "\uD7FF"
    ),
    (p/rng parser
       "\uE000",
      "\uFFFD"
    ),
    (p/rng parser
       (String. (int-array [0x010000]) 0 1),
      (String. (int-array [0x10FFFF]) 0 1)
    )
  ))
    {:trace "c_printable" :name "c_printable"}))

;; [002]
;; nb-json ::=
;;   x:9 | [x:20-x:10FFFF]
(def nb_json
  (with-meta
    (fn nb_json-fn [parser]
      (debug-rule "nb_json")
    (p/any parser
     (p/chr parser
       "\u0009"
    ),
    (p/rng parser
       "\u0020",
      (String. (int-array [0x10FFFF]) 0 1)
    )
  ))
    {:trace "nb_json" :name "nb_json"}))

;; [003]
;; c-byte-order-mark ::=
;;   x:FEFF
(def c_byte_order_mark
  (with-meta
    (fn c_byte_order_mark-fn [parser]
      (debug-rule "c_byte_order_mark")
    (p/chr parser "\uFEFF"))
    {:trace "c_byte_order_mark" :name "c_byte_order_mark"}))

;; [004]
;; c-sequence-entry ::=
;;   '-'
(def c_sequence_entry
  (with-meta
    (fn c_sequence_entry-fn [parser]
      (debug-rule "c_sequence_entry")
    (p/chr parser "-"))
    {:trace "c_sequence_entry" :name "c_sequence_entry"}))

;; [005]
;; c-mapping-key ::=
;;   '?'
(def c_mapping_key
  (with-meta
    (fn c_mapping_key-fn [parser]
      (debug-rule "c_mapping_key")
    (p/chr parser "?"))
    {:trace "c_mapping_key" :name "c_mapping_key"}))

;; [006]
;; c-mapping-value ::=
;;   ':'
(def c_mapping_value
  (with-meta
    (fn c_mapping_value-fn [parser]
      (debug-rule "c_mapping_value")
    (p/chr parser ":"))
    {:trace "c_mapping_value" :name "c_mapping_value"}))

;; [007]
;; c-collect-entry ::=
;;   ','
(def c_collect_entry
  (with-meta
    (fn c_collect_entry-fn [parser]
      (debug-rule "c_collect_entry")
    (p/chr parser ","))
    {:trace "c_collect_entry" :name "c_collect_entry"}))

;; [008]
;; c-sequence-start ::=
;;   '['
(def c_sequence_start
  (with-meta
    (fn c_sequence_start-fn [parser]
      (debug-rule "c_sequence_start")
    (p/chr parser "["))
    {:trace "c_sequence_start" :name "c_sequence_start"}))

;; [009]
;; c-sequence-end ::=
;;   ']'
(def c_sequence_end
  (with-meta
    (fn c_sequence_end-fn [parser]
      (debug-rule "c_sequence_end")
    (p/chr parser "]"))
    {:trace "c_sequence_end" :name "c_sequence_end"}))

;; [010]
;; c-mapping-start ::=
;;   '{'
(def c_mapping_start
  (with-meta
    (fn c_mapping_start-fn [parser]
      (debug-rule "c_mapping_start")
    (p/chr parser "\u007B"))
    {:trace "c_mapping_start" :name "c_mapping_start"}))

;; [011]
;; c-mapping-end ::=
;;   '}'
(def c_mapping_end
  (with-meta
    (fn c_mapping_end-fn [parser]
      (debug-rule "c_mapping_end")
    (p/chr parser "\u007D"))
    {:trace "c_mapping_end" :name "c_mapping_end"}))

;; [012]
;; c-comment ::=
;;   '#'
(def c_comment
  (with-meta
    (fn c_comment-fn [parser]
      (debug-rule "c_comment")
    (p/chr parser "\u0023"))
    {:trace "c_comment" :name "c_comment"}))

;; [013]
;; c-anchor ::=
;;   '&'
(def c_anchor
  (with-meta
    (fn c_anchor-fn [parser]
      (debug-rule "c_anchor")
    (p/chr parser "&"))
    {:trace "c_anchor" :name "c_anchor"}))

;; [014]
;; c-alias ::=
;;   '*'
(def c_alias
  (with-meta
    (fn c_alias-fn [parser]
      (debug-rule "c_alias")
    (p/chr parser "*"))
    {:trace "c_alias" :name "c_alias"}))

;; [015]
;; c-tag ::=
;;   '!'
(def c_tag
  (with-meta
    (fn c_tag-fn [parser]
      (debug-rule "c_tag")
    (p/chr parser "!"))
    {:trace "c_tag" :name "c_tag"}))

;; [016]
;; c-literal ::=
;;   '|'
(def c_literal
  (with-meta
    (fn c_literal-fn [parser]
      (debug-rule "c_literal")
    (p/chr parser "|"))
    {:trace "c_literal" :name "c_literal"}))

;; [017]
;; c-folded ::=
;;   '>'
(def c_folded
  (with-meta
    (fn c_folded-fn [parser]
      (debug-rule "c_folded")
    (p/chr parser ">"))
    {:trace "c_folded" :name "c_folded"}))

;; [018]
;; c-single-quote ::=
;;   '''
(def c_single_quote
  (with-meta
    (fn c_single_quote-fn [parser]
      (debug-rule "c_single_quote")
    (p/chr parser "'"))
    {:trace "c_single_quote" :name "c_single_quote"}))

;; [019]
;; c-double-quote ::=
;;   '"'
(def c_double_quote
  (with-meta
    (fn c_double_quote-fn [parser]
      (debug-rule "c_double_quote")
    (p/chr parser "\""))
    {:trace "c_double_quote" :name "c_double_quote"}))

;; [020]
;; c-directive ::=
;;   '%'
(def c_directive
  (with-meta
    (fn c_directive-fn [parser]
      (debug-rule "c_directive")
    (p/chr parser "%"))
    {:trace "c_directive" :name "c_directive"}))

;; [021]
;; c-reserved ::=
;;   '@' | '`'
(def c_reserved
  (with-meta
    (fn c_reserved-fn [parser]
      (debug-rule "c_reserved")
    (p/any parser
     (p/chr parser
       "\u0040"
    ),
    (p/chr parser
       "\u0060"
    )
  ))
    {:trace "c_reserved" :name "c_reserved"}))

;; [022]
;; c-indicator ::=
;;   '-' | '?' | ':' | ',' | '[' | ']' | '{' | '}'
;;   | '#' | '&' | '*' | '!' | '|' | '>' | ''' | '"'
;;   | '%' | '@' | '`'
(def c_indicator
  (with-meta
    (fn c_indicator-fn [parser]
      (debug-rule "c_indicator")
    (p/any parser
     (p/chr parser
       "-"
    ),
    (p/chr parser
       "?"
    ),
    (p/chr parser
       ":"
    ),
    (p/chr parser
       ","
    ),
    (p/chr parser
       "["
    ),
    (p/chr parser
       "]"
    ),
    (p/chr parser
       "\u007B"
    ),
    (p/chr parser
       "\u007D"
    ),
    (p/chr parser
       "\u0023"
    ),
    (p/chr parser
       "&"
    ),
    (p/chr parser
       "*"
    ),
    (p/chr parser
       "!"
    ),
    (p/chr parser
       "|"
    ),
    (p/chr parser
       ">"
    ),
    (p/chr parser
       "'"
    ),
    (p/chr parser
       "\""
    ),
    (p/chr parser
       "%"
    ),
    (p/chr parser
       "\u0040"
    ),
    (p/chr parser
       "\u0060"
    )
  ))
    {:trace "c_indicator" :name "c_indicator"}))

;; [023]
;; c-flow-indicator ::=
;;   ',' | '[' | ']' | '{' | '}'
(def c_flow_indicator
  (with-meta
    (fn c_flow_indicator-fn [parser]
      (debug-rule "c_flow_indicator")
    (p/any parser
     (p/chr parser
       ","
    ),
    (p/chr parser
       "["
    ),
    (p/chr parser
       "]"
    ),
    (p/chr parser
       "\u007B"
    ),
    (p/chr parser
       "\u007D"
    )
  ))
    {:trace "c_flow_indicator" :name "c_flow_indicator"}))

;; [024]
;; b-line-feed ::=
;;   x:A
(def b_line_feed
  (with-meta
    (fn b_line_feed-fn [parser]
      (debug-rule "b_line_feed")
    (p/chr parser "\u000A"))
    {:trace "b_line_feed" :name "b_line_feed"}))

;; [025]
;; b-carriage-return ::=
;;   x:D
(def b_carriage_return
  (with-meta
    (fn b_carriage_return-fn [parser]
      (debug-rule "b_carriage_return")
    (p/chr parser "\u000D"))
    {:trace "b_carriage_return" :name "b_carriage_return"}))

;; [026]
;; b-char ::=
;;   b-line-feed | b-carriage-return
(def b_char
  (with-meta
    (fn b_char-fn [parser]
      (debug-rule "b_char")
    (p/any parser
     b_line_feed,
    b_carriage_return
  ))
    {:trace "b_char" :name "b_char"}))

;; [027]
;; nb-char ::=
;;   c-printable - b-char - c-byte-order-mark
(def nb_char
  (with-meta
    (fn nb_char-fn [parser]
      (debug-rule "nb_char")
    (p/but parser
     c_printable,
    b_char,
    c_byte_order_mark
  ))
    {:trace "nb_char" :name "nb_char"}))

;; [028]
;; b-break ::=
;;   ( b-carriage-return b-line-feed )
;;   | b-carriage-return
;;   | b-line-feed
(def b_break
  (with-meta
    (fn b_break-fn [parser]
      (debug-rule "b_break")
    (p/any parser
     (p/all parser
       b_carriage_return,
      b_line_feed
    ),
    b_carriage_return,
    b_line_feed
  ))
    {:trace "b_break" :name "b_break"}))

;; [029]
;; b-as-line-feed ::=
;;   b-break
(def b_as_line_feed
  (with-meta
    (fn b_as_line_feed-fn [parser]
      (debug-rule "b_as_line_feed")
    b_break)
    {:trace "b_as_line_feed" :name "b_as_line_feed"}))

;; [030]
;; b-non-content ::=
;;   b-break
(def b_non_content
  (with-meta
    (fn b_non_content-fn [parser]
      (debug-rule "b_non_content")
    b_break)
    {:trace "b_non_content" :name "b_non_content"}))

;; [031]
;; s-space ::=
;;   x:20
(def s_space
  (with-meta
    (fn s_space-fn [parser]
      (debug-rule "s_space")
    (p/chr parser "\u0020"))
    {:trace "s_space" :name "s_space"}))

;; [032]
;; s-tab ::=
;;   x:9
(def s_tab
  (with-meta
    (fn s_tab-fn [parser]
      (debug-rule "s_tab")
    (p/chr parser "\u0009"))
    {:trace "s_tab" :name "s_tab"}))

;; [033]
;; s-white ::=
;;   s-space | s-tab
(def s_white
  (with-meta
    (fn s_white-fn [parser]
      (debug-rule "s_white")
    (p/any parser
     s_space,
    s_tab
  ))
    {:trace "s_white" :name "s_white"}))

;; [034]
;; ns-char ::=
;;   nb-char - s-white
(def ns_char
  (with-meta
    (fn ns_char-fn [parser]
      (debug-rule "ns_char")
    (p/but parser
     nb_char,
    s_white
  ))
    {:trace "ns_char" :name "ns_char"}))

;; [035]
;; ns-dec-digit ::=
;;   [x:30-x:39]
(def ns_dec_digit
  (with-meta
    (fn ns_dec_digit-fn [parser]
      (debug-rule "ns_dec_digit")
    (p/rng parser "\u0030", "\u0039"))
    {:trace "ns_dec_digit" :name "ns_dec_digit"}))

;; [036]
;; ns-hex-digit ::=
;;   ns-dec-digit
;;   | [x:41-x:46] | [x:61-x:66]
(def ns_hex_digit
  (with-meta
    (fn ns_hex_digit-fn [parser]
      (debug-rule "ns_hex_digit")
    (p/any parser
     ns_dec_digit,
    (p/rng parser
       "\u0041",
      "\u0046"
    ),
    (p/rng parser
       "\u0061",
      "\u0066"
    )
  ))
    {:trace "ns_hex_digit" :name "ns_hex_digit"}))

;; [037]
;; ns-ascii-letter ::=
;;   [x:41-x:5A] | [x:61-x:7A]
(def ns_ascii_letter
  (with-meta
    (fn ns_ascii_letter-fn [parser]
      (debug-rule "ns_ascii_letter")
    (p/any parser
     (p/rng parser
       "\u0041",
      "\u005A"
    ),
    (p/rng parser
       "\u0061",
      "\u007A"
    )
  ))
    {:trace "ns_ascii_letter" :name "ns_ascii_letter"}))

;; [038]
;; ns-word-char ::=
;;   ns-dec-digit | ns-ascii-letter | '-'
(def ns_word_char
  (with-meta
    (fn ns_word_char-fn [parser]
      (debug-rule "ns_word_char")
    (p/any parser
     ns_dec_digit,
    ns_ascii_letter,
    (p/chr parser
       "-"
    )
  ))
    {:trace "ns_word_char" :name "ns_word_char"}))

;; [039]
;; ns-uri-char ::=
;;   '%' ns-hex-digit ns-hex-digit | ns-word-char | '#'
;;   | ';' | '/' | '?' | ':' | '@' | '&' | '=' | '+' | '$' | ','
;;   | '_' | '.' | '!' | '~' | '*' | ''' | '(' | ')' | '[' | ']'
(def ns_uri_char
  (with-meta
    (fn ns_uri_char-fn [parser]
      (debug-rule "ns_uri_char")
    (p/any parser
     (p/all parser
       (p/chr parser
         "%"
      ),
      ns_hex_digit,
      ns_hex_digit
    ),
    ns_word_char,
    (p/chr parser
       "\u0023"
    ),
    (p/chr parser
       ";"
    ),
    (p/chr parser
       "/"
    ),
    (p/chr parser
       "?"
    ),
    (p/chr parser
       ":"
    ),
    (p/chr parser
       "\u0040"
    ),
    (p/chr parser
       "&"
    ),
    (p/chr parser
       "="
    ),
    (p/chr parser
       "+"
    ),
    (p/chr parser
       "$"
    ),
    (p/chr parser
       ","
    ),
    (p/chr parser
       "_"
    ),
    (p/chr parser
       "."
    ),
    (p/chr parser
       "!"
    ),
    (p/chr parser
       "~"
    ),
    (p/chr parser
       "*"
    ),
    (p/chr parser
       "'"
    ),
    (p/chr parser
       "("
    ),
    (p/chr parser
       ")"
    ),
    (p/chr parser
       "["
    ),
    (p/chr parser
       "]"
    )
  ))
    {:trace "ns_uri_char" :name "ns_uri_char"}))

;; [040]
;; ns-tag-char ::=
;;   ns-uri-char - '!' - c-flow-indicator
(def ns_tag_char
  (with-meta
    (fn ns_tag_char-fn [parser]
      (debug-rule "ns_tag_char")
    (p/but parser
     ns_uri_char,
    (p/chr parser
       "!"
    ),
    c_flow_indicator
  ))
    {:trace "ns_tag_char" :name "ns_tag_char"}))

;; [041]
;; c-escape ::=
;;   '\\'
(def c_escape
  (with-meta
    (fn c_escape-fn [parser]
      (debug-rule "c_escape")
    (p/chr parser "\\"))
    {:trace "c_escape" :name "c_escape"}))

;; [042]
;; ns-esc-null ::=
;;   '0'
(def ns_esc_null
  (with-meta
    (fn ns_esc_null-fn [parser]
      (debug-rule "ns_esc_null")
    (p/chr parser "0"))
    {:trace "ns_esc_null" :name "ns_esc_null"}))

;; [043]
;; ns-esc-bell ::=
;;   'a'
(def ns_esc_bell
  (with-meta
    (fn ns_esc_bell-fn [parser]
      (debug-rule "ns_esc_bell")
    (p/chr parser "a"))
    {:trace "ns_esc_bell" :name "ns_esc_bell"}))

;; [044]
;; ns-esc-backspace ::=
;;   'b'
(def ns_esc_backspace
  (with-meta
    (fn ns_esc_backspace-fn [parser]
      (debug-rule "ns_esc_backspace")
    (p/chr parser "b"))
    {:trace "ns_esc_backspace" :name "ns_esc_backspace"}))

;; [045]
;; ns-esc-horizontal-tab ::=
;;   't' | x:9
(def ns_esc_horizontal_tab
  (with-meta
    (fn ns_esc_horizontal_tab-fn [parser]
      (debug-rule "ns_esc_horizontal_tab")
    (p/any parser
     (p/chr parser
       "t"
    ),
    (p/chr parser
       "\u0009"
    )
  ))
    {:trace "ns_esc_horizontal_tab" :name "ns_esc_horizontal_tab"}))

;; [046]
;; ns-esc-line-feed ::=
;;   'n'
(def ns_esc_line_feed
  (with-meta
    (fn ns_esc_line_feed-fn [parser]
      (debug-rule "ns_esc_line_feed")
    (p/chr parser "n"))
    {:trace "ns_esc_line_feed" :name "ns_esc_line_feed"}))

;; [047]
;; ns-esc-vertical-tab ::=
;;   'v'
(def ns_esc_vertical_tab
  (with-meta
    (fn ns_esc_vertical_tab-fn [parser]
      (debug-rule "ns_esc_vertical_tab")
    (p/chr parser "v"))
    {:trace "ns_esc_vertical_tab" :name "ns_esc_vertical_tab"}))

;; [048]
;; ns-esc-form-feed ::=
;;   'f'
(def ns_esc_form_feed
  (with-meta
    (fn ns_esc_form_feed-fn [parser]
      (debug-rule "ns_esc_form_feed")
    (p/chr parser "f"))
    {:trace "ns_esc_form_feed" :name "ns_esc_form_feed"}))

;; [049]
;; ns-esc-carriage-return ::=
;;   'r'
(def ns_esc_carriage_return
  (with-meta
    (fn ns_esc_carriage_return-fn [parser]
      (debug-rule "ns_esc_carriage_return")
    (p/chr parser "r"))
    {:trace "ns_esc_carriage_return" :name "ns_esc_carriage_return"}))

;; [050]
;; ns-esc-escape ::=
;;   'e'
(def ns_esc_escape
  (with-meta
    (fn ns_esc_escape-fn [parser]
      (debug-rule "ns_esc_escape")
    (p/chr parser "e"))
    {:trace "ns_esc_escape" :name "ns_esc_escape"}))

;; [051]
;; ns-esc-space ::=
;;   x:20
(def ns_esc_space
  (with-meta
    (fn ns_esc_space-fn [parser]
      (debug-rule "ns_esc_space")
    (p/chr parser "\u0020"))
    {:trace "ns_esc_space" :name "ns_esc_space"}))

;; [052]
;; ns-esc-double-quote ::=
;;   '"'
(def ns_esc_double_quote
  (with-meta
    (fn ns_esc_double_quote-fn [parser]
      (debug-rule "ns_esc_double_quote")
    (p/chr parser "\""))
    {:trace "ns_esc_double_quote" :name "ns_esc_double_quote"}))

;; [053]
;; ns-esc-slash ::=
;;   '/'
(def ns_esc_slash
  (with-meta
    (fn ns_esc_slash-fn [parser]
      (debug-rule "ns_esc_slash")
    (p/chr parser "/"))
    {:trace "ns_esc_slash" :name "ns_esc_slash"}))

;; [054]
;; ns-esc-backslash ::=
;;   '\\'
(def ns_esc_backslash
  (with-meta
    (fn ns_esc_backslash-fn [parser]
      (debug-rule "ns_esc_backslash")
    (p/chr parser "\\"))
    {:trace "ns_esc_backslash" :name "ns_esc_backslash"}))

;; [055]
;; ns-esc-next-line ::=
;;   'N'
(def ns_esc_next_line
  (with-meta
    (fn ns_esc_next_line-fn [parser]
      (debug-rule "ns_esc_next_line")
    (p/chr parser "N"))
    {:trace "ns_esc_next_line" :name "ns_esc_next_line"}))

;; [056]
;; ns-esc-non-breaking-space ::=
;;   '_'
(def ns_esc_non_breaking_space
  (with-meta
    (fn ns_esc_non_breaking_space-fn [parser]
      (debug-rule "ns_esc_non_breaking_space")
    (p/chr parser "_"))
    {:trace "ns_esc_non_breaking_space" :name "ns_esc_non_breaking_space"}))

;; [057]
;; ns-esc-line-separator ::=
;;   'L'
(def ns_esc_line_separator
  (with-meta
    (fn ns_esc_line_separator-fn [parser]
      (debug-rule "ns_esc_line_separator")
    (p/chr parser "L"))
    {:trace "ns_esc_line_separator" :name "ns_esc_line_separator"}))

;; [058]
;; ns-esc-paragraph-separator ::=
;;   'P'
(def ns_esc_paragraph_separator
  (with-meta
    (fn ns_esc_paragraph_separator-fn [parser]
      (debug-rule "ns_esc_paragraph_separator")
    (p/chr parser "P"))
    {:trace "ns_esc_paragraph_separator" :name "ns_esc_paragraph_separator"}))

;; [059]
;; ns-esc-8-bit ::=
;;   'x'
;;   ( ns-hex-digit{2} )
(def ns_esc_8_bit
  (with-meta
    (fn ns_esc_8_bit-fn [parser]
      (debug-rule "ns_esc_8_bit")
    (p/all parser
     (p/chr parser
       "x"
    ),
    (p/rep parser
       2,
      2,
      ns_hex_digit
    )
  ))
    {:trace "ns_esc_8_bit" :name "ns_esc_8_bit"}))

;; [060]
;; ns-esc-16-bit ::=
;;   'u'
;;   ( ns-hex-digit{4} )
(def ns_esc_16_bit
  (with-meta
    (fn ns_esc_16_bit-fn [parser]
      (debug-rule "ns_esc_16_bit")
    (p/all parser
     (p/chr parser
       "u"
    ),
    (p/rep parser
       4,
      4,
      ns_hex_digit
    )
  ))
    {:trace "ns_esc_16_bit" :name "ns_esc_16_bit"}))

;; [061]
;; ns-esc-32-bit ::=
;;   'U'
;;   ( ns-hex-digit{8} )
(def ns_esc_32_bit
  (with-meta
    (fn ns_esc_32_bit-fn [parser]
      (debug-rule "ns_esc_32_bit")
    (p/all parser
     (p/chr parser
       "U"
    ),
    (p/rep parser
       8,
      8,
      ns_hex_digit
    )
  ))
    {:trace "ns_esc_32_bit" :name "ns_esc_32_bit"}))

;; [062]
;; c-ns-esc-char ::=
;;   '\\'
;;   ( ns-esc-null | ns-esc-bell | ns-esc-backspace
;;   | ns-esc-horizontal-tab | ns-esc-line-feed
;;   | ns-esc-vertical-tab | ns-esc-form-feed
;;   | ns-esc-carriage-return | ns-esc-escape | ns-esc-space
;;   | ns-esc-double-quote | ns-esc-slash | ns-esc-backslash
;;   | ns-esc-next-line | ns-esc-non-breaking-space
;;   | ns-esc-line-separator | ns-esc-paragraph-separator
;;   | ns-esc-8-bit | ns-esc-16-bit | ns-esc-32-bit )
(def c_ns_esc_char
  (with-meta
    (fn c_ns_esc_char-fn [parser]
      (debug-rule "c_ns_esc_char")
    (p/all parser
     (p/chr parser
       "\\"
    ),
    (p/any parser
       ns_esc_null,
      ns_esc_bell,
      ns_esc_backspace,
      ns_esc_horizontal_tab,
      ns_esc_line_feed,
      ns_esc_vertical_tab,
      ns_esc_form_feed,
      ns_esc_carriage_return,
      ns_esc_escape,
      ns_esc_space,
      ns_esc_double_quote,
      ns_esc_slash,
      ns_esc_backslash,
      ns_esc_next_line,
      ns_esc_non_breaking_space,
      ns_esc_line_separator,
      ns_esc_paragraph_separator,
      ns_esc_8_bit,
      ns_esc_16_bit,
      ns_esc_32_bit
    )
  ))
    {:trace "c_ns_esc_char" :name "c_ns_esc_char"}))

;; [063]
;; s-indent(n) ::=
;;   s-space{n}
(def s_indent
  (with-meta
    (fn s_indent-fn [parser n]
      (debug-rule "s_indent", n)
    (p/rep parser n, n, s_space))
    {:trace "s_indent" :name "s_indent"}))

;; [064]
;; s-indent(<n) ::=
;;   s-space{m} <where_m_<_n>
(def s_indent_lt
  (with-meta
    (fn s_indent_lt-fn [parser n]
      (debug-rule "s_indent_lt", n)
    (p/may parser
     (p/all parser
       (p/rep parser
         0,
        nil,
        s_space
      ),
      (p/lt parser
         (p/len parser
           (p/match parser)
        ),
        n
      )
    )
  ))
    {:trace "s_indent_lt" :name "s_indent_lt"}))

;; [065]
;; s-indent(<=n) ::=
;;   s-space{m} <where_m_<=_n>
(def s_indent_le
  (with-meta
    (fn s_indent_le-fn [parser n]
      (debug-rule "s_indent_le", n)
    (p/may parser
     (p/all parser
       (p/rep parser
         0,
        nil,
        s_space
      ),
      (p/le parser
         (p/len parser
           (p/match parser)
        ),
        n
      )
    )
  ))
    {:trace "s_indent_le" :name "s_indent_le"}))

;; [066]
;; s-separate-in-line ::=
;;   s-white+ | <start_of_line>
(def s_separate_in_line
  (with-meta
    (fn s_separate_in_line-fn [parser]
      (debug-rule "s_separate_in_line")
    (p/any parser
     (p/rep parser
       1,
      nil,
      s_white
    ),
    (p/start-of-line parser)
  ))
    {:trace "s_separate_in_line" :name "s_separate_in_line"}))

;; [067]
;; s-line-prefix(n,c) ::=
;;   ( c = block-out => s-block-line-prefix(n) )
;;   ( c = block-in => s-block-line-prefix(n) )
;;   ( c = flow-out => s-flow-line-prefix(n) )
;;   ( c = flow-in => s-flow-line-prefix(n) )
(def s_line_prefix
  (with-meta
    (fn s_line_prefix-fn [parser n c]
      (debug-rule "s_line_prefix", n, c)
    (p/case* parser
     c,
    {
      "block-in" [s_block_line_prefix, n],
      "block-out" [s_block_line_prefix, n],
      "flow-in" [s_flow_line_prefix, n],
      "flow-out" [s_flow_line_prefix, n],
    }
  ))
    {:trace "s_line_prefix" :name "s_line_prefix"}))

;; [068]
;; s-block-line-prefix(n) ::=
;;   s-indent(n)
(def s_block_line_prefix
  (with-meta
    (fn s_block_line_prefix-fn [parser n]
      (debug-rule "s_block_line_prefix", n)
    [s_indent, n])
    {:trace "s_block_line_prefix" :name "s_block_line_prefix"}))

;; [069]
;; s-flow-line-prefix(n) ::=
;;   s-indent(n)
;;   s-separate-in-line?
(def s_flow_line_prefix
  (with-meta
    (fn s_flow_line_prefix-fn [parser n]
      (debug-rule "s_flow_line_prefix", n)
    (p/all parser
     [s_indent, n],
    (p/rep parser
       0,
      1,
      s_separate_in_line
    )
  ))
    {:trace "s_flow_line_prefix" :name "s_flow_line_prefix"}))

;; [070]
;; l-empty(n,c) ::=
;;   ( s-line-prefix(n,c) | s-indent(<n) )
;;   b-as-line-feed
(def l_empty
  (with-meta
    (fn l_empty-fn [parser n c]
      (debug-rule "l_empty", n, c)
    (p/all parser
     (p/any parser
       [s_line_prefix, n, c],
      [s_indent_lt, n]
    ),
    b_as_line_feed
  ))
    {:trace "l_empty" :name "l_empty"}))

;; [071]
;; b-l-trimmed(n,c) ::=
;;   b-non-content l-empty(n,c)+
(def b_l_trimmed
  (with-meta
    (fn b_l_trimmed-fn [parser n c]
      (debug-rule "b_l_trimmed", n, c)
    (p/all parser
     b_non_content,
    (p/rep parser
       1,
      nil,
      [l_empty, n, c]
    )
  ))
    {:trace "b_l_trimmed" :name "b_l_trimmed"}))

;; [072]
;; b-as-space ::=
;;   b-break
(def b_as_space
  (with-meta
    (fn b_as_space-fn [parser]
      (debug-rule "b_as_space")
    b_break)
    {:trace "b_as_space" :name "b_as_space"}))

;; [073]
;; b-l-folded(n,c) ::=
;;   b-l-trimmed(n,c) | b-as-space
(def b_l_folded
  (with-meta
    (fn b_l_folded-fn [parser n c]
      (debug-rule "b_l_folded", n, c)
    (p/any parser
     [b_l_trimmed, n, c],
    b_as_space
  ))
    {:trace "b_l_folded" :name "b_l_folded"}))

;; [074]
;; s-flow-folded(n) ::=
;;   s-separate-in-line?
;;   b-l-folded(n,flow-in)
;;   s-flow-line-prefix(n)
(def s_flow_folded
  (with-meta
    (fn s_flow_folded-fn [parser n]
      (debug-rule "s_flow_folded", n)
    (p/all parser
     (p/rep parser
       0,
      1,
      s_separate_in_line
    ),
    [b_l_folded, n, "flow-in"],
    [s_flow_line_prefix, n]
  ))
    {:trace "s_flow_folded" :name "s_flow_folded"}))

;; [075]
;; c-nb-comment-text ::=
;;   '#' nb-char*
(def c_nb_comment_text
  (with-meta
    (fn c_nb_comment_text-fn [parser]
      (debug-rule "c_nb_comment_text")
    (p/all parser
     (p/chr parser
       "\u0023"
    ),
    (p/rep parser
       0,
      nil,
      nb_char
    )
  ))
    {:trace "c_nb_comment_text" :name "c_nb_comment_text"}))

;; [076]
;; b-comment ::=
;;   b-non-content | <end_of_file>
(def b_comment
  (with-meta
    (fn b_comment-fn [parser]
      (debug-rule "b_comment")
    (p/any parser
     b_non_content,
    (p/end-of-stream parser)
  ))
    {:trace "b_comment" :name "b_comment"}))

;; [077]
;; s-b-comment ::=
;;   ( s-separate-in-line
;;   c-nb-comment-text? )?
;;   b-comment
(def s_b_comment
  (with-meta
    (fn s_b_comment-fn [parser]
      (debug-rule "s_b_comment")
    (p/all parser
     (p/rep parser
       0,
      1,
      (p/all parser
         s_separate_in_line,
        (p/rep parser
           0,
          1,
          c_nb_comment_text
        )
      )),
    b_comment
  ))
    {:trace "s_b_comment" :name "s_b_comment"}))

;; [078]
;; l-comment ::=
;;   s-separate-in-line c-nb-comment-text?
;;   b-comment
(def l_comment
  (with-meta
    (fn l_comment-fn [parser]
      (debug-rule "l_comment")
    (p/all parser
     s_separate_in_line,
    (p/rep parser
       0,
      1,
      c_nb_comment_text
    ),
    b_comment
  ))
    {:trace "l_comment" :name "l_comment"}))

;; [079]
;; s-l-comments ::=
;;   ( s-b-comment | <start_of_line> )
;;   l-comment*
(def s_l_comments
  (with-meta
    (fn s_l_comments-fn [parser]
      (debug-rule "s_l_comments")
    (p/all parser
     (p/any parser
       s_b_comment,
      (p/start-of-line parser)
    ),
    (p/rep parser
       0,
      nil,
      l_comment
    )
  ))
    {:trace "s_l_comments" :name "s_l_comments"}))

;; [080]
;; s-separate(n,c) ::=
;;   ( c = block-out => s-separate-lines(n) )
;;   ( c = block-in => s-separate-lines(n) )
;;   ( c = flow-out => s-separate-lines(n) )
;;   ( c = flow-in => s-separate-lines(n) )
;;   ( c = block-key => s-separate-in-line )
;;   ( c = flow-key => s-separate-in-line )
(def s_separate
  (with-meta
    (fn s_separate-fn [parser n c]
      (debug-rule "s_separate", n, c)
    (p/case* parser
     c,
    {
      "block-in" [s_separate_lines, n],
      "block-key" s_separate_in_line,
      "block-out" [s_separate_lines, n],
      "flow-in" [s_separate_lines, n],
      "flow-key" s_separate_in_line,
      "flow-out" [s_separate_lines, n],
    }
  ))
    {:trace "s_separate" :name "s_separate"}))

;; [081]
;; s-separate-lines(n) ::=
;;   ( s-l-comments
;;   s-flow-line-prefix(n) )
;;   | s-separate-in-line
(def s_separate_lines
  (with-meta
    (fn s_separate_lines-fn [parser n]
      (debug-rule "s_separate_lines", n)
    (p/any parser
     (p/all parser
       s_l_comments,
      [s_flow_line_prefix, n]
    ),
    s_separate_in_line
  ))
    {:trace "s_separate_lines" :name "s_separate_lines"}))

;; [082]
;; l-directive ::=
;;   '%'
;;   ( ns-yaml-directive
;;   | ns-tag-directive
;;   | ns-reserved-directive )
;;   s-l-comments
(def l_directive
  (with-meta
    (fn l_directive-fn [parser]
      (debug-rule "l_directive")
    (p/all parser
     (p/chr parser
       "%"
    ),
    (p/any parser
       ns_yaml_directive,
      ns_tag_directive,
      ns_reserved_directive
    ),
    s_l_comments
  ))
    {:trace "l_directive" :name "l_directive"}))

;; [083]
;; ns-reserved-directive ::=
;;   ns-directive-name
;;   ( s-separate-in-line ns-directive-parameter )*
(def ns_reserved_directive
  (with-meta
    (fn ns_reserved_directive-fn [parser]
      (debug-rule "ns_reserved_directive")
    (p/all parser
     ns_directive_name,
    (p/rep parser
       0,
      nil,
      (p/all parser
         s_separate_in_line,
        ns_directive_parameter
      ))
  ))
    {:trace "ns_reserved_directive" :name "ns_reserved_directive"}))

;; [084]
;; ns-directive-name ::=
;;   ns-char+
(def ns_directive_name
  (with-meta
    (fn ns_directive_name-fn [parser]
      (debug-rule "ns_directive_name")
    (p/rep2 parser 1, nil, ns_char))
    {:trace "ns_directive_name" :name "ns_directive_name"}))

;; [085]
;; ns-directive-parameter ::=
;;   ns-char+
(def ns_directive_parameter
  (with-meta
    (fn ns_directive_parameter-fn [parser]
      (debug-rule "ns_directive_parameter")
    (p/rep2 parser 1, nil, ns_char))
    {:trace "ns_directive_parameter" :name "ns_directive_parameter"}))

;; [086]
;; ns-yaml-directive ::=
;;   'Y' 'A' 'M' 'L'
;;   s-separate-in-line ns-yaml-version
(def ns_yaml_directive
  (with-meta
    (fn ns_yaml_directive-fn [parser]
      (debug-rule "ns_yaml_directive")
    (p/all parser
     (p/chr parser
       "Y"
    ),
    (p/chr parser
       "A"
    ),
    (p/chr parser
       "M"
    ),
    (p/chr parser
       "L"
    ),
    s_separate_in_line,
    ns_yaml_version
  ))
    {:trace "ns_yaml_directive" :name "ns_yaml_directive"}))

;; [087]
;; ns-yaml-version ::=
;;   ns-dec-digit+ '.' ns-dec-digit+
(def ns_yaml_version
  (with-meta
    (fn ns_yaml_version-fn [parser]
      (debug-rule "ns_yaml_version")
    (p/all parser
     (p/rep parser
       1,
      nil,
      ns_dec_digit
    ),
    (p/chr parser
       "."
    ),
    (p/rep2 parser
       1,
      nil,
      ns_dec_digit
    )
  ))
    {:trace "ns_yaml_version" :name "ns_yaml_version"}))

;; [088]
;; ns-tag-directive ::=
;;   'T' 'A' 'G'
;;   s-separate-in-line c-tag-handle
;;   s-separate-in-line ns-tag-prefix
(def ns_tag_directive
  (with-meta
    (fn ns_tag_directive-fn [parser]
      (debug-rule "ns_tag_directive")
    (p/all parser
     (p/chr parser
       "T"
    ),
    (p/chr parser
       "A"
    ),
    (p/chr parser
       "G"
    ),
    s_separate_in_line,
    c_tag_handle,
    s_separate_in_line,
    ns_tag_prefix
  ))
    {:trace "ns_tag_directive" :name "ns_tag_directive"}))

;; [089]
;; c-tag-handle ::=
;;   c-named-tag-handle
;;   | c-secondary-tag-handle
;;   | c-primary-tag-handle
(def c_tag_handle
  (with-meta
    (fn c_tag_handle-fn [parser]
      (debug-rule "c_tag_handle")
    (p/any parser
     c_named_tag_handle,
    c_secondary_tag_handle,
    c_primary_tag_handle
  ))
    {:trace "c_tag_handle" :name "c_tag_handle"}))

;; [090]
;; c-primary-tag-handle ::=
;;   '!'
(def c_primary_tag_handle
  (with-meta
    (fn c_primary_tag_handle-fn [parser]
      (debug-rule "c_primary_tag_handle")
    (p/chr parser "!"))
    {:trace "c_primary_tag_handle" :name "c_primary_tag_handle"}))

;; [091]
;; c-secondary-tag-handle ::=
;;   '!' '!'
(def c_secondary_tag_handle
  (with-meta
    (fn c_secondary_tag_handle-fn [parser]
      (debug-rule "c_secondary_tag_handle")
    (p/all parser
     (p/chr parser
       "!"
    ),
    (p/chr parser
       "!"
    )
  ))
    {:trace "c_secondary_tag_handle" :name "c_secondary_tag_handle"}))

;; [092]
;; c-named-tag-handle ::=
;;   '!' ns-word-char+ '!'
(def c_named_tag_handle
  (with-meta
    (fn c_named_tag_handle-fn [parser]
      (debug-rule "c_named_tag_handle")
    (p/all parser
     (p/chr parser
       "!"
    ),
    (p/rep parser
       1,
      nil,
      ns_word_char
    ),
    (p/chr parser
       "!"
    )
  ))
    {:trace "c_named_tag_handle" :name "c_named_tag_handle"}))

;; [093]
;; ns-tag-prefix ::=
;;   c-ns-local-tag-prefix | ns-global-tag-prefix
(def ns_tag_prefix
  (with-meta
    (fn ns_tag_prefix-fn [parser]
      (debug-rule "ns_tag_prefix")
    (p/any parser
     c_ns_local_tag_prefix,
    ns_global_tag_prefix
  ))
    {:trace "ns_tag_prefix" :name "ns_tag_prefix"}))

;; [094]
;; c-ns-local-tag-prefix ::=
;;   '!' ns-uri-char*
(def c_ns_local_tag_prefix
  (with-meta
    (fn c_ns_local_tag_prefix-fn [parser]
      (debug-rule "c_ns_local_tag_prefix")
    (p/all parser
     (p/chr parser
       "!"
    ),
    (p/rep parser
       0,
      nil,
      ns_uri_char
    )
  ))
    {:trace "c_ns_local_tag_prefix" :name "c_ns_local_tag_prefix"}))

;; [095]
;; ns-global-tag-prefix ::=
;;   ns-tag-char ns-uri-char*
(def ns_global_tag_prefix
  (with-meta
    (fn ns_global_tag_prefix-fn [parser]
      (debug-rule "ns_global_tag_prefix")
    (p/all parser
     ns_tag_char,
    (p/rep parser
       0,
      nil,
      ns_uri_char
    )
  ))
    {:trace "ns_global_tag_prefix" :name "ns_global_tag_prefix"}))

;; [096]
;; c-ns-properties(n,c) ::=
;;   ( c-ns-tag-property
;;   ( s-separate(n,c) c-ns-anchor-property )? )
;;   | ( c-ns-anchor-property
;;   ( s-separate(n,c) c-ns-tag-property )? )
(def c_ns_properties
  (with-meta
    (fn c_ns_properties-fn [parser n c]
      (debug-rule "c_ns_properties", n, c)
    (p/any parser
     (p/all parser
       c_ns_tag_property,
      (p/rep parser
         0,
        1,
        (p/all parser
           [s_separate, n, c],
          c_ns_anchor_property
        ))
    ),
    (p/all parser
       c_ns_anchor_property,
      (p/rep parser
         0,
        1,
        (p/all parser
           [s_separate, n, c],
          c_ns_tag_property
        ))
    )
  ))
    {:trace "c_ns_properties" :name "c_ns_properties"}))

;; [097]
;; c-ns-tag-property ::=
;;   c-verbatim-tag
;;   | c-ns-shorthand-tag
;;   | c-non-specific-tag
(def c_ns_tag_property
  (with-meta
    (fn c_ns_tag_property-fn [parser]
      (debug-rule "c_ns_tag_property")
    (p/any parser
     c_verbatim_tag,
    c_ns_shorthand_tag,
    c_non_specific_tag
  ))
    {:trace "c_ns_tag_property" :name "c_ns_tag_property"}))

;; [098]
;; c-verbatim-tag ::=
;;   '!' '<' ns-uri-char+ '>'
(def c_verbatim_tag
  (with-meta
    (fn c_verbatim_tag-fn [parser]
      (debug-rule "c_verbatim_tag")
    (p/all parser
     (p/chr parser
       "!"
    ),
    (p/chr parser
       "<"
    ),
    (p/rep parser
       1,
      nil,
      ns_uri_char
    ),
    (p/chr parser
       ">"
    )
  ))
    {:trace "c_verbatim_tag" :name "c_verbatim_tag"}))

;; [099]
;; c-ns-shorthand-tag ::=
;;   c-tag-handle ns-tag-char+
(def c_ns_shorthand_tag
  (with-meta
    (fn c_ns_shorthand_tag-fn [parser]
      (debug-rule "c_ns_shorthand_tag")
    (p/all parser
     c_tag_handle,
    (p/rep parser
       1,
      nil,
      ns_tag_char
    )
  ))
    {:trace "c_ns_shorthand_tag" :name "c_ns_shorthand_tag"}))

;; [100]
;; c-non-specific-tag ::=
;;   '!'
(def c_non_specific_tag
  (with-meta
    (fn c_non_specific_tag-fn [parser]
      (debug-rule "c_non_specific_tag")
    (p/chr parser "!"))
    {:trace "c_non_specific_tag" :name "c_non_specific_tag"}))

;; [101]
;; c-ns-anchor-property ::=
;;   '&' ns-anchor-name
(def c_ns_anchor_property
  (with-meta
    (fn c_ns_anchor_property-fn [parser]
      (debug-rule "c_ns_anchor_property")
    (p/all parser
     (p/chr parser
       "&"
    ),
    ns_anchor_name
  ))
    {:trace "c_ns_anchor_property" :name "c_ns_anchor_property"}))

;; [102]
;; ns-anchor-char ::=
;;   ns-char - c-flow-indicator
(def ns_anchor_char
  (with-meta
    (fn ns_anchor_char-fn [parser]
      (debug-rule "ns_anchor_char")
    (p/but parser
     ns_char,
    c_flow_indicator
  ))
    {:trace "ns_anchor_char" :name "ns_anchor_char"}))

;; [103]
;; ns-anchor-name ::=
;;   ns-anchor-char+
(def ns_anchor_name
  (with-meta
    (fn ns_anchor_name-fn [parser]
      (debug-rule "ns_anchor_name")
    (p/rep2 parser 1, nil, ns_anchor_char))
    {:trace "ns_anchor_name" :name "ns_anchor_name"}))

;; [104]
;; c-ns-alias-node ::=
;;   '*' ns-anchor-name
(def c_ns_alias_node
  (with-meta
    (fn c_ns_alias_node-fn [parser]
      (debug-rule "c_ns_alias_node")
    (p/all parser
     (p/chr parser
       "*"
    ),
    ns_anchor_name
  ))
    {:trace "c_ns_alias_node" :name "c_ns_alias_node"}))

;; [105]
;; e-scalar ::=
;;   <empty>
(def e_scalar
  (with-meta
    (fn e_scalar-fn [parser]
      (debug-rule "e_scalar")
    empty-parser)
    {:trace "e_scalar" :name "e_scalar"}))

;; [106]
;; e-node ::=
;;   e-scalar
(def e_node
  (with-meta
    (fn e_node-fn [parser]
      (debug-rule "e_node")
    e_scalar)
    {:trace "e_node" :name "e_node"}))

;; [107]
;; nb-double-char ::=
;;   c-ns-esc-char | ( nb-json - '\\' - '"' )
(def nb_double_char
  (with-meta
    (fn nb_double_char-fn [parser]
      (debug-rule "nb_double_char")
    (p/any parser
     c_ns_esc_char,
    (p/but parser
       nb_json,
      (p/chr parser
         "\\"
      ),
      (p/chr parser
         "\""
      )
    )
  ))
    {:trace "nb_double_char" :name "nb_double_char"}))

;; [108]
;; ns-double-char ::=
;;   nb-double-char - s-white
(def ns_double_char
  (with-meta
    (fn ns_double_char-fn [parser]
      (debug-rule "ns_double_char")
    (p/but parser
     nb_double_char,
    s_white
  ))
    {:trace "ns_double_char" :name "ns_double_char"}))

;; [109]
;; c-double-quoted(n,c) ::=
;;   '"' nb-double-text(n,c)
;;   '"'
(def c_double_quoted
  (with-meta
    (fn c_double_quoted-fn [parser n c]
      (debug-rule "c_double_quoted", n, c)
    (p/all parser
     (p/chr parser
       "\""
    ),
    [nb_double_text, n, c],
    (p/chr parser
       "\""
    )
  ))
    {:trace "c_double_quoted" :name "c_double_quoted"}))

;; [110]
;; nb-double-text(n,c) ::=
;;   ( c = flow-out => nb-double-multi-line(n) )
;;   ( c = flow-in => nb-double-multi-line(n) )
;;   ( c = block-key => nb-double-one-line )
;;   ( c = flow-key => nb-double-one-line )
(def nb_double_text
  (with-meta
    (fn nb_double_text-fn [parser n c]
      (debug-rule "nb_double_text", n, c)
    (p/case* parser
     c,
    {
      "block-key" nb_double_one_line,
      "flow-in" [nb_double_multi_line, n],
      "flow-key" nb_double_one_line,
      "flow-out" [nb_double_multi_line, n],
    }
  ))
    {:trace "nb_double_text" :name "nb_double_text"}))

;; [111]
;; nb-double-one-line ::=
;;   nb-double-char*
(def nb_double_one_line
  (with-meta
    (fn nb_double_one_line-fn [parser]
      (debug-rule "nb_double_one_line")
    (p/rep2 parser 0, nil, nb_double_char))
    {:trace "nb_double_one_line" :name "nb_double_one_line"}))

;; [112]
;; s-double-escaped(n) ::=
;;   s-white* '\\'
;;   b-non-content
;;   l-empty(n,flow-in)* s-flow-line-prefix(n)
(def s_double_escaped
  (with-meta
    (fn s_double_escaped-fn [parser n]
      (debug-rule "s_double_escaped", n)
    (p/all parser
     (p/rep parser
       0,
      nil,
      s_white
    ),
    (p/chr parser
       "\\"
    ),
    b_non_content,
    (p/rep2 parser
       0,
      nil,
      [l_empty, n, "flow-in"]
    ),
    [s_flow_line_prefix, n]
  ))
    {:trace "s_double_escaped" :name "s_double_escaped"}))

;; [113]
;; s-double-break(n) ::=
;;   s-double-escaped(n) | s-flow-folded(n)
(def s_double_break
  (with-meta
    (fn s_double_break-fn [parser n]
      (debug-rule "s_double_break", n)
    (p/any parser
     [s_double_escaped, n],
    [s_flow_folded, n]
  ))
    {:trace "s_double_break" :name "s_double_break"}))

;; [114]
;; nb-ns-double-in-line ::=
;;   ( s-white* ns-double-char )*
(def nb_ns_double_in_line
  (with-meta
    (fn nb_ns_double_in_line-fn [parser]
      (debug-rule "nb_ns_double_in_line")
    (p/rep2 parser
     0,
    nil,
    (p/all parser
       (p/rep parser
         0,
        nil,
        s_white
      ),
      ns_double_char
    )))
    {:trace "nb_ns_double_in_line" :name "nb_ns_double_in_line"}))

;; [115]
;; s-double-next-line(n) ::=
;;   s-double-break(n)
;;   ( ns-double-char nb-ns-double-in-line
;;   ( s-double-next-line(n) | s-white* ) )?
(def s_double_next_line
  (with-meta
    (fn s_double_next_line-fn [parser n]
      (debug-rule "s_double_next_line", n)
    (p/all parser
     [s_double_break, n],
    (p/rep parser
       0,
      1,
      (p/all parser
         ns_double_char,
        nb_ns_double_in_line,
        (p/any parser
           [s_double_next_line, n],
          (p/rep parser
             0,
            nil,
            s_white
          )
        )
      ))
  ))
    {:trace "s_double_next_line" :name "s_double_next_line"}))

;; [116]
;; nb-double-multi-line(n) ::=
;;   nb-ns-double-in-line
;;   ( s-double-next-line(n) | s-white* )
(def nb_double_multi_line
  (with-meta
    (fn nb_double_multi_line-fn [parser n]
      (debug-rule "nb_double_multi_line", n)
    (p/all parser
     nb_ns_double_in_line,
    (p/any parser
       [s_double_next_line, n],
      (p/rep parser
         0,
        nil,
        s_white
      )
    )
  ))
    {:trace "nb_double_multi_line" :name "nb_double_multi_line"}))

;; [117]
;; c-quoted-quote ::=
;;   ''' '''
(def c_quoted_quote
  (with-meta
    (fn c_quoted_quote-fn [parser]
      (debug-rule "c_quoted_quote")
    (p/all parser
     (p/chr parser
       "'"
    ),
    (p/chr parser
       "'"
    )
  ))
    {:trace "c_quoted_quote" :name "c_quoted_quote"}))

;; [118]
;; nb-single-char ::=
;;   c-quoted-quote | ( nb-json - ''' )
(def nb_single_char
  (with-meta
    (fn nb_single_char-fn [parser]
      (debug-rule "nb_single_char")
    (p/any parser
     c_quoted_quote,
    (p/but parser
       nb_json,
      (p/chr parser
         "'"
      )
    )
  ))
    {:trace "nb_single_char" :name "nb_single_char"}))

;; [119]
;; ns-single-char ::=
;;   nb-single-char - s-white
(def ns_single_char
  (with-meta
    (fn ns_single_char-fn [parser]
      (debug-rule "ns_single_char")
    (p/but parser
     nb_single_char,
    s_white
  ))
    {:trace "ns_single_char" :name "ns_single_char"}))

;; [120]
;; c-single-quoted(n,c) ::=
;;   ''' nb-single-text(n,c)
;;   '''
(def c_single_quoted
  (with-meta
    (fn c_single_quoted-fn [parser n c]
      (debug-rule "c_single_quoted", n, c)
    (p/all parser
     (p/chr parser
       "'"
    ),
    [nb_single_text, n, c],
    (p/chr parser
       "'"
    )
  ))
    {:trace "c_single_quoted" :name "c_single_quoted"}))

;; [121]
;; nb-single-text(n,c) ::=
;;   ( c = flow-out => nb-single-multi-line(n) )
;;   ( c = flow-in => nb-single-multi-line(n) )
;;   ( c = block-key => nb-single-one-line )
;;   ( c = flow-key => nb-single-one-line )
(def nb_single_text
  (with-meta
    (fn nb_single_text-fn [parser n c]
      (debug-rule "nb_single_text", n, c)
    (p/case* parser
     c,
    {
      "block-key" nb_single_one_line,
      "flow-in" [nb_single_multi_line, n],
      "flow-key" nb_single_one_line,
      "flow-out" [nb_single_multi_line, n],
    }
  ))
    {:trace "nb_single_text" :name "nb_single_text"}))

;; [122]
;; nb-single-one-line ::=
;;   nb-single-char*
(def nb_single_one_line
  (with-meta
    (fn nb_single_one_line-fn [parser]
      (debug-rule "nb_single_one_line")
    (p/rep2 parser 0, nil, nb_single_char))
    {:trace "nb_single_one_line" :name "nb_single_one_line"}))

;; [123]
;; nb-ns-single-in-line ::=
;;   ( s-white* ns-single-char )*
(def nb_ns_single_in_line
  (with-meta
    (fn nb_ns_single_in_line-fn [parser]
      (debug-rule "nb_ns_single_in_line")
    (p/rep2 parser
     0,
    nil,
    (p/all parser
       (p/rep parser
         0,
        nil,
        s_white
      ),
      ns_single_char
    )))
    {:trace "nb_ns_single_in_line" :name "nb_ns_single_in_line"}))

;; [124]
;; s-single-next-line(n) ::=
;;   s-flow-folded(n)
;;   ( ns-single-char nb-ns-single-in-line
;;   ( s-single-next-line(n) | s-white* ) )?
(def s_single_next_line
  (with-meta
    (fn s_single_next_line-fn [parser n]
      (debug-rule "s_single_next_line", n)
    (p/all parser
     [s_flow_folded, n],
    (p/rep parser
       0,
      1,
      (p/all parser
         ns_single_char,
        nb_ns_single_in_line,
        (p/any parser
           [s_single_next_line, n],
          (p/rep parser
             0,
            nil,
            s_white
          )
        )
      ))
  ))
    {:trace "s_single_next_line" :name "s_single_next_line"}))

;; [125]
;; nb-single-multi-line(n) ::=
;;   nb-ns-single-in-line
;;   ( s-single-next-line(n) | s-white* )
(def nb_single_multi_line
  (with-meta
    (fn nb_single_multi_line-fn [parser n]
      (debug-rule "nb_single_multi_line", n)
    (p/all parser
     nb_ns_single_in_line,
    (p/any parser
       [s_single_next_line, n],
      (p/rep parser
         0,
        nil,
        s_white
      )
    )
  ))
    {:trace "nb_single_multi_line" :name "nb_single_multi_line"}))

;; [126]
;; ns-plain-first(c) ::=
;;   ( ns-char - c-indicator )
;;   | ( ( '?' | ':' | '-' )
;;   <followed_by_an_ns-plain-safe(c)> )
(def ns_plain_first
  (with-meta
    (fn ns_plain_first-fn [parser c]
      (debug-rule "ns_plain_first", c)
    (p/any parser
     (p/but parser
       ns_char,
      c_indicator
    ),
    (p/all parser
       (p/any parser
         (p/chr parser
           "?"
        ),
        (p/chr parser
           ":"
        ),
        (p/chr parser
           "-"
        )
      ),
      (p/chk parser
         "=",
        [ns_plain_safe, c]
      )
    )
  ))
    {:trace "ns_plain_first" :name "ns_plain_first"}))

;; [127]
;; ns-plain-safe(c) ::=
;;   ( c = flow-out => ns-plain-safe-out )
;;   ( c = flow-in => ns-plain-safe-in )
;;   ( c = block-key => ns-plain-safe-out )
;;   ( c = flow-key => ns-plain-safe-in )
(def ns_plain_safe
  (with-meta
    (fn ns_plain_safe-fn [parser c]
      (debug-rule "ns_plain_safe", c)
    (p/case* parser
     c,
    {
      "block-key" ns_plain_safe_out,
      "flow-in" ns_plain_safe_in,
      "flow-key" ns_plain_safe_in,
      "flow-out" ns_plain_safe_out,
    }
  ))
    {:trace "ns_plain_safe" :name "ns_plain_safe"}))

;; [128]
;; ns-plain-safe-out ::=
;;   ns-char
(def ns_plain_safe_out
  (with-meta
    (fn ns_plain_safe_out-fn [parser]
      (debug-rule "ns_plain_safe_out")
    ns_char)
    {:trace "ns_plain_safe_out" :name "ns_plain_safe_out"}))

;; [129]
;; ns-plain-safe-in ::=
;;   ns-char - c-flow-indicator
(def ns_plain_safe_in
  (with-meta
    (fn ns_plain_safe_in-fn [parser]
      (debug-rule "ns_plain_safe_in")
    (p/but parser
     ns_char,
    c_flow_indicator
  ))
    {:trace "ns_plain_safe_in" :name "ns_plain_safe_in"}))

;; [130]
;; ns-plain-char(c) ::=
;;   ( ns-plain-safe(c) - ':' - '#' )
;;   | ( <an_ns-char_preceding> '#' )
;;   | ( ':' <followed_by_an_ns-plain-safe(c)> )
(def ns_plain_char
  (with-meta
    (fn ns_plain_char-fn [parser c]
      (debug-rule "ns_plain_char", c)
    (p/any parser
     (p/but parser
       [ns_plain_safe, c],
      (p/chr parser
         ":"
      ),
      (p/chr parser
         "\u0023"
      )
    ),
    (p/all parser
       (p/chk parser
         "<=",
        ns_char
      ),
      (p/chr parser
         "\u0023"
      )
    ),
    (p/all parser
       (p/chr parser
         ":"
      ),
      (p/chk parser
         "=",
        [ns_plain_safe, c]
      )
    )
  ))
    {:trace "ns_plain_char" :name "ns_plain_char"}))

;; [131]
;; ns-plain(n,c) ::=
;;   ( c = flow-out => ns-plain-multi-line(n,c) )
;;   ( c = flow-in => ns-plain-multi-line(n,c) )
;;   ( c = block-key => ns-plain-one-line(c) )
;;   ( c = flow-key => ns-plain-one-line(c) )
(def ns_plain
  (with-meta
    (fn ns_plain-fn [parser n c]
      (debug-rule "ns_plain", n, c)
    (p/case* parser
     c,
    {
      "block-key" [ns_plain_one_line, c],
      "flow-in" [ns_plain_multi_line, n, c],
      "flow-key" [ns_plain_one_line, c],
      "flow-out" [ns_plain_multi_line, n, c],
    }
  ))
    {:trace "ns_plain" :name "ns_plain"}))

;; [132]
;; nb-ns-plain-in-line(c) ::=
;;   ( s-white*
;;   ns-plain-char(c) )*
(def nb_ns_plain_in_line
  (with-meta
    (fn nb_ns_plain_in_line-fn [parser c]
      (debug-rule "nb_ns_plain_in_line", c)
    (p/rep2 parser
     0,
    nil,
    (p/all parser
       (p/rep parser
         0,
        nil,
        s_white
      ),
      [ns_plain_char, c]
    )))
    {:trace "nb_ns_plain_in_line" :name "nb_ns_plain_in_line"}))

;; [133]
;; ns-plain-one-line(c) ::=
;;   ns-plain-first(c)
;;   nb-ns-plain-in-line(c)
(def ns_plain_one_line
  (with-meta
    (fn ns_plain_one_line-fn [parser c]
      (debug-rule "ns_plain_one_line", c)
    (p/all parser
     [ns_plain_first, c],
    [nb_ns_plain_in_line, c]
  ))
    {:trace "ns_plain_one_line" :name "ns_plain_one_line"}))

;; [134]
;; s-ns-plain-next-line(n,c) ::=
;;   s-flow-folded(n)
;;   ns-plain-char(c) nb-ns-plain-in-line(c)
(def s_ns_plain_next_line
  (with-meta
    (fn s_ns_plain_next_line-fn [parser n c]
      (debug-rule "s_ns_plain_next_line", n, c)
    (p/all parser
     [s_flow_folded, n],
    [ns_plain_char, c],
    [nb_ns_plain_in_line, c]
  ))
    {:trace "s_ns_plain_next_line" :name "s_ns_plain_next_line"}))

;; [135]
;; ns-plain-multi-line(n,c) ::=
;;   ns-plain-one-line(c)
;;   s-ns-plain-next-line(n,c)*
(def ns_plain_multi_line
  (with-meta
    (fn ns_plain_multi_line-fn [parser n c]
      (debug-rule "ns_plain_multi_line", n, c)
    (p/all parser
     [ns_plain_one_line, c],
    (p/rep parser
       0,
      nil,
      [s_ns_plain_next_line, n, c]
    )
  ))
    {:trace "ns_plain_multi_line" :name "ns_plain_multi_line"}))

;; [136]
;; in-flow(c) ::=
;;   ( c = flow-out => flow-in )
;;   ( c = flow-in => flow-in )
;;   ( c = block-key => flow-key )
;;   ( c = flow-key => flow-key )
(def in_flow
  (with-meta
    (fn in_flow-fn [parser c]
      (debug-rule "in_flow", c)
    (p/flip parser
     c,
    {
      "block-key" "flow-key",
      "flow-in" "flow-in",
      "flow-key" "flow-key",
      "flow-out" "flow-in",
    }
  ))
    {:trace "in_flow" :name "in_flow"}))

;; [137]
;; c-flow-sequence(n,c) ::=
;;   '[' s-separate(n,c)?
;;   ns-s-flow-seq-entries(n,in-flow(c))? ']'
(def c_flow_sequence
  (with-meta
    (fn c_flow_sequence-fn [parser n c]
      (debug-rule "c_flow_sequence", n, c)
    (p/all parser
     (p/chr parser
       "["
    ),
    (p/rep parser
       0,
      1,
      [s_separate, n, c]
    ),
    (p/rep2 parser
       0,
      1,
      [ns_s_flow_seq_entries, n, [in_flow, c]]
    ),
    (p/chr parser
       "]"
    )
  ))
    {:trace "c_flow_sequence" :name "c_flow_sequence"}))

;; [138]
;; ns-s-flow-seq-entries(n,c) ::=
;;   ns-flow-seq-entry(n,c)
;;   s-separate(n,c)?
;;   ( ',' s-separate(n,c)?
;;   ns-s-flow-seq-entries(n,c)? )?
(def ns_s_flow_seq_entries
  (with-meta
    (fn ns_s_flow_seq_entries-fn [parser n c]
      (debug-rule "ns_s_flow_seq_entries", n, c)
    (p/all parser
     [ns_flow_seq_entry, n, c],
    (p/rep parser
       0,
      1,
      [s_separate, n, c]
    ),
    (p/rep2 parser
       0,
      1,
      (p/all parser
         (p/chr parser
           ","
        ),
        (p/rep parser
           0,
          1,
          [s_separate, n, c]
        ),
        (p/rep2 parser
           0,
          1,
          [ns_s_flow_seq_entries, n, c]
        )
      ))
  ))
    {:trace "ns_s_flow_seq_entries" :name "ns_s_flow_seq_entries"}))

;; [139]
;; ns-flow-seq-entry(n,c) ::=
;;   ns-flow-pair(n,c) | ns-flow-node(n,c)
(def ns_flow_seq_entry
  (with-meta
    (fn ns_flow_seq_entry-fn [parser n c]
      (debug-rule "ns_flow_seq_entry", n, c)
    (p/any parser
     [ns_flow_pair, n, c],
    [ns_flow_node, n, c]
  ))
    {:trace "ns_flow_seq_entry" :name "ns_flow_seq_entry"}))

;; [140]
;; c-flow-mapping(n,c) ::=
;;   '{' s-separate(n,c)?
;;   ns-s-flow-map-entries(n,in-flow(c))? '}'
(def c_flow_mapping
  (with-meta
    (fn c_flow_mapping-fn [parser n c]
      (debug-rule "c_flow_mapping", n, c)
    (p/all parser
     (p/chr parser
       "\u007B"
    ),
    (p/rep parser
       0,
      1,
      [s_separate, n, c]
    ),
    (p/rep2 parser
       0,
      1,
      [ns_s_flow_map_entries, n, [in_flow, c]]
    ),
    (p/chr parser
       "\u007D"
    )
  ))
    {:trace "c_flow_mapping" :name "c_flow_mapping"}))

;; [141]
;; ns-s-flow-map-entries(n,c) ::=
;;   ns-flow-map-entry(n,c)
;;   s-separate(n,c)?
;;   ( ',' s-separate(n,c)?
;;   ns-s-flow-map-entries(n,c)? )?
(def ns_s_flow_map_entries
  (with-meta
    (fn ns_s_flow_map_entries-fn [parser n c]
      (debug-rule "ns_s_flow_map_entries", n, c)
    (p/all parser
     [ns_flow_map_entry, n, c],
    (p/rep parser
       0,
      1,
      [s_separate, n, c]
    ),
    (p/rep2 parser
       0,
      1,
      (p/all parser
         (p/chr parser
           ","
        ),
        (p/rep parser
           0,
          1,
          [s_separate, n, c]
        ),
        (p/rep2 parser
           0,
          1,
          [ns_s_flow_map_entries, n, c]
        )
      ))
  ))
    {:trace "ns_s_flow_map_entries" :name "ns_s_flow_map_entries"}))

;; [142]
;; ns-flow-map-entry(n,c) ::=
;;   ( '?' s-separate(n,c)
;;   ns-flow-map-explicit-entry(n,c) )
;;   | ns-flow-map-implicit-entry(n,c)
(def ns_flow_map_entry
  (with-meta
    (fn ns_flow_map_entry-fn [parser n c]
      (debug-rule "ns_flow_map_entry", n, c)
    (p/any parser
     (p/all parser
       (p/chr parser
         "?"
      ),
      (p/chk parser
         "=",
        (p/any parser
           (p/end-of-stream parser),
          s_white,
          b_break
        )
      ),
      [s_separate, n, c],
      [ns_flow_map_explicit_entry, n, c]
    ),
    [ns_flow_map_implicit_entry, n, c]
  ))
    {:trace "ns_flow_map_entry" :name "ns_flow_map_entry"}))

;; [143]
;; ns-flow-map-explicit-entry(n,c) ::=
;;   ns-flow-map-implicit-entry(n,c)
;;   | ( e-node
;;   e-node )
(def ns_flow_map_explicit_entry
  (with-meta
    (fn ns_flow_map_explicit_entry-fn [parser n c]
      (debug-rule "ns_flow_map_explicit_entry", n, c)
    (p/any parser
     [ns_flow_map_implicit_entry, n, c],
    (p/all parser
       e_node,
      e_node
    )
  ))
    {:trace "ns_flow_map_explicit_entry" :name "ns_flow_map_explicit_entry"}))

;; [144]
;; ns-flow-map-implicit-entry(n,c) ::=
;;   ns-flow-map-yaml-key-entry(n,c)
;;   | c-ns-flow-map-empty-key-entry(n,c)
;;   | c-ns-flow-map-json-key-entry(n,c)
(def ns_flow_map_implicit_entry
  (with-meta
    (fn ns_flow_map_implicit_entry-fn [parser n c]
      (debug-rule "ns_flow_map_implicit_entry", n, c)
    (p/any parser
     [ns_flow_map_yaml_key_entry, n, c],
    [c_ns_flow_map_empty_key_entry, n, c],
    [c_ns_flow_map_json_key_entry, n, c]
  ))
    {:trace "ns_flow_map_implicit_entry" :name "ns_flow_map_implicit_entry"}))

;; [145]
;; ns-flow-map-yaml-key-entry(n,c) ::=
;;   ns-flow-yaml-node(n,c)
;;   ( ( s-separate(n,c)?
;;   c-ns-flow-map-separate-value(n,c) )
;;   | e-node )
(def ns_flow_map_yaml_key_entry
  (with-meta
    (fn ns_flow_map_yaml_key_entry-fn [parser n c]
      (debug-rule "ns_flow_map_yaml_key_entry", n, c)
    (p/all parser
     [ns_flow_yaml_node, n, c],
    (p/any parser
       (p/all parser
         (p/rep parser
           0,
          1,
          [s_separate, n, c]
        ),
        [c_ns_flow_map_separate_value, n, c]
      ),
      e_node
    )
  ))
    {:trace "ns_flow_map_yaml_key_entry" :name "ns_flow_map_yaml_key_entry"}))

;; [146]
;; c-ns-flow-map-empty-key-entry(n,c) ::=
;;   e-node
;;   c-ns-flow-map-separate-value(n,c)
(def c_ns_flow_map_empty_key_entry
  (with-meta
    (fn c_ns_flow_map_empty_key_entry-fn [parser n c]
      (debug-rule "c_ns_flow_map_empty_key_entry", n, c)
    (p/all parser
     e_node,
    [c_ns_flow_map_separate_value, n, c]
  ))
    {:trace "c_ns_flow_map_empty_key_entry" :name "c_ns_flow_map_empty_key_entry"}))

;; [147]
;; c-ns-flow-map-separate-value(n,c) ::=
;;   ':' <not_followed_by_an_ns-plain-safe(c)>
;;   ( ( s-separate(n,c) ns-flow-node(n,c) )
;;   | e-node )
(def c_ns_flow_map_separate_value
  (with-meta
    (fn c_ns_flow_map_separate_value-fn [parser n c]
      (debug-rule "c_ns_flow_map_separate_value", n, c)
    (p/all parser
     (p/chr parser
       ":"
    ),
    (p/chk parser
       "!",
      [ns_plain_safe, c]
    ),
    (p/any parser
       (p/all parser
         [s_separate, n, c],
        [ns_flow_node, n, c]
      ),
      e_node
    )
  ))
    {:trace "c_ns_flow_map_separate_value" :name "c_ns_flow_map_separate_value"}))

;; [148]
;; c-ns-flow-map-json-key-entry(n,c) ::=
;;   c-flow-json-node(n,c)
;;   ( ( s-separate(n,c)?
;;   c-ns-flow-map-adjacent-value(n,c) )
;;   | e-node )
(def c_ns_flow_map_json_key_entry
  (with-meta
    (fn c_ns_flow_map_json_key_entry-fn [parser n c]
      (debug-rule "c_ns_flow_map_json_key_entry", n, c)
    (p/all parser
     [c_flow_json_node, n, c],
    (p/any parser
       (p/all parser
         (p/rep parser
           0,
          1,
          [s_separate, n, c]
        ),
        [c_ns_flow_map_adjacent_value, n, c]
      ),
      e_node
    )
  ))
    {:trace "c_ns_flow_map_json_key_entry" :name "c_ns_flow_map_json_key_entry"}))

;; [149]
;; c-ns-flow-map-adjacent-value(n,c) ::=
;;   ':' ( (
;;   s-separate(n,c)?
;;   ns-flow-node(n,c) )
;;   | e-node )
(def c_ns_flow_map_adjacent_value
  (with-meta
    (fn c_ns_flow_map_adjacent_value-fn [parser n c]
      (debug-rule "c_ns_flow_map_adjacent_value", n, c)
    (p/all parser
     (p/chr parser
       ":"
    ),
    (p/any parser
       (p/all parser
         (p/rep parser
           0,
          1,
          [s_separate, n, c]
        ),
        [ns_flow_node, n, c]
      ),
      e_node
    )
  ))
    {:trace "c_ns_flow_map_adjacent_value" :name "c_ns_flow_map_adjacent_value"}))

;; [150]
;; ns-flow-pair(n,c) ::=
;;   ( '?' s-separate(n,c)
;;   ns-flow-map-explicit-entry(n,c) )
;;   | ns-flow-pair-entry(n,c)
(def ns_flow_pair
  (with-meta
    (fn ns_flow_pair-fn [parser n c]
      (debug-rule "ns_flow_pair", n, c)
    (p/any parser
     (p/all parser
       (p/chr parser
         "?"
      ),
      (p/chk parser
         "=",
        (p/any parser
           (p/end-of-stream parser),
          s_white,
          b_break
        )
      ),
      [s_separate, n, c],
      [ns_flow_map_explicit_entry, n, c]
    ),
    [ns_flow_pair_entry, n, c]
  ))
    {:trace "ns_flow_pair" :name "ns_flow_pair"}))

;; [151]
;; ns-flow-pair-entry(n,c) ::=
;;   ns-flow-pair-yaml-key-entry(n,c)
;;   | c-ns-flow-map-empty-key-entry(n,c)
;;   | c-ns-flow-pair-json-key-entry(n,c)
(def ns_flow_pair_entry
  (with-meta
    (fn ns_flow_pair_entry-fn [parser n c]
      (debug-rule "ns_flow_pair_entry", n, c)
    (p/any parser
     [ns_flow_pair_yaml_key_entry, n, c],
    [c_ns_flow_map_empty_key_entry, n, c],
    [c_ns_flow_pair_json_key_entry, n, c]
  ))
    {:trace "ns_flow_pair_entry" :name "ns_flow_pair_entry"}))

;; [152]
;; ns-flow-pair-yaml-key-entry(n,c) ::=
;;   ns-s-implicit-yaml-key(flow-key)
;;   c-ns-flow-map-separate-value(n,c)
(def ns_flow_pair_yaml_key_entry
  (with-meta
    (fn ns_flow_pair_yaml_key_entry-fn [parser n c]
      (debug-rule "ns_flow_pair_yaml_key_entry", n, c)
    (p/all parser
     [ns_s_implicit_yaml_key, "flow-key"],
    [c_ns_flow_map_separate_value, n, c]
  ))
    {:trace "ns_flow_pair_yaml_key_entry" :name "ns_flow_pair_yaml_key_entry"}))

;; [153]
;; c-ns-flow-pair-json-key-entry(n,c) ::=
;;   c-s-implicit-json-key(flow-key)
;;   c-ns-flow-map-adjacent-value(n,c)
(def c_ns_flow_pair_json_key_entry
  (with-meta
    (fn c_ns_flow_pair_json_key_entry-fn [parser n c]
      (debug-rule "c_ns_flow_pair_json_key_entry", n, c)
    (p/all parser
     [c_s_implicit_json_key, "flow-key"],
    [c_ns_flow_map_adjacent_value, n, c]
  ))
    {:trace "c_ns_flow_pair_json_key_entry" :name "c_ns_flow_pair_json_key_entry"}))

;; [154]
;; ns-s-implicit-yaml-key(c) ::=
;;   ns-flow-yaml-node(n/a,c)
;;   s-separate-in-line?
;;   <at_most_1024_characters_altogether>
(def ns_s_implicit_yaml_key
  (with-meta
    (fn ns_s_implicit_yaml_key-fn [parser c]
      (debug-rule "ns_s_implicit_yaml_key", c)
    (p/all parser
     (p/max* parser
       1024
    ),
    [ns_flow_yaml_node, nil, c],
    (p/rep parser
       0,
      1,
      s_separate_in_line
    )
  ))
    {:trace "ns_s_implicit_yaml_key" :name "ns_s_implicit_yaml_key"}))

;; [155]
;; c-s-implicit-json-key(c) ::=
;;   c-flow-json-node(n/a,c)
;;   s-separate-in-line?
;;   <at_most_1024_characters_altogether>
(def c_s_implicit_json_key
  (with-meta
    (fn c_s_implicit_json_key-fn [parser c]
      (debug-rule "c_s_implicit_json_key", c)
    (p/all parser
     (p/max* parser
       1024
    ),
    [c_flow_json_node, nil, c],
    (p/rep parser
       0,
      1,
      s_separate_in_line
    )
  ))
    {:trace "c_s_implicit_json_key" :name "c_s_implicit_json_key"}))

;; [156]
;; ns-flow-yaml-content(n,c) ::=
;;   ns-plain(n,c)
(def ns_flow_yaml_content
  (with-meta
    (fn ns_flow_yaml_content-fn [parser n c]
      (debug-rule "ns_flow_yaml_content", n, c)
    [ns_plain, n, c])
    {:trace "ns_flow_yaml_content" :name "ns_flow_yaml_content"}))

;; [157]
;; c-flow-json-content(n,c) ::=
;;   c-flow-sequence(n,c) | c-flow-mapping(n,c)
;;   | c-single-quoted(n,c) | c-double-quoted(n,c)
(def c_flow_json_content
  (with-meta
    (fn c_flow_json_content-fn [parser n c]
      (debug-rule "c_flow_json_content", n, c)
    (p/any parser
     [c_flow_sequence, n, c],
    [c_flow_mapping, n, c],
    [c_single_quoted, n, c],
    [c_double_quoted, n, c]
  ))
    {:trace "c_flow_json_content" :name "c_flow_json_content"}))

;; [158]
;; ns-flow-content(n,c) ::=
;;   ns-flow-yaml-content(n,c) | c-flow-json-content(n,c)
(def ns_flow_content
  (with-meta
    (fn ns_flow_content-fn [parser n c]
      (debug-rule "ns_flow_content", n, c)
    (p/any parser
     [ns_flow_yaml_content, n, c],
    [c_flow_json_content, n, c]
  ))
    {:trace "ns_flow_content" :name "ns_flow_content"}))

;; [159]
;; ns-flow-yaml-node(n,c) ::=
;;   c-ns-alias-node
;;   | ns-flow-yaml-content(n,c)
;;   | ( c-ns-properties(n,c)
;;   ( ( s-separate(n,c)
;;   ns-flow-yaml-content(n,c) )
;;   | e-scalar ) )
(def ns_flow_yaml_node
  (with-meta
    (fn ns_flow_yaml_node-fn [parser n c]
      (debug-rule "ns_flow_yaml_node", n, c)
    (p/any parser
     c_ns_alias_node,
    [ns_flow_yaml_content, n, c],
    (p/all parser
       [c_ns_properties, n, c],
      (p/any parser
         (p/all parser
           [s_separate, n, c],
          [ns_flow_content, n, c]
        ),
        e_scalar
      )
    )
  ))
    {:trace "ns_flow_yaml_node" :name "ns_flow_yaml_node"}))

;; [160]
;; c-flow-json-node(n,c) ::=
;;   ( c-ns-properties(n,c)
;;   s-separate(n,c) )?
;;   c-flow-json-content(n,c)
(def c_flow_json_node
  (with-meta
    (fn c_flow_json_node-fn [parser n c]
      (debug-rule "c_flow_json_node", n, c)
    (p/all parser
     (p/rep parser
       0,
      1,
      (p/all parser
         [c_ns_properties, n, c],
        [s_separate, n, c]
      )),
    [c_flow_json_content, n, c]
  ))
    {:trace "c_flow_json_node" :name "c_flow_json_node"}))

;; [161]
;; ns-flow-node(n,c) ::=
;;   c-ns-alias-node
;;   | ns-flow-content(n,c)
;;   | ( c-ns-properties(n,c)
;;   ( ( s-separate(n,c)
;;   ns-flow-content(n,c) )
;;   | e-scalar ) )
(def ns_flow_node
  (with-meta
    (fn ns_flow_node-fn [parser n c]
      (debug-rule "ns_flow_node", n, c)
    (p/any parser
     c_ns_alias_node,
    [ns_flow_content, n, c],
    (p/all parser
       [c_ns_properties, n, c],
      (p/any parser
         (p/all parser
           [s_separate, n, c],
          [ns_flow_content, n, c]
        ),
        e_scalar
      )
    )
  ))
    {:trace "ns_flow_node" :name "ns_flow_node"}))

;; [162]
;; c-b-block-header(m,t) ::=
;;   ( ( c-indentation-indicator(m)
;;   c-chomping-indicator(t) )
;;   | ( c-chomping-indicator(t)
;;   c-indentation-indicator(m) ) )
;;   s-b-comment
(def c_b_block_header
  (with-meta
    (fn c_b_block_header-fn [parser n]
      (debug-rule "c_b_block_header", n)
    (p/all parser
     (p/any parser
       (p/all parser
         [c_indentation_indicator, n],
        c_chomping_indicator,
        (p/chk parser
           "=",
          (p/any parser
             (p/end-of-stream parser),
            s_white,
            b_break
          )
        )
      ),
      (p/all parser
         c_chomping_indicator,
        [c_indentation_indicator, n],
        (p/chk parser
           "=",
          (p/any parser
             (p/end-of-stream parser),
            s_white,
            b_break
          )
        )
      )
    ),
    s_b_comment
  ))
    {:trace "c_b_block_header" :name "c_b_block_header"}))

;; [163]
;; c-indentation-indicator(m) ::=
;;   ( ns-dec-digit => m = ns-dec-digit - x:30 )
;;   ( <empty> => m = auto-detect() )
(def c_indentation_indicator
  (with-meta
    (fn c_indentation_indicator-fn [parser n]
      (debug-rule "c_indentation_indicator", n)
    (p/any parser
     (p/if* parser
       (p/rng parser
         "\u0031",
        "\u0039"
      ),
      (p/set* parser
         "m",
        (p/ord parser
           (p/match parser)
        )
      )
    ),
    (p/if* parser
       empty-parser,
      (p/set* parser
         "m",
        [auto_detect, n]
      )
    )
  ))
    {:trace "c_indentation_indicator" :name "c_indentation_indicator"}))

;; [164]
;; c-chomping-indicator(t) ::=
;;   ( '-' => t = strip )
;;   ( '+' => t = keep )
;;   ( <empty> => t = clip )
(def c_chomping_indicator
  (with-meta
    (fn c_chomping_indicator-fn [parser]
      (debug-rule "c_chomping_indicator")
    (p/any parser
     (p/if* parser
       (p/chr parser
         "-"
      ),
      (p/set* parser
         "t",
        "strip"
      )
    ),
    (p/if* parser
       (p/chr parser
         "+"
      ),
      (p/set* parser
         "t",
        "keep"
      )
    ),
    (p/if* parser
       empty-parser,
      (p/set* parser
         "t",
        "clip"
      )
    )
  ))
    {:trace "c_chomping_indicator" :name "c_chomping_indicator"}))

;; [165]
;; b-chomped-last(t) ::=
;;   ( t = strip => b-non-content | <end_of_file> )
;;   ( t = clip => b-as-line-feed | <end_of_file> )
;;   ( t = keep => b-as-line-feed | <end_of_file> )
(def b_chomped_last
  (with-meta
    (fn b_chomped_last-fn [parser t]
      (debug-rule "b_chomped_last", t)
    (p/case* parser
     t,
    {
      "clip" (p/any parser b_as_line_feed, (p/end-of-stream parser) ),
      "keep" (p/any parser b_as_line_feed, (p/end-of-stream parser) ),
      "strip" (p/any parser b_non_content, (p/end-of-stream parser) ),
    }
  ))
    {:trace "b_chomped_last" :name "b_chomped_last"}))

;; [166]
;; l-chomped-empty(n,t) ::=
;;   ( t = strip => l-strip-empty(n) )
;;   ( t = clip => l-strip-empty(n) )
;;   ( t = keep => l-keep-empty(n) )
(def l_chomped_empty
  (with-meta
    (fn l_chomped_empty-fn [parser n t]
      (debug-rule "l_chomped_empty", n, t)
    (p/case* parser
     t,
    {
      "clip" [l_strip_empty, n],
      "keep" [l_keep_empty, n],
      "strip" [l_strip_empty, n],
    }
  ))
    {:trace "l_chomped_empty" :name "l_chomped_empty"}))

;; [167]
;; l-strip-empty(n) ::=
;;   ( s-indent(<=n) b-non-content )*
;;   l-trail-comments(n)?
(def l_strip_empty
  (with-meta
    (fn l_strip_empty-fn [parser n]
      (debug-rule "l_strip_empty", n)
    (p/all parser
     (p/rep parser
       0,
      nil,
      (p/all parser
         [s_indent_le, n],
        b_non_content
      )),
    (p/rep2 parser
       0,
      1,
      [l_trail_comments, n]
    )
  ))
    {:trace "l_strip_empty" :name "l_strip_empty"}))

;; [168]
;; l-keep-empty(n) ::=
;;   l-empty(n,block-in)*
;;   l-trail-comments(n)?
(def l_keep_empty
  (with-meta
    (fn l_keep_empty-fn [parser n]
      (debug-rule "l_keep_empty", n)
    (p/all parser
     (p/rep parser
       0,
      nil,
      [l_empty, n, "block-in"]
    ),
    (p/rep2 parser
       0,
      1,
      [l_trail_comments, n]
    )
  ))
    {:trace "l_keep_empty" :name "l_keep_empty"}))

;; [169]
;; l-trail-comments(n) ::=
;;   s-indent(<n)
;;   c-nb-comment-text b-comment
;;   l-comment*
(def l_trail_comments
  (with-meta
    (fn l_trail_comments-fn [parser n]
      (debug-rule "l_trail_comments", n)
    (p/all parser
     [s_indent_lt, n],
    c_nb_comment_text,
    b_comment,
    (p/rep parser
       0,
      nil,
      l_comment
    )
  ))
    {:trace "l_trail_comments" :name "l_trail_comments"}))

;; [170]
;; c-l+literal(n) ::=
;;   '|' c-b-block-header(m,t)
;;   l-literal-content(n+m,t)
(def c_l_literal
  (with-meta
    (fn c_l_literal-fn [parser n]
      (debug-rule "c_l_literal", n)
    (p/all parser
     (p/chr parser
       "|"
    ),
    [c_b_block_header, n],
    [l_literal_content, (p/add parser
       n,
      (p/m parser
      )
    ), (p/t parser
    )]
  ))
    {:trace "c_l_literal" :name "c_l_literal"}))

;; [171]
;; l-nb-literal-text(n) ::=
;;   l-empty(n,block-in)*
;;   s-indent(n) nb-char+
(def l_nb_literal_text
  (with-meta
    (fn l_nb_literal_text-fn [parser n]
      (debug-rule "l_nb_literal_text", n)
    (p/all parser
     (p/rep parser
       0,
      nil,
      [l_empty, n, "block-in"]
    ),
    [s_indent, n],
    (p/rep2 parser
       1,
      nil,
      nb_char
    )
  ))
    {:trace "l_nb_literal_text" :name "l_nb_literal_text"}))

;; [172]
;; b-nb-literal-next(n) ::=
;;   b-as-line-feed
;;   l-nb-literal-text(n)
(def b_nb_literal_next
  (with-meta
    (fn b_nb_literal_next-fn [parser n]
      (debug-rule "b_nb_literal_next", n)
    (p/all parser
     b_as_line_feed,
    [l_nb_literal_text, n]
  ))
    {:trace "b_nb_literal_next" :name "b_nb_literal_next"}))

;; [173]
;; l-literal-content(n,t) ::=
;;   ( l-nb-literal-text(n)
;;   b-nb-literal-next(n)*
;;   b-chomped-last(t) )?
;;   l-chomped-empty(n,t)
(def l_literal_content
  (with-meta
    (fn l_literal_content-fn [parser n t]
      (debug-rule "l_literal_content", n, t)
    (p/all parser
     (p/rep parser
       0,
      1,
      (p/all parser
         [l_nb_literal_text, n],
        (p/rep parser
           0,
          nil,
          [b_nb_literal_next, n]
        ),
        [b_chomped_last, (p/t parser
        )]
      )),
    [l_chomped_empty, n, (p/t parser
    )]
  ))
    {:trace "l_literal_content" :name "l_literal_content"}))

;; [174]
;; c-l+folded(n) ::=
;;   '>' c-b-block-header(m,t)
;;   l-folded-content(n+m,t)
(def c_l_folded
  (with-meta
    (fn c_l_folded-fn [parser n]
      (debug-rule "c_l_folded", n)
    (p/all parser
     (p/chr parser
       ">"
    ),
    [c_b_block_header, n],
    [l_folded_content, (p/add parser
       n,
      (p/m parser
      )
    ), (p/t parser
    )]
  ))
    {:trace "c_l_folded" :name "c_l_folded"}))

;; [175]
;; s-nb-folded-text(n) ::=
;;   s-indent(n) ns-char
;;   nb-char*
(def s_nb_folded_text
  (with-meta
    (fn s_nb_folded_text-fn [parser n]
      (debug-rule "s_nb_folded_text", n)
    (p/all parser
     [s_indent, n],
    ns_char,
    (p/rep parser
       0,
      nil,
      nb_char
    )
  ))
    {:trace "s_nb_folded_text" :name "s_nb_folded_text"}))

;; [176]
;; l-nb-folded-lines(n) ::=
;;   s-nb-folded-text(n)
;;   ( b-l-folded(n,block-in) s-nb-folded-text(n) )*
(def l_nb_folded_lines
  (with-meta
    (fn l_nb_folded_lines-fn [parser n]
      (debug-rule "l_nb_folded_lines", n)
    (p/all parser
     [s_nb_folded_text, n],
    (p/rep parser
       0,
      nil,
      (p/all parser
         [b_l_folded, n, "block-in"],
        [s_nb_folded_text, n]
      ))
  ))
    {:trace "l_nb_folded_lines" :name "l_nb_folded_lines"}))

;; [177]
;; s-nb-spaced-text(n) ::=
;;   s-indent(n) s-white
;;   nb-char*
(def s_nb_spaced_text
  (with-meta
    (fn s_nb_spaced_text-fn [parser n]
      (debug-rule "s_nb_spaced_text", n)
    (p/all parser
     [s_indent, n],
    s_white,
    (p/rep parser
       0,
      nil,
      nb_char
    )
  ))
    {:trace "s_nb_spaced_text" :name "s_nb_spaced_text"}))

;; [178]
;; b-l-spaced(n) ::=
;;   b-as-line-feed
;;   l-empty(n,block-in)*
(def b_l_spaced
  (with-meta
    (fn b_l_spaced-fn [parser n]
      (debug-rule "b_l_spaced", n)
    (p/all parser
     b_as_line_feed,
    (p/rep parser
       0,
      nil,
      [l_empty, n, "block-in"]
    )
  ))
    {:trace "b_l_spaced" :name "b_l_spaced"}))

;; [179]
;; l-nb-spaced-lines(n) ::=
;;   s-nb-spaced-text(n)
;;   ( b-l-spaced(n) s-nb-spaced-text(n) )*
(def l_nb_spaced_lines
  (with-meta
    (fn l_nb_spaced_lines-fn [parser n]
      (debug-rule "l_nb_spaced_lines", n)
    (p/all parser
     [s_nb_spaced_text, n],
    (p/rep parser
       0,
      nil,
      (p/all parser
         [b_l_spaced, n],
        [s_nb_spaced_text, n]
      ))
  ))
    {:trace "l_nb_spaced_lines" :name "l_nb_spaced_lines"}))

;; [180]
;; l-nb-same-lines(n) ::=
;;   l-empty(n,block-in)*
;;   ( l-nb-folded-lines(n) | l-nb-spaced-lines(n) )
(def l_nb_same_lines
  (with-meta
    (fn l_nb_same_lines-fn [parser n]
      (debug-rule "l_nb_same_lines", n)
    (p/all parser
     (p/rep parser
       0,
      nil,
      [l_empty, n, "block-in"]
    ),
    (p/any parser
       [l_nb_folded_lines, n],
      [l_nb_spaced_lines, n]
    )
  ))
    {:trace "l_nb_same_lines" :name "l_nb_same_lines"}))

;; [181]
;; l-nb-diff-lines(n) ::=
;;   l-nb-same-lines(n)
;;   ( b-as-line-feed l-nb-same-lines(n) )*
(def l_nb_diff_lines
  (with-meta
    (fn l_nb_diff_lines-fn [parser n]
      (debug-rule "l_nb_diff_lines", n)
    (p/all parser
     [l_nb_same_lines, n],
    (p/rep parser
       0,
      nil,
      (p/all parser
         b_as_line_feed,
        [l_nb_same_lines, n]
      ))
  ))
    {:trace "l_nb_diff_lines" :name "l_nb_diff_lines"}))

;; [182]
;; l-folded-content(n,t) ::=
;;   ( l-nb-diff-lines(n)
;;   b-chomped-last(t) )?
;;   l-chomped-empty(n,t)
(def l_folded_content
  (with-meta
    (fn l_folded_content-fn [parser n t]
      (debug-rule "l_folded_content", n, t)
    (p/all parser
     (p/rep parser
       0,
      1,
      (p/all parser
         [l_nb_diff_lines, n],
        [b_chomped_last, (p/t parser
        )]
      )),
    [l_chomped_empty, n, (p/t parser
    )]
  ))
    {:trace "l_folded_content" :name "l_folded_content"}))

;; [183]
;; l+block-sequence(n) ::=
;;   ( s-indent(n+m)
;;   c-l-block-seq-entry(n+m) )+
;;   <for_some_fixed_auto-detected_m_>_0>
(def l_block_sequence
  (with-meta
    (fn l_block_sequence-fn [parser n]
  ;; m must be > 0 (JS treats 0 as falsy, Clojure doesn't)
  (let [m (p/call parser [auto_detect_indent, n] "number")]
    (if (and m (pos? m))
    (do
      (debug-rule "l_block_sequence", n)
    (p/all parser
     (p/rep parser
       1,
      nil,
      (p/all parser
         [s_indent, (p/add parser
           n,
          m
        )],
        [c_l_block_seq_entry, (p/add parser
           n,
          m
        )]
      ))
  ))
      false)))
    {:trace "l_block_sequence" :name "l_block_sequence"}))

;; [184]
;; c-l-block-seq-entry(n) ::=
;;   '-' <not_followed_by_an_ns-char>
;;   s-l+block-indented(n,block-in)
(def c_l_block_seq_entry
  (with-meta
    (fn c_l_block_seq_entry-fn [parser n]
      (debug-rule "c_l_block_seq_entry", n)
    (p/all parser
     (p/chr parser
       "-"
    ),
    (p/chk parser
       "!",
      ns_char
    ),
    [s_l_block_indented, n, "block-in"]
  ))
    {:trace "c_l_block_seq_entry" :name "c_l_block_seq_entry"}))

;; [185]
;; s-l+block-indented(n,c) ::=
;;   ( s-indent(m)
;;   ( ns-l-compact-sequence(n+1+m)
;;   | ns-l-compact-mapping(n+1+m) ) )
;;   | s-l+block-node(n,c)
;;   | ( e-node s-l-comments )
(def s_l_block_indented
  (with-meta
    (fn s_l_block_indented-fn [parser n c]
  (let [m (p/call parser [auto_detect_indent, n] "number")]
      (debug-rule "s_l_block_indented", n, c)
    (p/any parser
     (p/all parser
       [s_indent, m],
      (p/any parser
         [ns_l_compact_sequence, (p/add parser
           n,
          (p/add parser
             1,
            m
          )
        )],
        [ns_l_compact_mapping, (p/add parser
           n,
          (p/add parser
             1,
            m
          )
        )]
      )
    ),
    [s_l_block_node, n, c],
    (p/all parser
       e_node,
      s_l_comments
    )
  )))
    {:trace "s_l_block_indented" :name "s_l_block_indented"}))

;; [186]
;; ns-l-compact-sequence(n) ::=
;;   c-l-block-seq-entry(n)
;;   ( s-indent(n) c-l-block-seq-entry(n) )*
(def ns_l_compact_sequence
  (with-meta
    (fn ns_l_compact_sequence-fn [parser n]
      (debug-rule "ns_l_compact_sequence", n)
    (p/all parser
     [c_l_block_seq_entry, n],
    (p/rep parser
       0,
      nil,
      (p/all parser
         [s_indent, n],
        [c_l_block_seq_entry, n]
      ))
  ))
    {:trace "ns_l_compact_sequence" :name "ns_l_compact_sequence"}))

;; [187]
;; l+block-mapping(n) ::=
;;   ( s-indent(n+m)
;;   ns-l-block-map-entry(n+m) )+
;;   <for_some_fixed_auto-detected_m_>_0>
(def l_block_mapping
  (with-meta
    (fn l_block_mapping-fn [parser n]
  ;; m must be > 0 (JS treats 0 as falsy, Clojure doesn't)
  (let [m (p/call parser [auto_detect_indent, n] "number")]
    (if (and m (pos? m))
    (do
      (debug-rule "l_block_mapping", n)
    (p/all parser
     (p/rep parser
       1,
      nil,
      (p/all parser
         [s_indent, (p/add parser
           n,
          m
        )],
        [ns_l_block_map_entry, (p/add parser
           n,
          m
        )]
      ))
  ))
      false)))
    {:trace "l_block_mapping" :name "l_block_mapping"}))

;; [188]
;; ns-l-block-map-entry(n) ::=
;;   c-l-block-map-explicit-entry(n)
;;   | ns-l-block-map-implicit-entry(n)
(def ns_l_block_map_entry
  (with-meta
    (fn ns_l_block_map_entry-fn [parser n]
      (debug-rule "ns_l_block_map_entry", n)
    (p/any parser
     [c_l_block_map_explicit_entry, n],
    [ns_l_block_map_implicit_entry, n]
  ))
    {:trace "ns_l_block_map_entry" :name "ns_l_block_map_entry"}))

;; [189]
;; c-l-block-map-explicit-entry(n) ::=
;;   c-l-block-map-explicit-key(n)
;;   ( l-block-map-explicit-value(n)
;;   | e-node )
(def c_l_block_map_explicit_entry
  (with-meta
    (fn c_l_block_map_explicit_entry-fn [parser n]
      (debug-rule "c_l_block_map_explicit_entry", n)
    (p/all parser
     [c_l_block_map_explicit_key, n],
    (p/any parser
       [l_block_map_explicit_value, n],
      e_node
    )
  ))
    {:trace "c_l_block_map_explicit_entry" :name "c_l_block_map_explicit_entry"}))

;; [190]
;; c-l-block-map-explicit-key(n) ::=
;;   '?'
;;   s-l+block-indented(n,block-out)
(def c_l_block_map_explicit_key
  (with-meta
    (fn c_l_block_map_explicit_key-fn [parser n]
      (debug-rule "c_l_block_map_explicit_key", n)
    (p/all parser
     (p/chr parser
       "?"
    ),
    (p/chk parser
       "=",
      (p/any parser
         (p/end-of-stream parser),
        s_white,
        b_break
      )
    ),
    [s_l_block_indented, n, "block-out"]
  ))
    {:trace "c_l_block_map_explicit_key" :name "c_l_block_map_explicit_key"}))

;; [191]
;; l-block-map-explicit-value(n) ::=
;;   s-indent(n)
;;   ':' s-l+block-indented(n,block-out)
(def l_block_map_explicit_value
  (with-meta
    (fn l_block_map_explicit_value-fn [parser n]
      (debug-rule "l_block_map_explicit_value", n)
    (p/all parser
     [s_indent, n],
    (p/chr parser
       ":"
    ),
    [s_l_block_indented, n, "block-out"]
  ))
    {:trace "l_block_map_explicit_value" :name "l_block_map_explicit_value"}))

;; [192]
;; ns-l-block-map-implicit-entry(n) ::=
;;   (
;;   ns-s-block-map-implicit-key
;;   | e-node )
;;   c-l-block-map-implicit-value(n)
(def ns_l_block_map_implicit_entry
  (with-meta
    (fn ns_l_block_map_implicit_entry-fn [parser n]
      (debug-rule "ns_l_block_map_implicit_entry", n)
    (p/all parser
     (p/any parser
       ns_s_block_map_implicit_key,
      e_node
    ),
    [c_l_block_map_implicit_value, n]
  ))
    {:trace "ns_l_block_map_implicit_entry" :name "ns_l_block_map_implicit_entry"}))

;; [193]
;; ns-s-block-map-implicit-key ::=
;;   c-s-implicit-json-key(block-key)
;;   | ns-s-implicit-yaml-key(block-key)
(def ns_s_block_map_implicit_key
  (with-meta
    (fn ns_s_block_map_implicit_key-fn [parser]
      (debug-rule "ns_s_block_map_implicit_key")
    (p/any parser
     [c_s_implicit_json_key, "block-key"],
    [ns_s_implicit_yaml_key, "block-key"]
  ))
    {:trace "ns_s_block_map_implicit_key" :name "ns_s_block_map_implicit_key"}))

;; [194]
;; c-l-block-map-implicit-value(n) ::=
;;   ':' (
;;   s-l+block-node(n,block-out)
;;   | ( e-node s-l-comments ) )
(def c_l_block_map_implicit_value
  (with-meta
    (fn c_l_block_map_implicit_value-fn [parser n]
      (debug-rule "c_l_block_map_implicit_value", n)
    (p/all parser
     (p/chr parser
       ":"
    ),
    (p/any parser
       [s_l_block_node, n, "block-out"],
      (p/all parser
         e_node,
        s_l_comments
      )
    )
  ))
    {:trace "c_l_block_map_implicit_value" :name "c_l_block_map_implicit_value"}))

;; [195]
;; ns-l-compact-mapping(n) ::=
;;   ns-l-block-map-entry(n)
;;   ( s-indent(n) ns-l-block-map-entry(n) )*
(def ns_l_compact_mapping
  (with-meta
    (fn ns_l_compact_mapping-fn [parser n]
      (debug-rule "ns_l_compact_mapping", n)
    (p/all parser
     [ns_l_block_map_entry, n],
    (p/rep parser
       0,
      nil,
      (p/all parser
         [s_indent, n],
        [ns_l_block_map_entry, n]
      ))
  ))
    {:trace "ns_l_compact_mapping" :name "ns_l_compact_mapping"}))

;; [196]
;; s-l+block-node(n,c) ::=
;;   s-l+block-in-block(n,c) | s-l+flow-in-block(n)
(def s_l_block_node
  (with-meta
    (fn s_l_block_node-fn [parser n c]
      (debug-rule "s_l_block_node", n, c)
    (p/any parser
     [s_l_block_in_block, n, c],
    [s_l_flow_in_block, n]
  ))
    {:trace "s_l_block_node" :name "s_l_block_node"}))

;; [197]
;; s-l+flow-in-block(n) ::=
;;   s-separate(n+1,flow-out)
;;   ns-flow-node(n+1,flow-out) s-l-comments
(def s_l_flow_in_block
  (with-meta
    (fn s_l_flow_in_block-fn [parser n]
      (debug-rule "s_l_flow_in_block", n)
    (p/all parser
     [s_separate, (p/add parser
       n,
      1
    ), "flow-out"],
    [ns_flow_node, (p/add parser
       n,
      1
    ), "flow-out"],
    s_l_comments
  ))
    {:trace "s_l_flow_in_block" :name "s_l_flow_in_block"}))

;; [198]
;; s-l+block-in-block(n,c) ::=
;;   s-l+block-scalar(n,c) | s-l+block-collection(n,c)
(def s_l_block_in_block
  (with-meta
    (fn s_l_block_in_block-fn [parser n c]
      (debug-rule "s_l_block_in_block", n, c)
    (p/any parser
     [s_l_block_scalar, n, c],
    [s_l_block_collection, n, c]
  ))
    {:trace "s_l_block_in_block" :name "s_l_block_in_block"}))

;; [199]
;; s-l+block-scalar(n,c) ::=
;;   s-separate(n+1,c)
;;   ( c-ns-properties(n+1,c) s-separate(n+1,c) )?
;;   ( c-l+literal(n) | c-l+folded(n) )
(def s_l_block_scalar
  (with-meta
    (fn s_l_block_scalar-fn [parser n c]
      (debug-rule "s_l_block_scalar", n, c)
    (p/all parser
     [s_separate, (p/add parser
       n,
      1
    ), c],
    (p/rep parser
       0,
      1,
      (p/all parser
         [c_ns_properties, (p/add parser
           n,
          1
        ), c],
        [s_separate, (p/add parser
           n,
          1
        ), c]
      )),
    (p/any parser
       [c_l_literal, n],
      [c_l_folded, n]
    )
  ))
    {:trace "s_l_block_scalar" :name "s_l_block_scalar"}))

;; [200]
;; s-l+block-collection(n,c) ::=
;;   ( s-separate(n+1,c)
;;   c-ns-properties(n+1,c) )?
;;   s-l-comments
;;   ( l+block-sequence(seq-spaces(n,c))
;;   | l+block-mapping(n) )
(def s_l_block_collection
  (with-meta
    (fn s_l_block_collection-fn [parser n c]
      (debug-rule "s_l_block_collection", n, c)
    (p/all parser
     (p/rep parser
       0,
      1,
      (p/all parser
         [s_separate, (p/add parser
           n,
          1
        ), c],
        (p/any parser
           (p/all parser
             [c_ns_properties, (p/add parser
               n,
              1
            ), c],
            s_l_comments
          ),
          (p/all parser
             c_ns_tag_property,
            s_l_comments
          ),
          (p/all parser
             c_ns_anchor_property,
            s_l_comments
          )
        )
      )),
    s_l_comments,
    (p/any parser
       [l_block_sequence, [seq_spaces, n, c]],
      [l_block_mapping, n]
    )
  ))
    {:trace "s_l_block_collection" :name "s_l_block_collection"}))

;; [201]
;; seq-spaces(n,c) ::=
;;   ( c = block-out => n-1 )
;;   ( c = block-in => n )
(def seq_spaces
  (with-meta
    (fn seq_spaces-fn [parser n c]
      (debug-rule "seq_spaces", n, c)
    (p/flip parser
     c,
    {
      "block-in" n,
      "block-out" (p/sub parser n, 1),
    }
  ))
    {:trace "seq_spaces" :name "seq_spaces"}))

;; [202]
;; l-document-prefix ::=
;;   c-byte-order-mark? l-comment*
(def l_document_prefix
  (with-meta
    (fn l_document_prefix-fn [parser]
      (debug-rule "l_document_prefix")
    (p/all parser
     (p/rep parser
       0,
      1,
      c_byte_order_mark
    ),
    (p/rep2 parser
       0,
      nil,
      l_comment
    )
  ))
    {:trace "l_document_prefix" :name "l_document_prefix"}))

;; [203]
;; c-directives-end ::=
;;   '-' '-' '-'
(def c_directives_end
  (with-meta
    (fn c_directives_end-fn [parser]
      (debug-rule "c_directives_end")
    (p/all parser
     (p/chr parser
       "-"
    ),
    (p/chr parser
       "-"
    ),
    (p/chr parser
       "-"
    ),
    (p/chk parser
       "=",
      (p/any parser
         (p/end-of-stream parser),
        s_white,
        b_break
      )
    )
  ))
    {:trace "c_directives_end" :name "c_directives_end"}))

;; [204]
;; c-document-end ::=
;;   '.' '.' '.'
(def c_document_end
  (with-meta
    (fn c_document_end-fn [parser]
      (debug-rule "c_document_end")
    (p/all parser
     (p/chr parser
       "."
    ),
    (p/chr parser
       "."
    ),
    (p/chr parser
       "."
    )
  ))
    {:trace "c_document_end" :name "c_document_end"}))

;; [205]
;; l-document-suffix ::=
;;   c-document-end s-l-comments
(def l_document_suffix
  (with-meta
    (fn l_document_suffix-fn [parser]
      (debug-rule "l_document_suffix")
    (p/all parser
     c_document_end,
    s_l_comments
  ))
    {:trace "l_document_suffix" :name "l_document_suffix"}))

;; [206]
;; c-forbidden ::=
;;   <start_of_line>
;;   ( c-directives-end | c-document-end )
;;   ( b-char | s-white | <end_of_file> )
(def c_forbidden
  (with-meta
    (fn c_forbidden-fn [parser]
      (debug-rule "c_forbidden")
    (p/all parser
     (p/start-of-line parser),
    (p/any parser
       c_directives_end,
      c_document_end
    ),
    (p/any parser
       b_char,
      s_white,
      (p/end-of-stream parser)
    )
  ))
    {:trace "c_forbidden" :name "c_forbidden"}))

;; [207]
;; l-bare-document ::=
;;   s-l+block-node(-1,block-in)
;;   <excluding_c-forbidden_content>
(def l_bare_document
  (with-meta
    (fn l_bare_document-fn [parser]
      (debug-rule "l_bare_document")
    (p/all parser
     (p/exclude parser
       c_forbidden
    ),
    [s_l_block_node, -1, "block-in"]
  ))
    {:trace "l_bare_document" :name "l_bare_document"}))

;; [208]
;; l-explicit-document ::=
;;   c-directives-end
;;   ( l-bare-document
;;   | ( e-node s-l-comments ) )
(def l_explicit_document
  (with-meta
    (fn l_explicit_document-fn [parser]
      (debug-rule "l_explicit_document")
    (p/all parser
     c_directives_end,
    (p/any parser
       l_bare_document,
      (p/all parser
         e_node,
        s_l_comments
      )
    )
  ))
    {:trace "l_explicit_document" :name "l_explicit_document"}))

;; [209]
;; l-directive-document ::=
;;   l-directive+
;;   l-explicit-document
(def l_directive_document
  (with-meta
    (fn l_directive_document-fn [parser]
      (debug-rule "l_directive_document")
    (p/all parser
     (p/rep parser
       1,
      nil,
      l_directive
    ),
    l_explicit_document
  ))
    {:trace "l_directive_document" :name "l_directive_document"}))

;; [210]
;; l-any-document ::=
;;   l-directive-document
;;   | l-explicit-document
;;   | l-bare-document
(def l_any_document
  (with-meta
    (fn l_any_document-fn [parser]
      (debug-rule "l_any_document")
    (p/any parser
     l_directive_document,
    l_explicit_document,
    l_bare_document
  ))
    {:trace "l_any_document" :name "l_any_document"}))

;; [211]
;; l-yaml-stream ::=
;;   l-document-prefix* l-any-document?
;;   ( ( l-document-suffix+ l-document-prefix*
;;   l-any-document? )
;;   | ( l-document-prefix* l-explicit-document? ) )*
(def l_yaml_stream
  (with-meta
    (fn l_yaml_stream-fn [parser]
      (debug-rule "l_yaml_stream")
    (p/all parser
     l_document_prefix,
    (p/rep parser
       0,
      1,
      l_any_document
    ),
    (p/rep2 parser
       0,
      nil,
      (p/any parser
         (p/all parser
           l_document_suffix,
          (p/rep parser
             0,
            nil,
            l_document_prefix
          ),
          (p/rep2 parser
             0,
            1,
            l_any_document
          )
        ),
        (p/all parser
           l_document_prefix,
          (p/rep parser
             0,
            1,
            l_explicit_document
          )
        )
      ))
  ))
    {:trace "l_yaml_stream" :name "l_yaml_stream"}))


