<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Bounded Context `notification` — Estrutura de Pacotes

> Arquivos de referência para o módulo `notification` do SIFAP 2.0.  
> Copie para o protótipo conforme as instruções abaixo.

## Estrutura gerada

```
notification/
├── domain/
│   ├── Notification.java            # @Entity JPA
│   ├── NotificationChannel.java     # enum EMAIL | SMS | PUSH
│   ├── NotificationStatus.java      # sealed interface (Java 21)
│   ├── NotificationEvent.java       # record para Spring Events
│   └── NotificationRepository.java  # interface Spring Data
├── application/
│   ├── NotificationService.java     # @Service — send / retry / findById
│   ├── SendNotificationCommand.java # record command de entrada
│   ├── NotificationDto.java         # record DTO de saída
│   └── NotificationNotFoundException.java
├── infrastructure/
│   ├── NotificationController.java      # @RestController /api/v1/notifications
│   ├── NotificationJpaRepository.java   # extends JpaRepository
│   └── NotificationEventListener.java   # @EventListener (recebe eventos de outros módulos)
└── V2__init_notification_module.sql     # migração Flyway
```

## Como copiar para o protótipo

```bash
# A partir da raiz do repositório, após rodar 11-scripts/setup.sh:
DEST=prototype/backend/src/main/java/br/gov/client/sifap/notification
mkdir -p "$DEST/domain" "$DEST/application" "$DEST/infrastructure"

cp 03-implementacao/notification/domain/*.java       "$DEST/domain/"
cp 03-implementacao/notification/application/*.java  "$DEST/application/"
cp 03-implementacao/notification/infrastructure/*.java "$DEST/infrastructure/"
cp 03-implementacao/notification/V2__init_notification_module.sql \
   prototype/backend/src/main/resources/db/migration/
```

## Rastreabilidade (HARD GATE CI)

| Arquivo | source_legacy |
|---------|--------------|
| `Notification.java` | `[GREENFIELD]` — LGPD/modernização |
| `NotificationEvent.java` | `BATCHCON.NSN#L166-L168`, `BATCHPGT.NSN#L88-L142`, `RELAUDIT.NSN#L45-L72` |
| `NotificationEventListener.java` | `BATCHCON.NSN#L166` |
| `V2__init_notification_module.sql` | `[GREENFIELD]` |

> ⚠️ Ao criar REQ-IDs para este módulo no `SPECIFICATION.md`, adicione `source_legacy:` em cada um. REQ-IDs GREENFIELD devem incluir justificativa (ex.: LGPD Art. 6º).

## Regras de arquitetura mantidas

- ✅ `NotificationService` nunca importa `payment.*` — comunica via `NotificationEvent` (Spring Events)
- ✅ `@Transactional` apenas na camada `application`
- ✅ Sem retorno `null` — usa `Optional` / lança exceção
- ✅ CPF mascarado via `NotificationService#maskSensitiveData` antes de persistir
- ✅ DTOs como Java records; entidade com construtor no-args protegido
- ✅ Endpoints com annotations OpenAPI/Swagger
- ✅ Erros com `ProblemDetail` (RFC 7807)
