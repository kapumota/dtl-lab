### Paper 1 manuscript plan

#### Target venue

Primary target:

Simulation Modelling Practice and Theory

Publisher:

Elsevier

Submission strategy:

Traditional subscription publication. Open access is not required.

#### Scope alignment

The manuscript is positioned as a reproducible modelling, formal verification, validation, and experimental evaluation study of cross-shard transaction handling in distributed ledgers.

The paper emphasizes:

* formal modelling
* model checking
* validation and verification
* experimental design
* distributed systems
* blockchain transaction handling
* reproducibility

#### Scientific baseline

Protocol:

`paper1-q3-v1`

Definitive experimental matrix:

1272 tasks

Formal tools:

* TLC 1.7.4
* Alloy 6.2.0

Implementation:

Java 17

Analysis:

Python 3.12

Independent reproduction:

completed

Final reproduction bundle SHA-256:

`d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6`

Reproduced source commit:

`6cd88c377afd23fee4998882f91142d71e7d963e`

Independent reproduction result:

* all reproduction gates passed
* 32 of 32 regenerated artifacts matched by SHA-256
* zero differences
* zero unresolved incidents

#### Research questions

RQ1 examines whether the valid formal models satisfy the declared properties within the evaluated bounds.

RQ2 examines whether the scientific mutants expose violations of their target properties.

RQ3 examines bounded trace conformance between the Java implementation and the formal model using valid and corrupted traces.

RQ4 examines how model-checking cost changes as the evaluated system configurations increase in size.

#### Claims boundary

The manuscript must not claim:

* exhaustive correctness beyond the declared bounds
* general refinement between the Java implementation and the formal models
* absolute performance superiority of TLC over Alloy or vice versa
* a demonstrated asymptotic complexity law from the three evaluated size levels
* generalization of the experimental results to production blockchain networks
* complete repetition of all 1272 performance measurements during independent reproduction

#### Main scientific contribution

The paper presents a reproducible workflow that connects executable cross-shard transaction logic, two formal modelling approaches, mutation-based property validation, implementation-to-model trace conformance, bounded model-checking cost analysis, and independently reproduced analytical artifacts.

#### Manuscript strategy

The manuscript will be written from evidence to claims.

Writing order:

1. methodology and experimental design
2. results
3. discussion and threats to validity
4. reproducibility and artifact
5. related work
6. introduction
7. abstract, title, keywords, and highlights

#### Target manuscript structure

1. Introduction
2. Background and Related Work
3. Cross-Shard Transaction Model
4. Research Methodology
5. Experimental Design
6. Results
7. Discussion
8. Threats to Validity
9. Reproducibility and Artifact
10. Conclusions

#### Journal constraints

Abstract:

maximum 250 words

Keywords:

1 to 7

Highlights:

3 to 5 bullets

Maximum highlight length:

85 characters including spaces

Section numbering:

required

Reference style at initial submission:

consistent numbered references

#### Status

Phase 8F-A initialized.

The scientific results from Phases 8A through 8E are treated as frozen inputs to manuscript construction.
