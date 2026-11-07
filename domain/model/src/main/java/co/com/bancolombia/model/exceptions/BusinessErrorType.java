package co.com.bancolombia.model.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catálogo de errores de negocio del dominio.
 *
 * <p>Cada tipo tiene un código estable que el consumidor de la API puede usar,
 * independiente del texto del mensaje. El dominio NO conoce códigos HTTP;
 * la traducción a HTTP la hace el entry point.</p>
 */
@Getter
@RequiredArgsConstructor
public enum BusinessErrorType {

    /** Algún dato de entrada no cumple las reglas de validación. */
    INVALID_DATA("USR-400"),

    /** El correo electrónico ya pertenece a otro usuario. */
    EMAIL_ALREADY_REGISTERED("USR-409");

    private final String code;
}
