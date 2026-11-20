---- MODULE MC ----
EXTENDS CrossShardCommitV2
MCInputShared == [t \in Transfers |-> "u1"]
MCInputDisjoint == [t \in Transfers |-> IF t = "t1" THEN "u1" ELSE "u2"]
MCSrc == [t \in Transfers |-> "s1"]
MCDst == [t \in Transfers |-> IF t = "t1" THEN "s2" ELSE "s3"]
MCAmount == [t \in Transfers |-> IF t = "t1" THEN 1 ELSE 2]
====
