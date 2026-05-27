// ============================================================================
// BeneficiaryController.java — beneficiary · infrastructure
// ============================================================================
// REST adapter. Camada fina: delega tudo ao BeneficiaryService.
// Endpoints:
//   POST   /api/v1/beneficiaries              → 201 + BeneficiaryDto
//   GET    /api/v1/beneficiaries/{id}         → 200 + BeneficiaryDto
//   GET    /api/v1/beneficiaries/cpf/{cpf}    → 200 + BeneficiaryDto
//   GET    /api/v1/beneficiaries?status=A     → 200 + Page<BeneficiaryDto>
//   PATCH  /api/v1/beneficiaries/{id}/status  → 200 + BeneficiaryDto
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN
// ============================================================================

package br.gov.client.sifap.beneficiary.infrastructure;

import br.gov.client.sifap.beneficiary.application.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "Beneficiaries", description = "Gestão de beneficiários do SIFAP 2.0")
public class BeneficiaryController {

    private final BeneficiaryService service;

    public BeneficiaryController(BeneficiaryService service) {
        this.service = service;
    }

    @Operation(summary = "Registra novo beneficiário")
    @PostMapping
    public ResponseEntity<BeneficiaryDto> register(
            @Valid @RequestBody RegisterBeneficiaryCommand cmd) {
        BeneficiaryDto dto = service.register(cmd);
        return ResponseEntity
                .created(URI.create("/api/v1/beneficiaries/" + dto.id()))
                .body(dto);
    }

    @Operation(summary = "Consulta beneficiário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Consulta beneficiário por CPF")
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<BeneficiaryDto> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.findByCpf(cpf));
    }

    @Operation(summary = "Lista beneficiários ativos paginados (ordenados por CPF)")
    @GetMapping
    public ResponseEntity<Page<BeneficiaryDto>> findActive(
            @PageableDefault(size = 100, sort = "cpf") Pageable pageable) {
        return ResponseEntity.ok(service.findAllActive(pageable));
    }

    @Operation(summary = "Altera status do beneficiário (suspender, cancelar, reativar)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<BeneficiaryDto> changeStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(service.changeStatus(id, status, reason));
    }

    @ExceptionHandler(BeneficiaryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(BeneficiaryNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Beneficiary Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleConflict(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }
}
