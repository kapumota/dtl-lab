package dltlab.app;

import dltlab.security.CrossShardReplayAttack;
import dltlab.security.CrossShardTimeoutAttack;
import dltlab.security.DoubleSpendAttack;
import dltlab.security.InvalidSignatureAttack;

/** Punto de entrada de linea de comandos. */
public class DltLabCLI {
    public static void main(String[] args) {
        if (args.length == 0 || (args.length >= 2 && args[0].equals("demo") && args[1].equals("full"))) {
            new DemoRunner().runFullDemo();
            return;
        }

        if (args.length >= 2 && args[0].equals("demo") && args[1].equals("mev")) {
            new DemoRunner().runMevOnly();
            return;
        }

        if (args.length >= 2 && args[0].equals("demo") && args[1].equals("consensus")) {
            new DemoRunner().runConsensusOnly();
            return;
        }

        if (args.length >= 2 && args[0].equals("demo") && args[1].equals("sharding")) {
            new DemoRunner().runShardingOnly();
            return;
        }

        if (args[0].equals("verify")) {
            new DemoRunner().runVerificationOnly();
            return;
        }

        if (args[0].equals("security")) {
            new DemoRunner().runSecurityOnly();
            return;
        }

        if (args.length >= 2 && args[0].equals("attack")) {
            switch (args[1]) {
                case "double-spend" -> System.out.println(new DoubleSpendAttack().run().render());
                case "invalid-signature" -> System.out.println(new InvalidSignatureAttack().run().render());
                case "cross-shard-replay" -> System.out.println(new CrossShardReplayAttack().run().render());
                case "cross-shard-timeout" -> System.out.println(new CrossShardTimeoutAttack().run().render());
                default -> printHelp();
            }
            return;
        }

        printHelp();
    }

    private static void printHelp() {
        System.out.println("Uso:");
        System.out.println("  java dltlab.app.DltLabCLI demo full");
        System.out.println("  java dltlab.app.DltLabCLI demo mev");
        System.out.println("  java dltlab.app.DltLabCLI demo consensus");
        System.out.println("  java dltlab.app.DltLabCLI demo sharding");
        System.out.println("  java dltlab.app.DltLabCLI verify");
        System.out.println("  java dltlab.app.DltLabCLI security");
        System.out.println("  java dltlab.app.DltLabCLI attack double-spend");
        System.out.println("  java dltlab.app.DltLabCLI attack invalid-signature");
        System.out.println("  java dltlab.app.DltLabCLI attack cross-shard-replay");
        System.out.println("  java dltlab.app.DltLabCLI attack cross-shard-timeout");
    }
}
