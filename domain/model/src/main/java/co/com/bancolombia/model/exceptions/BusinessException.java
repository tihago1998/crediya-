package co.com.bancolombia.model.exceptions;

import lombok.Getter;

/**
 * Excepción de negocio lanzada cuando se incumple una regla del dominio
 * (datos inválidos, correo duplicado, etc.).
 *
 * <p>Es una excepción controlada por el sistema: el entry point la convierte en
 * una respuesta de error clara para el consumidor de la API.</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** Tipo de error de negocio que originó la excepción. */
    private final BusinessErrorType errorType;

    /**
     * @param errorType tipo de error de negocio
     * @param message   mensaje descriptivo para el consumidor de la API
     */
    public BusinessException(BusinessErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
