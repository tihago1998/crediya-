package co.com.bancolombia.api.exception;

import java.time.OffsetDateTime;

/**
 * Formato estándar de error que devuelve la API. Ejemplo:
 * <pre>
 * {
 *   "timestamp": "2026-11-05T10:15:30-05:00",
 *   "status": 409,
 *   "code": "USR-409",
 *   "message": "El correo electrónico ya se encuentra registrado",
 *   "path": "/api/v1/usuarios"
 * }
 * </pre>
 *
 * @param timestamp momento en que ocurrió el error
 * @param status    código HTTP
 * @param code      código de error de negocio/técnico
 * @param message   mensaje claro para el consumidor
 * @param path      ruta solicitada
 */
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        String path
) {
}
