---- MODULE CommitAfterAbort ----
EXTENDS Naturals, FiniteSets, TLC

(***************************************************************************)
(* Modelo multisesion acotado del commit cross-shard de DLT-Lab.           *)
(* Cada transferencia conserva estado, recibos, credito, liberacion y      *)
(* votos independientes. La red se representa mediante mensajes acotados.  *)
(***************************************************************************)

CONSTANTS Shards,
          Transfers,
          Validators,
          Quorum,
          ReceiptCopies,
          DelayedCopies,
          EnableTimeout

MutationMode == "CommitAfterAbort"

StatusValues == {"Pending", "Locked", "Prepared", "Committed", "Aborted"}
ShardOrNone == Shards \cup {"None"}

VARIABLES status,
          sourceShard,
          targetShard,
          locked,
          receiptOwner,
          receiptUseCount,
          destinationCredit,
          fundsReleased,
          messages,
          votes

vars == << status,
          sourceShard,
          targetShard,
          locked,
          receiptOwner,
          receiptUseCount,
          destinationCredit,
          fundsReleased,
          messages,
          votes >>

ReceiptMessage(t, copy, delayed) ==
    [kind |-> "Receipt", transfer |-> t, copy |-> copy, delayed |-> delayed]

AllReceiptMessages ==
    { ReceiptMessage(t, copy, delayed) :
        t \in Transfers,
        copy \in 1..ReceiptCopies,
        delayed \in BOOLEAN }

MessagesFor(t) ==
    { message \in messages : message.transfer = t }

Init ==
    /\ status = [t \in Transfers |-> "Pending"]
    /\ sourceShard \in [Transfers -> Shards]
    /\ targetShard \in [Transfers -> Shards]
    /\ \A t \in Transfers : sourceShard[t] # targetShard[t]
    /\ locked = [t \in Transfers |-> FALSE]
    /\ receiptOwner = [t \in Transfers |-> "None"]
    /\ receiptUseCount = [t \in Transfers |-> 0]
    /\ destinationCredit = [t \in Transfers |-> FALSE]
    /\ fundsReleased = [t \in Transfers |-> FALSE]
    /\ messages = {}
    /\ votes = [t \in Transfers |-> {}]

LockTransfer(t) ==
    /\ status[t] = "Pending"
    /\ sourceShard[t] # targetShard[t]
    /\ status' = [status EXCEPT ![t] = "Locked"]
    /\ locked' = [locked EXCEPT ![t] = TRUE]
    /\ messages' = messages \cup
          { ReceiptMessage(t, copy, copy \in DelayedCopies) :
              copy \in 1..ReceiptCopies }
    /\ UNCHANGED << sourceShard,
                    targetShard,
                    receiptOwner,
                    receiptUseCount,
                    destinationCredit,
                    fundsReleased,
                    votes >>

ReleaseDelayedReceipt(t, copy) ==
    LET delayedMessage == ReceiptMessage(t, copy, TRUE)
        readyMessage == ReceiptMessage(t, copy, FALSE)
    IN
    /\ delayedMessage \in messages
    /\ messages' = (messages \ {delayedMessage}) \cup {readyMessage}
    /\ UNCHANGED << status,
                    sourceShard,
                    targetShard,
                    locked,
                    receiptOwner,
                    receiptUseCount,
                    destinationCredit,
                    fundsReleased,
                    votes >>

CanConsumeReceipt(t) ==
    IF MutationMode = "NoReplayProtection"
    THEN /\ status[t] \in {"Locked", "Prepared", "Committed"}
         /\ receiptUseCount[t] < ReceiptCopies
    ELSE /\ status[t] = "Locked"
         /\ receiptUseCount[t] = 0
         /\ ~destinationCredit[t]

ConsumeReceipt(t, copy) ==
    LET readyMessage == ReceiptMessage(t, copy, FALSE)
    IN
    /\ readyMessage \in messages
    /\ locked[t]
    /\ CanConsumeReceipt(t)
    /\ receiptOwner' = [receiptOwner EXCEPT ![t] = targetShard[t]]
    /\ receiptUseCount' = [receiptUseCount EXCEPT ![t] = @ + 1]
    /\ destinationCredit' = [destinationCredit EXCEPT ![t] = TRUE]
    /\ status' = [status EXCEPT
          ![t] = IF @ = "Committed" THEN "Committed" ELSE "Prepared"]
    /\ messages' = messages \ {readyMessage}
    /\ UNCHANGED << sourceShard,
                    targetShard,
                    locked,
                    fundsReleased,
                    votes >>

CreditBeforeReceipt(t) ==
    /\ MutationMode = "CreditBeforeReceipt"
    /\ status[t] = "Locked"
    /\ receiptUseCount[t] = 0
    /\ destinationCredit' = [destinationCredit EXCEPT ![t] = TRUE]
    /\ status' = [status EXCEPT ![t] = "Prepared"]
    /\ UNCHANGED << sourceShard,
                    targetShard,
                    locked,
                    receiptOwner,
                    receiptUseCount,
                    fundsReleased,
                    messages,
                    votes >>

CastVote(t, validator) ==
    /\ status[t] = "Prepared"
    /\ validator \notin votes[t]
    /\ votes' = [votes EXCEPT ![t] = @ \cup {validator}]
    /\ UNCHANGED << status,
                    sourceShard,
                    targetShard,
                    locked,
                    receiptOwner,
                    receiptUseCount,
                    destinationCredit,
                    fundsReleased,
                    messages >>

CanCommit(t) ==
    /\ status[t] = "Prepared"
    /\ destinationCredit[t]
    /\ IF MutationMode = "QuorumBypass"
        THEN Cardinality(votes[t]) < Quorum
        ELSE Cardinality(votes[t]) >= Quorum

CommitTransfer(t) ==
    /\ CanCommit(t)
    /\ status' = [status EXCEPT ![t] = "Committed"]
    /\ UNCHANGED << sourceShard,
                    targetShard,
                    locked,
                    receiptOwner,
                    receiptUseCount,
                    destinationCredit,
                    fundsReleased,
                    messages,
                    votes >>

TimeoutTransfer(t) ==
    /\ EnableTimeout
    /\ status[t] = "Locked"
    /\ ~destinationCredit[t]
    /\ status' = [status EXCEPT ![t] = "Aborted"]
    /\ locked' = [locked EXCEPT ![t] = FALSE]
    /\ fundsReleased' =
          IF MutationMode = "TimeoutWithoutRelease"
          THEN fundsReleased
          ELSE [fundsReleased EXCEPT ![t] = TRUE]
    /\ messages' = messages \ MessagesFor(t)
    /\ UNCHANGED << sourceShard,
                    targetShard,
                    receiptOwner,
                    receiptUseCount,
                    destinationCredit,
                    votes >>

CommitAfterAbort(t) ==
    /\ MutationMode = "CommitAfterAbort"
    /\ status[t] = "Aborted"
    /\ status' = [status EXCEPT ![t] = "Committed"]
    /\ UNCHANGED << sourceShard,
                    targetShard,
                    locked,
                    receiptOwner,
                    receiptUseCount,
                    destinationCredit,
                    fundsReleased,
                    messages,
                    votes >>

Stutter ==
    UNCHANGED vars

Next ==
    \/ \E t \in Transfers : LockTransfer(t)
    \/ \E t \in Transfers, copy \in 1..ReceiptCopies :
         ReleaseDelayedReceipt(t, copy)
    \/ \E t \in Transfers, copy \in 1..ReceiptCopies :
         ConsumeReceipt(t, copy)
    \/ \E t \in Transfers : CreditBeforeReceipt(t)
    \/ \E t \in Transfers, validator \in Validators :
         CastVote(t, validator)
    \/ \E t \in Transfers : CommitTransfer(t)
    \/ \E t \in Transfers : TimeoutTransfer(t)
    \/ \E t \in Transfers : CommitAfterAbort(t)
    \/ Stutter

Spec ==
    Init /\ [][Next]_vars

TypeOK ==
    /\ status \in [Transfers -> StatusValues]
    /\ sourceShard \in [Transfers -> Shards]
    /\ targetShard \in [Transfers -> Shards]
    /\ locked \in [Transfers -> BOOLEAN]
    /\ receiptOwner \in [Transfers -> ShardOrNone]
    /\ receiptUseCount \in [Transfers -> Nat]
    /\ destinationCredit \in [Transfers -> BOOLEAN]
    /\ fundsReleased \in [Transfers -> BOOLEAN]
    /\ messages \subseteq AllReceiptMessages
    /\ votes \in [Transfers -> SUBSET Validators]
    /\ Quorum >= 1
    /\ Quorum <= Cardinality(Validators)
    /\ ReceiptCopies >= 1
    /\ DelayedCopies \subseteq 1..ReceiptCopies

NoReceiptReplay ==
    \A t \in Transfers : receiptUseCount[t] <= 1

DestinationCreditRequiresValidReceipt ==
    \A t \in Transfers :
        destinationCredit[t] =>
            /\ locked[t]
            /\ receiptOwner[t] = targetShard[t]
            /\ receiptUseCount[t] = 1

DecisionConsistency ==
    \A t \in Transfers :
        status[t] = "Committed" => ~fundsReleased[t]

EventuallyReleasedAfterTimeout ==
    \A t \in Transfers :
        status[t] = "Aborted" => fundsReleased[t]

QuorumRequired ==
    \A t \in Transfers :
        status[t] = "Committed" => Cardinality(votes[t]) >= Quorum

NoValueLoss ==
    \A t \in Transfers :
        \/ status[t] = "Pending"
        \/ status[t] = "Locked"
        \/ status[t] = "Prepared"
        \/ /\ status[t] = "Committed"
             /\ destinationCredit[t]
        \/ /\ status[t] = "Aborted"
             /\ fundsReleased[t]

NoDoubleMint ==
    NoReceiptReplay /\ DestinationCreditRequiresValidReceipt

AtomicCommit ==
    DecisionConsistency

TimeoutReleasesFunds ==
    EventuallyReleasedAfterTimeout

====
