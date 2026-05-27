// ============================================================================
// AgreementController.java — agreement · infrastructure
// ============================================================================
// Endpoints:
//   GET  /api/v1/agreements              → 200 + List<AgreementDto> (ativos)
//   GET  /api/v1/agreements/{id}         → 200 + AgreementDto
//   GET  /api/v1/agreements/code/{code}  → 200 + AgreementDto
//   POST /api/v1/agreements              → 201 + AgreementDto
//   POST /api/v1/agreements/{code}/close → 200 + AgreementDto
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN
// ============================================================================

package br.gov.client.sifap.agreement.infrastructure;

import br.gov.client.sifap.agreement.application.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agreements")
@Tag(name = "Agreements", description = "Cadastro de Programas Sociais (SIFAP 2.0)")
public class AgreementController {

    private final AgreementService service;

    public AgreementController(AgreementService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todos os programas ativos")
    @GetMapping
    public ResponseEntity<List<AgreementDto>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @Operation(summary = "Consulta programa por ID")
    @GetMapping("/{id}")
    public ResponseEntity<AgreementDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Consulta programa por código (ex.: '1001')")
    @GetMapping("/code/{code}")
    public ResponseEntity<AgreementDto> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }

    @Operation(summary = "Cadastra novo programa social")
    @PostMapping
    public ResponseEntity<AgreementDto> create(@Valid @RequestBody CreateAgreementCommand cmd) {
        AgreementDto dto = service.create(cmd);
        return ResponseEntity
                .created(URI.create("/api/v1/agreements/" + dto.id()))
                .body(dto);
    }

    @Operation(summary = "Encerra um programa social")
    @PostMapping("/{code}/close")
    public ResponseEntity<AgreementDto> close(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closeDate) {
        return ResponseEntity.ok(service.close(code, closeDate));
    }

    @ExceptionHandler(AgreementNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(AgreementNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Agreement Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleConflict(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }
}
