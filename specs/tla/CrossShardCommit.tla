---- MODULE CrossShardCommit ----
EXTENDS Naturals, TLC

(***************************************************************************)
(* Especificacion formal acotada del commit cross-shard de DLT-Lab.        *)
(* El modelo separa el debito en el shard origen, el recibo cross-shard,    *)
(* el credito en el shard destino y la liberacion por timeout.             *)
(***************************************************************************)

CONSTANT Amount

VARIABLES originDebited,
          receiptCreated,
          receiptUseCount,
          destinationCredited,
          fundsReleased,
          committed,
          aborted,
          expired

vars == << originDebited,
          receiptCreated,
          receiptUseCount,
          destinationCredited,
          fundsReleased,
          committed,
          aborted,
          expired >>

Init ==
    /\ originDebited = FALSE
    /\ receiptCreated = FALSE
    /\ receiptUseCount = 0
    /\ destinationCredited = FALSE
    /\ fundsReleased = FALSE
    /\ committed = FALSE
    /\ aborted = FALSE
    /\ expired = FALSE

LockOrigin ==
    /\ ~originDebited
    /\ ~committed
    /\ ~aborted
    /\ originDebited' = TRUE
    /\ receiptCreated' = TRUE
    /\ UNCHANGED << receiptUseCount,
                    destinationCredited,
                    fundsReleased,
                    committed,
                    aborted,
                    expired >>

CommitDestination ==
    /\ originDebited
    /\ receiptCreated
    /\ receiptUseCount = 0
    /\ ~destinationCredited
    /\ ~fundsReleased
    /\ ~aborted
    /\ destinationCredited' = TRUE
    /\ receiptUseCount' = 1
    /\ committed' = TRUE
    /\ UNCHANGED << originDebited,
                    receiptCreated,
                    fundsReleased,
                    aborted,
                    expired >>

TimeoutOrigin ==
    /\ originDebited
    /\ ~destinationCredited
    /\ ~committed
    /\ ~aborted
    /\ fundsReleased' = TRUE
    /\ expired' = TRUE
    /\ aborted' = TRUE
    /\ UNCHANGED << originDebited,
                    receiptCreated,
                    receiptUseCount,
                    destinationCredited,
                    committed >>

Stutter ==
    UNCHANGED vars

Next ==
    \/ LockOrigin
    \/ CommitDestination
    \/ TimeoutOrigin
    \/ Stutter

Spec ==
    Init /\ [][Next]_vars

NoDoubleMint ==
    destinationCredited => (receiptCreated /\ receiptUseCount = 1)

NoValueLoss ==
    (committed \/ aborted) => (originDebited => (destinationCredited \/ fundsReleased))

NoReceiptReplay ==
    receiptUseCount \in 0..1

AtomicCommit ==
    ~(committed /\ aborted)

TimeoutReleasesFunds ==
    expired => fundsReleased

====
