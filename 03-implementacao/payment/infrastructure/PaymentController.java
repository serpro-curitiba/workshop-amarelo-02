// ============================================================================
// PaymentController.java — payment · infrastructure
// ============================================================================
// Endpoints:
//   POST  /api/v1/payments                   → 201 + PaymentDto
//   GET   /api/v1/payments/{id}              → 200 + PaymentDto
//   GET   /api/v1/payments/cpf/{cpf}         → 200 + List<PaymentDto>
//   GET   /api/v1/payments?status=G&page=0   → 200 + Page<PaymentDto>
//   POST  /api/v1/payments/{id}/issue        → 200 + PaymentDto
//   POST  /api/v1/payments/{id}/confirm      → 200 + PaymentDto
//   POST  /api/v1/payments/{id}/cancel       → 200 + PaymentDto
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN
//                  01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN
// ============================================================================

package br.gov.client.sifap.payment.infrastructure;

import br.gov.client.sifap.payment.application.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Gestão de pagamentos do SIFAP 2.0")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @Operation(summary = "Gera pagamento para um beneficiário na competência (idempotente)")
    @PostMapping
    public ResponseEntity<PaymentDto> generate(
            @RequestParam String cpf,
            @RequestParam String programCode,
            @RequestParam Integer period,
            @RequestParam BigDecimal grossAmount) {
        PaymentDto dto = service.generate(cpf, programCode, period, grossAmount);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Consulta pagamento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Lista pagamentos de um beneficiário (CPF)")
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<List<PaymentDto>> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.findByCpf(cpf));
    }

    @Operation(summary = "Lista pagamentos por status (paginado)")
    @GetMapping
    public ResponseEntity<Page<PaymentDto>> findByStatus(
            @RequestParam(defaultValue = "G") String status,
            @PageableDefault(size = 500) Pageable pageable) {
        return ResponseEntity.ok(service.findByStatus(status, pageable));
    }

    @Operation(summary = "Marca pagamento como emitido (enviado ao banco)")
    @PostMapping("/{id}/issue")
    public ResponseEntity<PaymentDto> issue(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issuedDate) {
        return ResponseEntity.ok(service.markIssued(id, issuedDate));
    }

    @Operation(summary = "Confirma pagamento (retorno bancário 00)")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentDto> confirm(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate confirmedDate) {
        return ResponseEntity.ok(service.confirm(id, confirmedDate));
    }

    @Operation(summary = "Cancela pagamento")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentDto> cancel(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(service.cancel(id, reason));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(PaymentNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Payment Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleConflict(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Invalid State Transition");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }
}
