module CrossShardCommit

// Modelo Alloy acotado del commit cross-shard de DLT-Lab.
// Las firmas representan estados observables de una transferencia.

abstract sig Flag {}
one sig Yes, No extends Flag {}

sig Receipt {}

sig Transfer {
  originDebited: one Flag,
  receiptCreated: one Flag,
  destinationCredited: one Flag,
  fundsReleased: one Flag,
  committed: one Flag,
  aborted: one Flag,
  expired: one Flag,
  receiptUse: set Receipt
}

pred wellFormed[t: Transfer] {
  t.destinationCredited = Yes implies t.originDebited = Yes
  t.destinationCredited = Yes implies t.receiptCreated = Yes
  t.destinationCredited = Yes implies #t.receiptUse = 1
  #t.receiptUse <= 1
  t.fundsReleased = Yes implies t.originDebited = Yes
  t.committed = Yes implies t.destinationCredited = Yes
  t.committed = Yes implies t.aborted = No
  t.aborted = Yes implies t.destinationCredited = No
  (t.aborted = Yes and t.originDebited = Yes) implies t.fundsReleased = Yes
  t.expired = Yes implies t.aborted = Yes
  t.expired = Yes implies t.fundsReleased = Yes
}

assert NoDoubleMint {
  all t: Transfer |
    wellFormed[t] implies
      (t.destinationCredited = Yes implies t.receiptCreated = Yes and #t.receiptUse = 1)
}

assert NoValueLoss {
  all t: Transfer |
    wellFormed[t] implies
      (((t.committed = Yes or t.aborted = Yes) and t.originDebited = Yes) implies
        (t.destinationCredited = Yes or t.fundsReleased = Yes))
}

assert NoReceiptReplay {
  all t: Transfer |
    wellFormed[t] implies #t.receiptUse <= 1
}

assert AtomicCommit {
  all t: Transfer |
    wellFormed[t] implies not (t.committed = Yes and t.aborted = Yes)
}

assert TimeoutReleasesFunds {
  all t: Transfer |
    wellFormed[t] implies (t.expired = Yes implies t.fundsReleased = Yes)
}

check NoDoubleMint for 5 Transfer, 5 Receipt expect 0
check NoValueLoss for 5 Transfer, 5 Receipt expect 0
check NoReceiptReplay for 5 Transfer, 5 Receipt expect 0
check AtomicCommit for 5 Transfer, 5 Receipt expect 0
check TimeoutReleasesFunds for 5 Transfer, 5 Receipt expect 0
