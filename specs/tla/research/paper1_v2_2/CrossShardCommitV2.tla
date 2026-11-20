---- MODULE CrossShardCommitV2 ----
EXTENDS Integers, FiniteSets, TLC

(***************************************************************************)
(* DLT-Lab Paper 1, modelo V2.2 con decision por shard.                       *)
(* Diferencias frente a V1                                                  *)
(*  1. Cada transferencia tiene estado local en el shard origen y en el     *)
(*     shard destino, mas una decision de coordinador. La atomicidad        *)
(*     entre shards es expresable y no se sigue de una sola guarda.         *)
(*  2. Las transferencias compiten por UTXO (InputOf puede coincidir).      *)
(*  3. Hay saldos numericos por shard y una propiedad de conservacion.      *)
(*  4. Hay propiedades temporales con equidad debil explicita.              *)
(***************************************************************************)

CONSTANTS Transfers, UTXOs, Shards, Validators,
          InputOf, SrcOf, DstOf, Amount,
          Quorum, ReceiptCopies, Mutation

ASSUME Mutation \in {"Correct", "LocalTimeoutAbort", "QuorumBypass",
                     "ReceiptReplay", "LockWithoutCheck", "DecisionFlip"}

\* Restricciones explicitas sobre los parametros (v2.2)
ASSUME /\ Transfers # {} /\ UTXOs # {} /\ Shards # {} /\ Validators # {}
       /\ DOMAIN InputOf = Transfers /\ \A t \in Transfers : InputOf[t] \in UTXOs
       /\ DOMAIN SrcOf = Transfers /\ \A t \in Transfers : SrcOf[t] \in Shards
       /\ DOMAIN DstOf = Transfers /\ \A t \in Transfers : DstOf[t] \in Shards
       /\ DOMAIN Amount = Transfers /\ \A t \in Transfers : Amount[t] \in Nat /\ Amount[t] > 0
       /\ Quorum \in Nat /\ Quorum >= 1 /\ Quorum <= Cardinality(Validators)
       /\ ReceiptCopies \in Nat /\ ReceiptCopies >= 1
       /\ Cardinality(Shards) >= 2
       /\ \A t \in Transfers : SrcOf[t] # DstOf[t]

VARIABLES srcState,    \* Idle, Locked, Debited, Released
          dstState,    \* None, Prepared, Credited, Rejected
          decision,    \* None, Commit, Abort
          lockOwner,   \* UTXO -> Transfers u {None}
          spent,       \* UTXO -> BOOLEAN
          uses,        \* consumo de recibo por transferencia
          credited,    \* acreditaciones aplicadas por transferencia
          msgs,        \* recibos en vuelo (t, copia)
          votes,
          bal          \* delta de saldo por shard

vars == << srcState, dstState, decision, lockOwner, spent,
           uses, credited, msgs, votes, bal >>

RECURSIVE SumSet(_, _)
SumSet(f, S) ==
    IF S = {} THEN 0
    ELSE LET x == CHOOSE y \in S : TRUE IN f[x] + SumSet(f, S \ {x})

Init ==
    /\ srcState = [t \in Transfers |-> "Idle"]
    /\ dstState = [t \in Transfers |-> "None"]
    /\ decision = [t \in Transfers |-> "None"]
    /\ lockOwner = [u \in UTXOs |-> "None"]
    /\ spent = [u \in UTXOs |-> FALSE]
    /\ uses = [t \in Transfers |-> 0]
    /\ credited = [t \in Transfers |-> 0]
    /\ msgs = {}
    /\ votes = [t \in Transfers |-> {}]
    /\ bal = [s \in Shards |-> 0]

Lock(t) ==
    LET u == InputOf[t] IN
    /\ srcState[t] = "Idle"
    /\ ~spent[u]
    /\ (lockOwner[u] = "None" \/ Mutation = "LockWithoutCheck")
    /\ srcState' = [srcState EXCEPT ![t] = "Locked"]
    /\ lockOwner' = [lockOwner EXCEPT ![u] = t]
    /\ msgs' = msgs \cup { <<t, c>> : c \in 1..ReceiptCopies }
    /\ UNCHANGED << dstState, decision, spent, uses, credited, votes, bal >>

Prepare(t, c) ==
    /\ <<t, c>> \in msgs
    /\ (decision[t] = "None" \/ Mutation = "ReceiptReplay")
    /\ IF Mutation = "ReceiptReplay"
         THEN /\ dstState[t] \in {"None", "Credited"}
              /\ uses[t] < ReceiptCopies
         ELSE /\ dstState[t] = "None"
              /\ uses[t] = 0
    /\ dstState' = [dstState EXCEPT ![t] = "Prepared"]
    /\ uses' = [uses EXCEPT ![t] = @ + 1]
    /\ msgs' = msgs \ {<<t, c>>}
    /\ UNCHANGED << srcState, decision, lockOwner, spent, credited, votes, bal >>

Vote(t, v) ==
    /\ dstState[t] = "Prepared"
    /\ decision[t] = "None"
    /\ v \notin votes[t]
    /\ votes' = [votes EXCEPT ![t] = @ \cup {v}]
    /\ UNCHANGED << srcState, dstState, decision, lockOwner, spent,
                    uses, credited, msgs, bal >>

DecideCommit(t) ==
    /\ decision[t] = "None"
    /\ dstState[t] = "Prepared"
    /\ (Cardinality(votes[t]) >= Quorum \/ Mutation = "QuorumBypass")
    /\ decision' = [decision EXCEPT ![t] = "Commit"]
    /\ UNCHANGED << srcState, dstState, lockOwner, spent,
                    uses, credited, msgs, votes, bal >>

DecideAbort(t) ==
    /\ srcState[t] = "Locked"
    /\ (decision[t] = "None" \/ (Mutation = "DecisionFlip" /\ decision[t] = "Commit"))
    /\ decision' = [decision EXCEPT ![t] = "Abort"]
    /\ UNCHANGED << srcState, dstState, lockOwner, spent,
                    uses, credited, msgs, votes, bal >>

ApplySrc(t) ==
    LET u == InputOf[t] IN
    /\ srcState[t] = "Locked"
    /\ decision[t] # "None"
    /\ IF decision[t] = "Commit"
         THEN /\ srcState' = [srcState EXCEPT ![t] = "Debited"]
              /\ spent' = [spent EXCEPT ![u] = TRUE]
              /\ bal' = [bal EXCEPT ![SrcOf[t]] = @ - Amount[t]]
         ELSE /\ srcState' = [srcState EXCEPT ![t] = "Released"]
              /\ UNCHANGED << spent, bal >>
    /\ lockOwner' = [lockOwner EXCEPT ![u] = IF @ = t THEN "None" ELSE @]
    /\ UNCHANGED << dstState, decision, uses, credited, msgs, votes >>

LocalTimeoutRelease(t) ==
    LET u == InputOf[t] IN
    /\ Mutation = "LocalTimeoutAbort"
    /\ srcState[t] = "Locked"
    /\ decision[t] = "None"
    /\ dstState[t] = "Prepared"
    /\ srcState' = [srcState EXCEPT ![t] = "Released"]
    /\ lockOwner' = [lockOwner EXCEPT ![u] = IF @ = t THEN "None" ELSE @]
    /\ UNCHANGED << dstState, decision, spent, uses, credited, msgs, votes, bal >>

ApplyDst(t) ==
    /\ decision[t] # "None"
    /\ \/ /\ decision[t] = "Commit"
          /\ dstState[t] \in {"Prepared"}
          /\ dstState' = [dstState EXCEPT ![t] = "Credited"]
          /\ credited' = [credited EXCEPT ![t] = @ + 1]
          /\ bal' = [bal EXCEPT ![DstOf[t]] = @ + Amount[t]]
       \/ /\ decision[t] = "Abort"
          /\ dstState[t] \in {"None", "Prepared"}
          /\ dstState' = [dstState EXCEPT ![t] = "Rejected"]
          /\ UNCHANGED << credited, bal >>
    /\ UNCHANGED << srcState, decision, lockOwner, spent, uses, msgs, votes >>

Next ==
    \/ \E t \in Transfers : Lock(t) \/ DecideCommit(t) \/ DecideAbort(t)
                            \/ ApplySrc(t) \/ ApplyDst(t) \/ LocalTimeoutRelease(t)
    \/ \E t \in Transfers, c \in 1..ReceiptCopies : Prepare(t, c)
    \/ \E t \in Transfers, v \in Validators : Vote(t, v)

Spec == Init /\ [][Next]_vars

Fairness ==
    \A t \in Transfers :
        /\ WF_vars(DecideAbort(t))
        /\ WF_vars(ApplySrc(t))
        /\ WF_vars(ApplyDst(t))

LiveSpec == Spec /\ Fairness

SrcTerminal(t) == srcState[t] \in {"Debited", "Released"}
DstTerminal(t) == dstState[t] \in {"Credited", "Rejected"}

TypeOK ==
    /\ srcState \in [Transfers -> {"Idle", "Locked", "Debited", "Released"}]
    /\ dstState \in [Transfers -> {"None", "Prepared", "Credited", "Rejected"}]
    /\ decision \in [Transfers -> {"None", "Commit", "Abort"}]
    /\ lockOwner \in [UTXOs -> Transfers \cup {"None"}]
    /\ spent \in [UTXOs -> BOOLEAN]
    /\ uses \in [Transfers -> 0..ReceiptCopies]
    /\ credited \in [Transfers -> Nat]
    /\ msgs \subseteq (Transfers \X (1..ReceiptCopies))
    /\ votes \in [Transfers -> SUBSET Validators]
    /\ bal \in [Shards -> Int]

\* A1. Atomicidad entre shards (no se sigue de una sola guarda)
Atomicity ==
    \A t \in Transfers :
        /\ ~(srcState[t] = "Debited" /\ dstState[t] = "Rejected")
        /\ ~(srcState[t] = "Released" /\ dstState[t] = "Credited")

\* A2. Sin doble gasto del mismo UTXO entre transferencias
NoDoubleSpend ==
    \A u \in UTXOs :
        Cardinality({ t \in Transfers : InputOf[t] = u /\ srcState[t] = "Debited" }) <= 1

\* A3. Sin doble acreditacion ni replay de recibo
NoDoubleCredit == \A t \in Transfers : credited[t] <= 1
NoReceiptReplay == \A t \in Transfers : uses[t] <= 1

\* A4. Quorum
QuorumRequired ==
    \A t \in Transfers :
        decision[t] = "Commit" => Cardinality(votes[t]) >= Quorum

\* A5. Conservacion aritmetica al terminar todas las transferencias iniciadas
Conservation ==
    (\A t \in Transfers : srcState[t] # "Idle" => (SrcTerminal(t) /\ DstTerminal(t)))
      => SumSet(bal, Shards) = 0

\* A6. Estabilidad de la decision y de los estados terminales (safety de accion)
DecisionStable ==
    [][\A t \in Transfers : decision[t] # "None" => decision'[t] = decision[t]]_vars
TerminalStable ==
    [][\A t \in Transfers :
         /\ (SrcTerminal(t) => srcState'[t] = srcState[t])
         /\ (DstTerminal(t) => dstState'[t] = dstState[t])]_vars

\* V2.2: coherencia local observada; aun sin prueba Java--V2.
LockOwnerCoherence ==
    \A t \in Transfers :
        srcState[t] = "Locked" => lockOwner[InputOf[t]] = t

DecisionBeforeEffect ==
    \A t \in Transfers :
        /\ (srcState[t] = "Debited" => decision[t] = "Commit")
        /\ (srcState[t] = "Released" => decision[t] = "Abort")
        /\ (dstState[t] = "Credited" => decision[t] = "Commit")
        /\ (dstState[t] = "Rejected" => decision[t] = "Abort")

CreditCounterCoherence ==
    \A t \in Transfers :
        (dstState[t] = "Credited") <=> (credited[t] = 1)

SpentWitness ==
    \A u \in UTXOs :
        spent[u] => \E t \in Transfers :
            InputOf[t] = u /\ srcState[t] = "Debited"

\* L1. Terminacion bajo equidad debil (solo con LiveSpec)
Termination ==
    \A t \in Transfers : (srcState[t] = "Locked") ~> (SrcTerminal(t) /\ DstTerminal(t))
\* L2. Sin bloqueo permanente de UTXO
NoPermanentLock ==
    \A u \in UTXOs : (lockOwner[u] # "None") ~> (lockOwner[u] = "None")

\* Testigo de aplicacion parcial observable. Se espera un contraejemplo:
\* no es una violacion de Atomicity en esta especificacion.
ReachPartialApplication ==
    ~(\E t \in Transfers :
         decision[t] = "Commit" /\
            ((srcState[t] = "Debited" /\ dstState[t] # "Credited") \/
             (dstState[t] = "Credited" /\ srcState[t] # "Debited")))

\* No vacuidad (se espera contraejemplo de cada uno)
ReachCommit == ~(\E t \in Transfers : srcState[t] = "Debited" /\ dstState[t] = "Credited")
ReachAbort  == ~(\E t \in Transfers : srcState[t] = "Released" /\ dstState[t] = "Rejected")
ReachContention == ~(\E t1, t2 \in Transfers : t1 # t2 /\ InputOf[t1] = InputOf[t2]
                      /\ srcState[t1] = "Released" /\ srcState[t2] = "Locked")
====
