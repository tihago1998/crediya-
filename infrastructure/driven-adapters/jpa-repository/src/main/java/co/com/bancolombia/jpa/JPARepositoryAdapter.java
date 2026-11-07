package co.com.bancolombia.jpa;

import co.com.bancolombia.jpa.entity.UserEntity;
import co.com.bancolombia.jpa.helper.AdapterOperations;
import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de persistencia: implementa el puerto de dominio {@link UserRepository}
 * usando Spring Data JPA sobre PostgreSQL.
 *
 * <p>Convierte entre el modelo de dominio {@link User} y la entidad {@link UserEntity},
 * de modo que el dominio nunca conoce detalles de JPA.</p>
 */
@Slf4j
@Repository
public class JPARepositoryAdapter extends AdapterOperations<User, UserEntity, String, JPARepository>
        implements UserRepository {

    public JPARepositoryAdapter(JPARepository repository, ObjectMapper mapper) {
        // Se pasa la función de conversión entidad -> dominio
        super(repository, mapper, JPARepositoryAdapter::toDomain);
    }

    /**
     * Guarda el usuario dentro de una transacción.
     *
     * <p>{@code @Transactional} garantiza la atomicidad: si algo falla, se hace rollback
     * y no queda información a medias. Se usa {@code saveAndFlush} para que el INSERT
     * se ejecute dentro de este método y así poder capturar aquí la violación de la
     * restricción UNIQUE del correo (por ejemplo, dos registros simultáneos).</p>
     */
    @Override
    @Transactional
    public User save(User user) {
        log.debug("Persistiendo usuario en base de datos");
        try {
            User saved = toEntity(repository.saveAndFlush(toData(user)));
            log.info("Usuario persistido con id: {}", saved.getId());
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.warn("Violación de integridad al guardar usuario: {}", e.getMostSpecificCause().getMessage());
            throw new BusinessException(BusinessErrorType.EMAIL_ALREADY_REGISTERED,
                    "El correo electrónico ya se encuentra registrado");
        }
    }

    /** Consulta de solo lectura: {@code readOnly} optimiza la transacción. */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByCorreoElectronico(String correoElectronico) {
        boolean exists = repository.existsByCorreoElectronico(correoElectronico);
        log.debug("Verificación de correo existente: {}", exists);
        return exists;
    }

    /** Convierte el modelo de dominio a entidad JPA. */
    @Override
    protected UserEntity toData(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .fechaNacimiento(user.getFechaNacimiento())
                .direccion(user.getDireccion())
                .telefono(user.getTelefono())
                .correoElectronico(user.getCorreoElectronico())
                .salarioBase(user.getSalarioBase())
                .build();
    }

    /** Convierte la entidad JPA al modelo de dominio. */
    private static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .fechaNacimiento(entity.getFechaNacimiento())
                .direccion(entity.getDireccion())
                .telefono(entity.getTelefono())
                .correoElectronico(entity.getCorreoElectronico())
                .salarioBase(entity.getSalarioBase())
                .build();
    }
}
