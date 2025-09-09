package ma.digitalia.systemalert.event;

import lombok.Getter;
import ma.digitalia.systemalert.model.dto.AlerteDTO;
import org.springframework.context.ApplicationEvent;

@Getter
public class AlerteUpdatedEvent extends ApplicationEvent {
    private final AlerteDTO alerte;

    public AlerteUpdatedEvent(Object source, AlerteDTO alerte) {
        super(source);
        this.alerte = alerte;
    }

    public AlerteDTO getAlerte() {
        return alerte;
    }
}