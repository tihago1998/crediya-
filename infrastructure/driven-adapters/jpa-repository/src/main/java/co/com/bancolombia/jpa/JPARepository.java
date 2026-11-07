package co.com.bancolombia.jpa;

import co.com.bancolombia.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link UserEntity}.
 *
 * <p>Spring genera la implementación automáticamente. {@link JpaRepository} incluye
 * las operaciones CRUD, consultas por ejemplo y {@code saveAndFlush}.</p>
 */
public interface JPARepository extends JpaRepository<UserEntity, String> {

    /**
     * Consulta derivada del nombre del método:
     * {@code SELECT COUNT(*) > 0 FROM usuarios WHERE correo_electronico = ?}
     *
     * @param correoElectronico correo a buscar
     * @return {@code true} si existe un usuario con ese correo
     */
    boolean existsByCorreoElectronico(String correoElectronico);
}
