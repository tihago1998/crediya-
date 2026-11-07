package co.com.bancolombia.api.exception;

import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;

/**
 * Manejador global de excepciones de la API.
 *
 * <p>Garantiza que TODA excepción se procese y se responda con un {@link ErrorResponse}
 * claro, sin exponer trazas ni mensajes técnicos inesperados al consumidor:</p>
 * <ul>
 *   <li>{@link BusinessException} (reglas de negocio) → 400 o 409.</li>
 *   <li>Errores propios de Spring MVC (JSON mal formado, método no permitido,
 *       ruta inexistente, etc.) → su código HTTP correspondiente.</li>
 *   <li>Cualquier otra excepción → 500 con mensaje genérico.</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Errores de negocio lanzados por el dominio. */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorType() == BusinessErrorType.EMAIL_ALREADY_REGISTERED
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        log.warn("Error de negocio [{}]: {}", ex.getErrorType().getCode(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(buildError(status.value(), ex.getErrorType().getCode(), ex.getMessage(), request.getRequestURI()));
    }

    /** Cualquier error no previsto: se registra completo en el log pero al cliente se le da un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado procesando {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SYS-500",
                        "Ocurrió un error interno, intente más tarde", request.getRequestURI()));
    }

    /**
     * Punto común por el que pasan todas las excepciones estándar de Spring MVC
     * (415, 405, 404, JSON inválido...). Se sobrescribe para responder con nuestro formato.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String message = ex instanceof HttpMessageNotReadableException
                // Ej: salario_base = "abc" o fecha_nacimiento con formato inválido
                ? "El cuerpo de la solicitud tiene un formato inválido. Verifique tipos de datos "
                  + "(salario_base numérico, fecha_nacimiento con formato yyyy-MM-dd)"
                : "No fue posible procesar la solicitud: " + reasonPhrase(statusCode);

        String path = request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : "";

        log.warn("Error en la solicitud {} ({}): {}", path, statusCode.value(), ex.getMessage());
        return ResponseEntity.status(statusCode).headers(headers)
                .body(buildError(statusCode.value(), "REQ-" + statusCode.value(), message, path));
    }

    /** Texto estándar del código HTTP (ej. "Not Found"); evita fallar con códigos no estándar. */
    private static String reasonPhrase(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status != null ? status.getReasonPhrase() : String.valueOf(statusCode.value());
    }

    private static ErrorResponse buildError(int status, String code, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, code, message, path);
    }
}
