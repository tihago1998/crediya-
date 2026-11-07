package co.com.bancolombia.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA que mapea la tabla {@code usuarios} en PostgreSQL.
 *
 * <p>Es un modelo de infraestructura: solo existe en este adaptador y se convierte
 * a/desde el modelo de dominio {@code User} en {@code JPARepositoryAdapter}.</p>
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    /** Llave primaria tipo UUID generada automáticamente por Hibernate. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    /** Restricción UNIQUE: segunda barrera contra correos duplicados a nivel de base de datos. */
    @Column(name = "correo_electronico", nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Column(name = "salario_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal salarioBase;
}
