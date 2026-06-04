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
  t.fundsReleased = Yes implies t.originDebited = Yes
  t.committed = Yes implies t.destinationCredited = Yes
  t.aborted = Yes implies t.destinationCredited = No
  t.expired = Yes implies t.aborted = Yes
}

assert NoDoubleMint {
  all t: Transfer |
    wellFormed[t] implies
      (t.destinationCredited = Yes implies t.receiptCreated = Yes and #t.receiptUse = 1)
}

assert NoValueLoss {
  all t: Transfer |
    wellFormed[t] implies
      ((t.committed = Yes or t.aborted = Yes) and t.originDebited = Yes implies
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

check NoDoubleMint for 5 Transfer, 5 Receipt
check NoValueLoss for 5 Transfer, 5 Receipt
check NoReceiptReplay for 5 Transfer, 5 Receipt
check AtomicCommit for 5 Transfer, 5 Receipt
check TimeoutReleasesFunds for 5 Transfer, 5 Receipt
