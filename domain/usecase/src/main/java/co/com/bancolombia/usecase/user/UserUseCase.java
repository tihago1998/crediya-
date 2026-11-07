package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Caso de uso: Registrar usuarios en el sistema (Historia de Usuario 1).
 *
 * <p>Orquesta las reglas de negocio del registro y delega la persistencia al
 * puerto {@link UserRepository}. Se registra como bean automáticamente gracias a
 * {@code UseCasesConfig} (todas las clases que terminan en "UseCase").</p>
 */
@RequiredArgsConstructor
public class UserUseCase {

    /** Logger de Java estándar: el dominio no depende de librerías de logging externas. */
    private static final Logger LOG = Logger.getLogger(UserUseCase.class.getName());

    /** Puerto de persistencia; la implementación real la inyecta Spring (adaptador JPA). */
    private final UserRepository userRepository;

    /**
     * Registra un nuevo usuario.
     *
     * <ol>
     *   <li>Valida campos obligatorios y formatos (email, rango de salario).</li>
     *   <li>Normaliza el correo (sin espacios y en minúsculas).</li>
     *   <li>Verifica que el correo no esté registrado previamente.</li>
     *   <li>Guarda el usuario de forma permanente.</li>
     * </ol>
     *
     * @param user datos del usuario a registrar
     * @return el usuario registrado con su identificador
     * @throws BusinessException si los datos son inválidos o el correo ya existe
     */
    public User saveUser(User user) {
        LOG.info("Iniciando registro de usuario");

        UserValidator.validate(user);

        // Se normaliza el correo para que "Ana@Mail.com" y "ana@mail.com" se consideren iguales
        User userToSave = user.toBuilder()
                .id(null) // el id siempre lo genera la base de datos
                .correoElectronico(user.getCorreoElectronico().trim().toLowerCase(Locale.ROOT))
                .build();

        if (userRepository.existsByCorreoElectronico(userToSave.getCorreoElectronico())) {
            LOG.warning("Registro rechazado: el correo electrónico ya está registrado");
            throw new BusinessException(BusinessErrorType.EMAIL_ALREADY_REGISTERED,
                    "El correo electrónico ya se encuentra registrado");
        }

        User savedUser = userRepository.save(userToSave);
        LOG.info(() -> "Usuario registrado exitosamente con id: " + savedUser.getId());
        return savedUser;
    }
}
