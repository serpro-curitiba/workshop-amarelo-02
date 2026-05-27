// ============================================================================
// NotificationRepository.java — bounded context: notification
// ============================================================================
// Porta de saída (interface Spring Data JPA). A implementação é gerada
// automaticamente pelo framework — não crie uma classe concreta para isso.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD]
// ============================================================================

package br.gov.client.sifap.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Busca todas as notificações com determinado status — usado pelo job de retry. */
    List<Notification> findByStatus(String status);

    /** Busca notificações por referência para facilitar rastreabilidade. */
    @Query("SELECT n FROM Notification n WHERE n.referenceId = :refId AND n.referenceTable = :refTable")
    List<Notification> findByReference(
            @Param("refId") Long referenceId,
            @Param("refTable") String referenceTable);
}
