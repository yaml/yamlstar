# YAMLStar Benchmark Report

Run: `20260724T212809Z`
Time: `2026-07-24T21:28:09Z`
Baseline: `graalvm`

## Environment

| Host | OS | Architecture | CPU |
|---|---|---|---|
| blacktop | Linux | x86_64 | 13th Gen Intel(R) Core(TM) i7-1365U |

Warmups: 3; measured repetitions: 15.

## Builds

| Engine | Build time | Library size | Sources |
|---|---:|---:|---|
| graalvm | 92066.416 ms | 34.69 MiB | yaml-parser `v0.2.2-1-g91157fb` |
| gloat-lgvm | 1800.857 ms | 14.71 MiB | gloat `v0.1.62-5-gf25b5a2-dirty`<br>let-go `v1.12.2-20-gf4ef3cb`<br>yaml-parser `v0.2.2-1-g91157fb` |
| gloat-glj | 73840.944 ms | 25.93 MiB | gloat `v0.1.62-5-gf25b5a2-dirty`<br>glojure `v0.7.0-7-g99b2a88-dirty`<br>yaml-parser `v0.2.2-1-g91157fb` |

## Initialization

| Engine | Time |
|---|---:|
| graalvm | 4.157 ms |
| gloat-lgvm | 0.697 ms |
| gloat-glj | 0.853 ms |

## Raw ABI load

| Input/stat | graalvm ms | gloat-lgvm ms | vs graalvm | gloat-glj ms | vs graalvm |
|---|---:|---:|---:|---:|---:|
| scalar/median | 0.244 | 1.688 | 6.92x | 3.577 | 14.65x |
| scalar/p95 | 0.270 | 4.033 | 14.93x | 5.961 | 22.07x |
| mapping/median | 0.507 | 3.096 | 6.11x | 7.664 | 15.12x |
| mapping/p95 | 0.774 | 4.737 | 6.12x | 9.230 | 11.92x |
| nested/median | 1.693 | 8.336 | 4.92x | 19.594 | 11.57x |
| nested/p95 | 2.846 | 9.560 | 3.36x | 26.853 | 9.43x |

## ABI load plus JSON decoding

| Input/stat | graalvm ms | gloat-lgvm ms | vs graalvm | gloat-glj ms | vs graalvm |
|---|---:|---:|---:|---:|---:|
| scalar/median | 0.262 | 1.643 | 6.27x | 3.572 | 13.64x |
| scalar/p95 | 0.402 | 2.686 | 6.69x | 3.950 | 9.84x |
| mapping/median | 0.528 | 3.225 | 6.11x | 8.225 | 15.57x |
| mapping/p95 | 0.833 | 4.399 | 5.28x | 14.355 | 17.23x |
| nested/median | 1.163 | 8.558 | 7.36x | 19.571 | 16.82x |
| nested/p95 | 1.291 | 10.421 | 8.07x | 22.378 | 17.33x |

Ratios are relative to the baseline within this same run. Build time is not runtime execution time.

## Revisions

| Component | Revision | Dirty |
|---|---|---|
| yamlstar | `0.1.17-6-g66a8d7d-dirty` | yes |
| graalvm/yaml-parser | `v0.2.2-1-g91157fb` | no |
| gloat-lgvm/gloat | `v0.1.62-5-gf25b5a2-dirty` | yes |
| gloat-lgvm/let-go | `v1.12.2-20-gf4ef3cb` | no |
| gloat-lgvm/yaml-parser | `v0.2.2-1-g91157fb` | no |
| gloat-glj/gloat | `v0.1.62-5-gf25b5a2-dirty` | yes |
| gloat-glj/glojure | `v0.7.0-7-g99b2a88-dirty` | yes |
| gloat-glj/yaml-parser | `v0.2.2-1-g91157fb` | no |
