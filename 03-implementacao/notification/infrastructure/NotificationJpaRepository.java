// ============================================================================
// NotificationJpaRepository.java — bounded context: notification · infrastructure
// ============================================================================
// Adaptador de persistência — extends JpaRepository implementa NotificationRepository.
// Spring Data gera a implementação em runtime; não adicione lógica aqui.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD]
// ============================================================================

package br.gov.client.sifap.notification.infrastructure;

import br.gov.client.sifap.notification.domain.Notification;
import br.gov.client.sifap.notification.domain.NotificationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Implementação JPA de NotificationRepository.
 * Spring Data gera automaticamente findByStatus e findByReference (definidos na interface pai).
 */
public interface NotificationJpaRepository
        extends NotificationRepository, JpaRepository<Notification, Long> {
}
