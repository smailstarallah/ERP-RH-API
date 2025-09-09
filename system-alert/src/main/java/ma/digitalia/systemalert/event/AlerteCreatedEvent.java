package ma.digitalia.systemalert.event;

import ma.digitalia.systemalert.model.dto.AlerteDTO;

/**
 * Événement déclenché lors de la création d'une nouvelle alerte
 */
public class AlerteCreatedEvent {

    private final AlerteDTO alerte;
    private final String source;

    public AlerteCreatedEvent(AlerteDTO alerte, String source) {
        this.alerte = alerte;
        this.source = source;
    }

    public AlerteDTO getAlerte() {
        return alerte;
    }

    public String getSource() {
        return source;
    }
}
