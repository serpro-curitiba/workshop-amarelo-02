// ============================================================================
// NotificationNotFoundException.java — bounded context: notification · application
// ============================================================================

package br.gov.client.sifap.notification.application;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long id) {
        super("Notification not found: id=" + id);
    }
}
