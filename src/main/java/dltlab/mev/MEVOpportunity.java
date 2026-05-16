package dltlab.mev;

/**
 * Oportunidad MEV abstracta.
 *
 * El simulador no modela un DEX real; modela el efecto pedagogico del orden:
 * una transaccion de busqueda puede ir antes, despues o alrededor de una
 * transaccion victima y transferir valor adicional al productor del bloque.
 */
public record MEVOpportunity(
        MEVType type,
        String victimLabel,
        String beforeLabel,
        String afterLabel,
        long extractableValue,
        String description
) {
    public MEVOpportunity {
        if (type == null) throw new IllegalArgumentException("El tipo MEV es obligatorio.");
        if (victimLabel == null || victimLabel.isBlank()) {
            throw new IllegalArgumentException("La transaccion victima es obligatoria.");
        }
        if (extractableValue < 0) {
            throw new IllegalArgumentException("El valor extraible no puede ser negativo.");
        }
    }

    public static MEVOpportunity frontRun(String victimLabel, String frontRunLabel,
                                          long extractableValue, String description) {
        return new MEVOpportunity(MEVType.FRONT_RUNNING, victimLabel, frontRunLabel, null,
                extractableValue, description);
    }

    public static MEVOpportunity backRun(String victimLabel, String backRunLabel,
                                         long extractableValue, String description) {
        return new MEVOpportunity(MEVType.BACK_RUNNING, victimLabel, null, backRunLabel,
                extractableValue, description);
    }

    public static MEVOpportunity sandwich(String victimLabel, String beforeLabel, String afterLabel,
                                          long extractableValue, String description) {
        return new MEVOpportunity(MEVType.SANDWICH, victimLabel, beforeLabel, afterLabel,
                extractableValue, description);
    }
}
