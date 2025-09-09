package ma.digitalia.systemalert.exception;

/**
 * Exception lancée quand une alerte n'est pas trouvée
 */
public class AlerteNotFoundException extends RuntimeException {

    public AlerteNotFoundException(String message) {
        super(message);
    }

    public AlerteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlerteNotFoundException(Long alerteId) {
        super("Alerte avec l'ID " + alerteId + " non trouvée");
    }
}
