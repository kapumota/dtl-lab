module mutants/CreditBeforeReceipt

// Modelo multisesion acotado del commit cross-shard de DLT-Lab.
// Los estados ordenados representan una traza finita del protocolo.

open util/ordering[State]

abstract sig Status {}
one sig Pending, Locked, Prepared, Committed, Aborted extends Status {}

abstract sig UseCount {}
one sig ZeroUse, OneUse, ManyUses extends UseCount {}

abstract sig MessageKind {}
one sig ReceiptMessage, VoteMessage, TimeoutMessage extends MessageKind {}

sig Shard {}
sig Validator {}

sig Transfer {
  source: one Shard,
  target: one Shard
}

sig Receipt {
  owner: one Transfer
}

sig Message {
  kind: one MessageKind,
  transfer: one Transfer,
  receipt: lone Receipt,
  validator: lone Validator
}

sig State {
  status: Transfer -> one Status,
  locked: set Transfer,
  receiptUseCount: Receipt -> one UseCount,
  destinationCredit: set Transfer,
  fundsReleased: set Transfer,
  messages: set Message,
  votes: Transfer -> Validator
}

fact StaticTopology {
  all t: Transfer | t.source != t.target

  all t: Transfer | #t.~owner >= 2

  all r: Receipt |
    # {m: Message |
      m.kind = ReceiptMessage and
      m.receipt = r and
      m.transfer = r.owner
    } >= 2

  all t: Transfer, v: Validator |
    one m: Message |
      m.kind = VoteMessage and
      m.transfer = t and
      m.validator = v and
      no m.receipt

  all t: Transfer |
    one m: Message |
      m.kind = TimeoutMessage and
      m.transfer = t and
      no m.receipt and
      no m.validator

  all m: Message |
    (m.kind = ReceiptMessage implies one m.receipt and no m.validator) and
    (m.kind = VoteMessage implies no m.receipt and one m.validator) and
    (m.kind = TimeoutMessage implies no m.receipt and no m.validator)
}

pred init[s: State] {
  all t: Transfer | s.status[t] = Pending
  no s.locked
  all r: Receipt | s.receiptUseCount[r] = ZeroUse
  no s.destinationCredit
  no s.fundsReleased
  no s.messages
  no s.votes
}

pred lockTransfer[s, nextState: State, t: Transfer] {
  s.status[t] = Pending
  nextState.status = s.status ++ t->Locked
  nextState.locked = s.locked + t
  nextState.messages = s.messages +
    {m: Message | m.kind = ReceiptMessage and m.transfer = t}
  nextState.receiptUseCount = s.receiptUseCount
  nextState.destinationCredit = s.destinationCredit
  nextState.fundsReleased = s.fundsReleased
  nextState.votes = s.votes
}

pred consumeReceipt[s, nextState: State, t: Transfer, r: Receipt] {
  some message: s.messages | {
    message.kind = ReceiptMessage
    message.receipt = r
    s.status[t] = Locked
    t in s.locked
    r.owner = t
    s.receiptUseCount[r] = ZeroUse

    nextState.status = s.status ++ t->Prepared
    nextState.locked = s.locked
    nextState.receiptUseCount = s.receiptUseCount ++ r->OneUse
    nextState.destinationCredit = s.destinationCredit + t
    nextState.fundsReleased = s.fundsReleased
    nextState.messages = s.messages - message
    nextState.votes = s.votes
  }
}

pred castVote[s, nextState: State, t: Transfer, v: Validator] {
  s.status[t] = Prepared
  v not in s.votes[t]

  nextState.status = s.status
  nextState.locked = s.locked
  nextState.receiptUseCount = s.receiptUseCount
  nextState.destinationCredit = s.destinationCredit
  nextState.fundsReleased = s.fundsReleased
  nextState.messages = s.messages
  nextState.votes = s.votes + t->v
}

pred commitTransfer[s, nextState: State, t: Transfer] {
  s.status[t] = Prepared
  t in s.destinationCredit
  #s.votes[t] >= 2

  nextState.status = s.status ++ t->Committed
  nextState.locked = s.locked
  nextState.receiptUseCount = s.receiptUseCount
  nextState.destinationCredit = s.destinationCredit
  nextState.fundsReleased = s.fundsReleased
  nextState.messages = s.messages
  nextState.votes = s.votes
}

pred timeoutTransfer[s, nextState: State, t: Transfer] {
  s.status[t] = Locked
  t not in s.destinationCredit

  nextState.status = s.status ++ t->Aborted
  nextState.locked = s.locked - t
  nextState.receiptUseCount = s.receiptUseCount
  nextState.destinationCredit = s.destinationCredit
  nextState.fundsReleased = s.fundsReleased + t
  nextState.messages = s.messages - {m: Message | m.transfer = t}
  nextState.votes = s.votes
}

pred stutter[s, nextState: State] {
  nextState.status = s.status
  nextState.locked = s.locked
  nextState.receiptUseCount = s.receiptUseCount
  nextState.destinationCredit = s.destinationCredit
  nextState.fundsReleased = s.fundsReleased
  nextState.messages = s.messages
  nextState.votes = s.votes
}

pred creditBeforeReceipt[s, nextState: State, t: Transfer] {
  s.status[t] = Locked
  all r: t.~owner | s.receiptUseCount[r] = ZeroUse

  nextState.status = s.status ++ t->Prepared
  nextState.locked = s.locked
  nextState.receiptUseCount = s.receiptUseCount
  nextState.destinationCredit = s.destinationCredit + t
  nextState.fundsReleased = s.fundsReleased
  nextState.messages = s.messages
  nextState.votes = s.votes
}

fact Trace {
  init[first]
  all s: State - last |
    let nextState = s.next |
      some t: Transfer, r: Receipt, v: Validator |
      lockTransfer[s, nextState, t]
      or consumeReceipt[s, nextState, t, r]
      or creditBeforeReceipt[s, nextState, t]
      or castVote[s, nextState, t, v]
      or commitTransfer[s, nextState, t]
      or timeoutTransfer[s, nextState, t]
      or stutter[s, nextState]
}

assert NoReceiptReplay {
  all s: State, r: Receipt | s.receiptUseCount[r] != ManyUses
}

assert DestinationCreditRequiresValidReceipt {
  all s: State, t: Transfer |
    t in s.destinationCredit implies
      some r: Receipt |
        r.owner = t and s.receiptUseCount[r] in OneUse + ManyUses
}

assert DecisionConsistency {
  all s: State, t: Transfer |
    s.status[t] = Committed implies t not in s.fundsReleased
}

assert EventuallyReleasedAfterTimeout {
  all s: State, t: Transfer |
    s.status[t] = Aborted implies t in s.fundsReleased
}

assert QuorumRequired {
  all s: State, t: Transfer |
    s.status[t] = Committed implies #s.votes[t] >= 2
}
assert NoValueLossAtTermination {
  all s: State, t: Transfer |
    (s.status[t] = Committed implies t in s.destinationCredit) and
    (s.status[t] = Aborted implies t in s.fundsReleased)
}

assert TerminalStateIrreversibility {
  all s: State - last, t: Transfer |
    s.status[t] in Committed + Aborted implies
      s.next.status[t] = s.status[t]
}

check NoReceiptReplay for exactly 7 State, exactly 2 Transfer, exactly 2 Shard, exactly 3 Validator, exactly 4 Receipt, exactly 20 Message expect 0
check DestinationCreditRequiresValidReceipt for exactly 7 State, exactly 2 Transfer, exactly 2 Shard, exactly 3 Validator, exactly 4 Receipt, exactly 20 Message expect 1
check DecisionConsistency for exactly 7 State, exactly 2 Transfer, exactly 2 Shard, exactly 3 Validator, exactly 4 Receipt, exactly 20 Message expect 0
check EventuallyReleasedAfterTimeout for exactly 7 State, exactly 2 Transfer, exactly 2 Shard, exactly 3 Validator, exactly 4 Receipt, exactly 20 Message expect 0
check QuorumRequired for exactly 7 State, exactly 2 Transfer, exactly 2 Shard, exactly 3 Validator, exactly 4 Receipt, exactly 20 Message expect 0
check NoValueLossAtTermination for exactly 7 State, exactly 2 Transfer, exactly 2 Shard, exactly 3 Validator, exactly 4 Receipt, exactly 20 Message expect 0
check TerminalStateIrreversibility for exactly 7 State, exactly 2 Transfer, exactly 2 Shard, exactly 3 Validator, exactly 4 Receipt, exactly 20 Message expect 0
