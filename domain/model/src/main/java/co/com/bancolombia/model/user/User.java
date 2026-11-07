package co.com.bancolombia.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de dominio que representa a un usuario (solicitante) del sistema CrediYa.
 *
 * <p>Es una clase pura de negocio: no conoce JPA, HTTP ni ningún otro detalle de
 * infraestructura (principio de la arquitectura hexagonal).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    /** Identificador único generado por la capa de persistencia. */
    private String id;

    /** Nombres del usuario (obligatorio). */
    private String nombres;

    /** Apellidos del usuario (obligatorio). */
    private String apellidos;

    /** Fecha de nacimiento (opcional). */
    private LocalDate fechaNacimiento;

    /** Dirección de residencia (opcional). */
    private String direccion;

    /** Teléfono de contacto (opcional). */
    private String telefono;

    /** Correo electrónico (obligatorio, único y con formato válido). */
    private String correoElectronico;

    /** Salario base (obligatorio, entre 0 y 15.000.000). */
    private BigDecimal salarioBase;
}
