package co.com.bancolombia.api.user;

import co.com.bancolombia.api.user.dto.UserRequestDTO;
import co.com.bancolombia.api.user.dto.UserResponseDTO;
import co.com.bancolombia.api.user.mapper.UserDTOMapper;
import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST (entry point) para la gestión de usuarios.
 *
 * <p>Solo traduce HTTP &lt;-&gt; dominio y delega toda la lógica al caso de uso
 * {@link UserUseCase}. Los errores los procesa {@code GlobalExceptionHandler}.</p>
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    /** Caso de uso inyectado por constructor (campo final, regla de arquitectura 2.7). */
    private final UserUseCase userUseCase;

    /**
     * POST /api/v1/usuarios — Registra un nuevo usuario.
     *
     * @param request datos del usuario en formato JSON
     * @return 201 Created con el usuario registrado
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody(required = false) UserRequestDTO request) {
        log.info("Solicitud recibida: POST /api/v1/usuarios");

        // Si el cuerpo viene vacío se responde con un error controlado, no con un 500
        if (request == null) {
            throw new BusinessException(BusinessErrorType.INVALID_DATA, "El cuerpo de la solicitud es obligatorio");
        }

        User savedUser = userUseCase.saveUser(UserDTOMapper.toDomain(request));

        log.info("Usuario registrado, respondiendo 201 Created. id: {}", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDTOMapper.toResponse(savedUser));
    }
}
