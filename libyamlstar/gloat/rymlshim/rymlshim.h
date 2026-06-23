#ifndef YAMLSTAR_RYMLSHIM_H
#define YAMLSTAR_RYMLSHIM_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

enum {
  RYML_KEY  = 1 << 0,
  RYML_VAL  = 1 << 1,
  RYML_BEG  = 1 << 2,
  RYML_END  = 1 << 3,
  RYML_SEQ  = 1 << 4,
  RYML_MAP  = 1 << 5,
  RYML_DOC  = 1 << 6,
  RYML_EXPL = 1 << 7,
  RYML_STRM = 1 << 8,
  RYML_BSEQ = RYML_BEG | RYML_SEQ,
  RYML_ESEQ = RYML_END | RYML_SEQ,
  RYML_BMAP = RYML_BEG | RYML_MAP,
  RYML_EMAP = RYML_END | RYML_MAP,
  RYML_BSTR = RYML_BEG | RYML_STRM,
  RYML_ESTR = RYML_END | RYML_STRM,
  RYML_BDOC = RYML_BEG | RYML_DOC,
  RYML_EDOC = RYML_END | RYML_DOC,
  RYML_SCLR = 1 << 9,
  RYML_ALIA = 1 << 10,
  RYML_ANCH = 1 << 11,
  RYML_TAG  = 1 << 12,
  RYML_YAML = 1 << 13,
  RYML_TAGH = 1 << 14,
  RYML_TAGP = 1 << 15,
  RYML_PLAI = 1 << 16,
  RYML_SQUO = 1 << 17,
  RYML_DQUO = 1 << 18,
  RYML_LITL = 1 << 19,
  RYML_FOLD = 1 << 20,
  RYML_FLOW = 1 << 21,
  RYML_BLCK = 1 << 22,
  RYML_UNFILT = 1 << 23,
  RYML_AREN = 1 << 24,
  RYML_PSTR = 1 << 25,
  RYML_WSTR = RYML_SCLR | RYML_ALIA | RYML_ANCH | RYML_TAG | RYML_TAGH | RYML_TAGP | RYML_YAML
};

typedef struct {
  int ok;
  char *error;
  int32_t *events;
  size_t events_len;
  char *source;
  size_t source_len;
  char *arena;
  size_t arena_len;
} RymlParseResult;

RymlParseResult yamlstar_ryml_parse_events(const char *src, size_t src_len);
void yamlstar_ryml_free_result(RymlParseResult *result);

#ifdef __cplusplus
}
#endif

#endif
