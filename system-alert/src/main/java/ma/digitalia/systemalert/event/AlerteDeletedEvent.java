package ma.digitalia.systemalert.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AlerteDeletedEvent extends ApplicationEvent {
    private final Long alerteId;
    private final Long userId; // L'ID de l'utilisateur pour notifier son topic personnel

    public AlerteDeletedEvent(Object source, Long alerteId, Long userId) {
        super(source);
        this.alerteId = alerteId;
        this.userId = userId;
    }

}