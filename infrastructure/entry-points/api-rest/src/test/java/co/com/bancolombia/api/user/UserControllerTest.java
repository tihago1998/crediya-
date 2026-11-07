package co.com.bancolombia.api.user;

import co.com.bancolombia.api.exception.GlobalExceptionHandler;
import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del endpoint POST /api/v1/usuarios y del manejo de errores.
 */
class UserControllerTest {

    private static final String URL = "/api/v1/usuarios";

    private static final String VALID_BODY = """
            {
              "nombres": "Ana",
              "apellidos": "Pérez",
              "fecha_nacimiento": "1990-05-20",
              "direccion": "Calle 10",
              "telefono": "3001234567",
              "correo_electronico": "ana@mail.com",
              "salario_base": 2500000
            }
            """;

    private UserUseCase userUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userUseCase = mock(UserUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn201WhenUserIsRegistered() throws Exception {
        when(userUseCase.saveUser(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0, User.class).toBuilder().id("uuid-1").build());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("uuid-1"))
                .andExpect(jsonPath("$.correo_electronico").value("ana@mail.com"))
                .andExpect(jsonPath("$.fecha_nacimiento").value("1990-05-20"))
                .andExpect(jsonPath("$.salario_base").value(2500000));
    }

    @Test
    void shouldReturn400WhenBusinessValidationFails() throws Exception {
        when(userUseCase.saveUser(any(User.class)))
                .thenThrow(new BusinessException(BusinessErrorType.INVALID_DATA, "El campo nombres es obligatorio"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USR-400"))
                .andExpect(jsonPath("$.message").value("El campo nombres es obligatorio"))
                .andExpect(jsonPath("$.path").value(URL));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyRegistered() throws Exception {
        when(userUseCase.saveUser(any(User.class)))
                .thenThrow(new BusinessException(BusinessErrorType.EMAIL_ALREADY_REGISTERED, "duplicado"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USR-409"));
    }

    @Test
    void shouldReturn400WhenSalaryIsNotNumeric() throws Exception {
        String body = VALID_BODY.replace("2500000", "\"abc\"");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQ-400"));
    }

    @Test
    void shouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USR-400"));
    }

    @Test
    void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("REQ-415"));
    }

    @Test
    void shouldReturn405WhenMethodIsNotAllowed() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldReturn500WithGenericMessageOnUnexpectedError() throws Exception {
        when(userUseCase.saveUser(any(User.class))).thenThrow(new IllegalStateException("detalle interno"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("SYS-500"))
                .andExpect(jsonPath("$.message").value("Ocurrió un error interno, intente más tarde"));
    }

    @Test
    void shouldMapRequestToDomainCorrectly() throws Exception {
        when(userUseCase.saveUser(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            // Verifica que el JSON snake_case llegó correctamente al modelo de dominio
            if (!"Pérez".equals(u.getApellidos()) || u.getSalarioBase().compareTo(new BigDecimal("2500000")) != 0) {
                throw new AssertionError("Mapeo incorrecto");
            }
            return u;
        });

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());
    }
}
