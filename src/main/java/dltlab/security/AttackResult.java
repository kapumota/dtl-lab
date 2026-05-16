package dltlab.security;

/** Resultado de un ataque educativo. */
public record AttackResult(String attackName, boolean defenseWorked, String explanation) {
    public String render() {
        return "Ataque: " + attackName + "\n" +
                (defenseWorked ? "Defensa: PASO\n" : "Defensa: FALLO\n") +
                explanation;
    }
}
