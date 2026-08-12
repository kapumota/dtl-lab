### Paper 1 Contribution-Evidence Matrix

#### Purpose

This document defines the scientific claims allowed in the Paper 1 manuscript and maps each claim to reproducible experimental evidence.

The matrix is normative for manuscript construction. Introduction, Results, Discussion, Conclusions, Abstract, and Highlights must not make claims stronger than those defined here.

The scientific evidence corresponds to protocol `paper1-q3-v1`.

#### Global experimental evidence

The definitive experimental campaign contains:

```text
tasks_total:       1272
measured_total:    1160
warmup_total:       112
completed_total:   1188
timeout_total:       84
out_of_memory:        0
tool_errors:           0
```

The 84 timeouts are retained as censored observations and are not replaced by synthetic numerical values.

The analysis uses:

* medians for location
* interquartile ranges for dispersion
* bootstrap 95% confidence intervals for medians
* Wilson 95% intervals for proportions
* Spearman association for ordinal profile growth

Absolute execution-time comparisons between TLC and Alloy are outside the permitted claim space.

### Contribution C1: Bounded verification of cross-shard safety properties

#### Research question

RQ1: Do the valid formal models satisfy the declared properties within the evaluated bounds?

#### Hypothesis

H1.

#### Evidence

Measured runs:

```text
420
```

Completed runs:

```text
350
```

Censored or error runs:

```text
70
```

Property violations among completed runs:

```text
0
```

All Alloy small, medium, and large configurations completed without a counterexample for the seven evaluated properties.

TLC small and medium configurations completed without a counterexample for the seven evaluated properties.

The TLC large configuration produced 70 timeouts and therefore cannot be interpreted as successful property verification for that profile.

#### Primary table

`table-02-properties-results`

#### Supporting table

`table-07-incomplete-runs`

#### Allowed claim

Within the completed bounded configurations, no violation of the seven evaluated properties was observed.

H1 is partially supported under censoring.

#### Strong wording that is allowed

"The completed bounded model-checking runs produced no counterexample to the evaluated properties."

#### Prohibited claims

Do not state that:

* the protocol is proven correct
* all possible executions satisfy the properties
* the TLC large configuration was successfully verified
* absence of a counterexample constitutes an unbounded proof
* the experimental bounds represent production blockchain networks

#### Main limitation

Seventy TLC large-profile measurements reached the timeout threshold and remain censored.

### Contribution C2: Mutation-based validation of the property suite

#### Research question

RQ2: Do scientific mutants expose violations of their intended target properties?

#### Hypothesis

H2.

#### Evidence

Measured runs:

```text
100
```

Scientific mutants:

```text
10
```

Mutants detected:

```text
10
```

Mutants with consistent detection:

```text
10
```

Mutation score:

```text
1.0
```

The experimental suite contains five Alloy mutants and five TLA+ mutants.

Every mutant produced detection in all ten measured repetitions.

#### Primary table

`table-03-mutants-detection`

#### Supporting figure

`figure-07-counterexample-time-by-mutant`

#### Allowed claim

All ten designed scientific mutants were consistently detected through violations of their designated target properties.

#### Strong wording that is allowed

"The property suite detected all ten predefined scientific mutants across all measured repetitions."

#### Prohibited claims

Do not state that:

* the mutation score proves completeness of the specification
* all possible implementation or specification faults can be detected
* mutation adequacy implies protocol correctness
* TLC and Alloy detection times establish tool superiority

#### Main limitation

The mutation score applies only to the predefined scientific mutant catalogue.

### Contribution C3: Bounded implementation-to-model trace conformance

#### Research question

RQ3: Do valid implementation traces conform to the formal model, and are deliberately corrupted traces rejected at their expected diagnostic points?

#### Hypothesis

H3.

#### Evidence

Measured runs:

```text
600
```

Valid cases:

```text
10
```

Negative cases:

```text
10
```

Seeds per case:

```text
30
```

Valid traces:

```text
300 accepted / 300 evaluated
```

Negative traces:

```text
300 rejected / 300 evaluated
```

Expected diagnostic matches for negative cases:

```text
300 / 300
```

For every individual case, the observed classification proportion is:

```text
1.0
```

with a Wilson 95% lower bound of approximately:

```text
0.886487
```

for 30 observations per case.

#### Primary table

`table-04-multiseed-conformance`

#### Primary figure

`figure-08-multiseed-conformance`

#### Allowed claim

Across the evaluated trace catalogue and 30 deterministic seeds per case, all valid traces were accepted and all corrupted traces were rejected at the expected diagnostic points.

#### Strong wording that is allowed

"The evaluated implementation traces exhibited complete bounded classification agreement with the model over the predefined valid and negative trace catalogue."

#### Prohibited claims

Do not state that:

* the Java implementation formally refines the TLA+ specification
* implementation and model are behaviorally equivalent
* all possible Java executions conform to the model
* the experiment constitutes general refinement checking

#### Main limitation

The result establishes bounded trace conformance over the evaluated catalogue rather than general semantic refinement.

### Contribution C4: Characterization of bounded model-checking cost

#### Research question

RQ4: How does model-checking cost change as the evaluated system configurations increase in size?

#### Hypothesis

The original H4 proposed nonlinear growth.

The evidence supports growth but does not establish a nonlinear law.

#### Alloy evidence

Profiles observed:

```text
small
medium
large
```

Median elapsed time:

```text
small:   0.515306 s
medium:  0.766172 s
large:   1.568320 s
```

Large-to-small elapsed-time ratio:

```text
3.043470
```

Spearman association:

```text
1.0
```

Median memory:

```text
small:   179830 KiB
medium:  299268 KiB
large:   317612 KiB
```

Large-to-small memory ratio:

```text
1.766179
```

Spearman association:

```text
1.0
```

#### TLC evidence

Completed profiles:

```text
small
medium
```

Median elapsed time:

```text
small:   0.666802 s
medium:  1.118120 s
```

Median memory:

```text
small:   143660 KiB
medium:  601428 KiB
```

Median distinct states:

```text
small:      14
medium:  24336
```

Large-profile measurements:

```text
70 timeouts
```

No large-profile TLC median is estimated from the censored measurements.

#### Primary table

`table-05-cost-by-bound-profile`

#### Supporting tables

* `table-06-tlc-fault-cost`
* `table-07-incomplete-runs`

#### Primary figures

* `figure-03-distinct-states-vs-transfers`
* `figure-04-time-vs-transfers`
* `figure-05-memory-vs-shards`
* `figure-06-relative-cost-by-profile`

#### Allowed claim

Model-checking cost increases across the evaluated profile sizes.

Alloy provides complete observations for the three predefined profiles and shows monotonic increases in elapsed time and memory.

TLC shows increased cost from small to medium profiles, while the large profile is censored by timeout.

#### Strong wording that is allowed

"The evaluated configurations exhibit increasing bounded model-checking cost as profile size increases."

#### Prohibited claims

Do not state that:

* nonlinear asymptotic growth has been demonstrated
* an asymptotic complexity class has been estimated
* TLC is slower than Alloy
* Alloy is more scalable than TLC
* absolute times from TLC and Alloy are directly comparable
* the three profiles establish a general scalability law

#### Main limitation

Three ordinal profile sizes provide descriptive growth evidence but are insufficient to establish an asymptotic or nonlinear law.

TLC large-profile censoring further restricts inference.

### Contribution C5: Reproducible evidence pipeline

#### Scientific purpose

The experiment connects frozen raw measurements to deterministic analysis outputs and independently verifies that the analysis can be reproduced from the preserved evidence.

#### Definitive reproduction artifact

Protocol:

```text
paper1-q3-v1
```

Reproduced source commit:

```text
6cd88c377afd23fee4998882f91142d71e7d963e
```

Bundle SHA-256:

```text
d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6
```

Independent reproduction:

```text
status: reproducido
```

Reproduction gates:

```text
10 / 10 correct
```

Regenerated artifact comparison:

```text
32 compared
32 matching
0 different
```

Unresolved incidents:

```text
0
```

#### Allowed claim

The deterministic analysis pipeline was independently reproduced from the preserved raw evidence, yielding byte-identical derived datasets, tables, and figures.

#### Prohibited claims

Do not state that:

* all 1272 experimental executions were independently rerun
* timing measurements were independently replicated on another host
* the reproduction establishes hardware-independent performance results
* the reproduction eliminates all threats to reproducibility

#### Main limitation

Independent reproduction regenerates the analytical layer from preserved raw measurements and executes a representative scientific smoke test; it does not repeat the complete performance campaign.

### Claim-Evidence Summary

| ID | Claim                                                                                   | Evidence                                              | Primary artifact     | Claim status                   |
| -- | --------------------------------------------------------------------------------------- | ----------------------------------------------------- | -------------------- | ------------------------------ |
| C1 | No property violation was observed in completed bounded valid-model runs                | RQ1, 350 completed of 420 measured, 0 violations      | Table 2              | Partial support with censoring |
| C2 | All predefined scientific mutants were detected consistently                            | RQ2, 10/10 mutants, mutation score 1.0                | Table 3              | Supported                      |
| C3 | Evaluated valid and corrupted traces were classified correctly                          | RQ3, 600/600 classifications                          | Table 4, Figure 8    | Supported                      |
| C4 | Model-checking cost increases across evaluated configuration sizes                      | RQ4, profile medians, ratios, Spearman, TLC censoring | Table 5, Figures 3-6 | Growth evidence only           |
| C5 | The analysis artifacts can be regenerated deterministically from preserved raw evidence | Independent reproduction, 32/32 SHA-256 matches       | Reproduction report  | Reproduced                     |

### Evidence-to-Manuscript Mapping

| Manuscript section            | Required evidence                                                           |
| ----------------------------- | --------------------------------------------------------------------------- |
| Cross-Shard Transaction Model | properties, state transitions, failure semantics                            |
| Research Methodology          | RQ1-RQ4, hypotheses, mutation strategy, trace conformance                   |
| Experimental Design           | 1272-task matrix, profiles, repetitions, seeds, timeout and resource policy |
| Results RQ1                   | Table 2 and Table 7                                                         |
| Results RQ2                   | Table 3 and Figure 7                                                        |
| Results RQ3                   | Table 4 and Figure 8                                                        |
| Results RQ4                   | Table 5, Table 7, Figures 3-6                                               |
| Discussion                    | relationships among C1-C4 and interpretation boundaries                     |
| Threats to Validity           | Table 8                                                                     |
| Reproducibility and Artifact  | C5 and Phase 8E evidence                                                    |
| Conclusions                   | only claims C1-C5 within their declared boundaries                          |

### Manuscript-wide prohibited overclaims

The manuscript must never claim:

* full formal proof of the cross-shard protocol
* exhaustive verification outside the declared bounds
* general Java-to-TLA+ refinement
* asymptotic complexity from the three bound profiles
* absolute performance superiority between TLC and Alloy
* direct generalization to production blockchain deployments
* independent repetition of the complete 1272-run performance campaign

### Phase 8F-B status

The contribution-evidence contract is frozen for manuscript construction.

Any later manuscript statement that exceeds this matrix requires new scientific evidence rather than rhetorical reinterpretation of the existing results.
