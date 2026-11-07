package co.com.bancolombia.api.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cuerpo (JSON) de la petición para registrar un usuario.
 *
 * <p>{@code @JsonProperty} define el nombre del campo en el JSON (snake_case),
 * tal como lo pide la historia de usuario. Ejemplo:</p>
 * <pre>
 * {
 *   "nombres": "Ana María",
 *   "apellidos": "Pérez Gómez",
 *   "fecha_nacimiento": "1990-05-20",
 *   "direccion": "Calle 10 # 20-30",
 *   "telefono": "3001234567",
 *   "correo_electronico": "ana@mail.com",
 *   "salario_base": 2500000
 * }
 * </pre>
 */
public record UserRequestDTO(
        @JsonProperty("nombres") String nombres,
        @JsonProperty("apellidos") String apellidos,
        @JsonProperty("fecha_nacimiento") LocalDate fechaNacimiento,
        @JsonProperty("direccion") String direccion,
        @JsonProperty("telefono") String telefono,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("salario_base") BigDecimal salarioBase
) {
}
