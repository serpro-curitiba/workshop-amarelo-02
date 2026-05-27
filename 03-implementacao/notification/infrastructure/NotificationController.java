// ============================================================================
// NotificationController.java — bounded context: notification · infrastructure
// ============================================================================
// Adaptador de entrada REST. Camada fina — sem lógica de negócio aqui.
// Valida entrada com @Valid; responde com ProblemDetail (RFC 7807) para erros.
//
// Endpoints:
//   POST   /api/v1/notifications          → 201 Created + NotificationDto
//   GET    /api/v1/notifications/{id}     → 200 OK + NotificationDto
//   POST   /api/v1/notifications/{id}/retry → 200 OK + NotificationDto
//   GET    /api/v1/notifications?status=FAILED → 200 OK + List<NotificationDto>
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD]
// ============================================================================

package br.gov.client.sifap.notification.infrastructure;

import br.gov.client.sifap.notification.application.NotificationDto;
import br.gov.client.sifap.notification.application.NotificationNotFoundException;
import br.gov.client.sifap.notification.application.NotificationService;
import br.gov.client.sifap.notification.application.SendNotificationCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Gerenciamento de notificações do SIFAP 2.0")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Envia uma notificação para um destinatário")
    @PostMapping
    public ResponseEntity<NotificationDto> send(@Valid @RequestBody SendNotificationCommand command) {
        NotificationDto dto = notificationService.send(command);
        return ResponseEntity
                .created(URI.create("/api/v1/notifications/" + dto.id()))
                .body(dto);
    }

    @Operation(summary = "Consulta uma notificação por ID")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.findById(id));
    }

    @Operation(summary = "Reprocessa uma notificação com status FAILED")
    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationDto> retry(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.retry(id));
    }

    @Operation(summary = "Lista notificações por status (ex.: FAILED)")
    @GetMapping
    public ResponseEntity<List<NotificationDto>> findByStatus(
            @RequestParam(defaultValue = "FAILED") String status) {
        if ("FAILED".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(notificationService.findFailed());
        }
        return ResponseEntity.ok(List.of());
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotificationNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setTitle("Notification Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        detail.setTitle("Invalid State Transition");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(detail);
    }
}
