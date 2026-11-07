package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del caso de uso de registro de usuarios (criterios de aceptación de la HU 1).
 */
@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userUseCase = new UserUseCase(userRepository);
    }

    private static User validUser() {
        return User.builder()
                .nombres("Ana María")
                .apellidos("Pérez Gómez")
                .fechaNacimiento(LocalDate.of(1990, 5, 20))
                .direccion("Calle 10 # 20-30")
                .telefono("3001234567")
                .correoElectronico("Ana@Mail.com")
                .salarioBase(new BigDecimal("2500000"))
                .build();
    }

    @Test
    void shouldRegisterUserWhenDataIsValid() {
        when(userRepository.existsByCorreoElectronico("ana@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0, User.class).toBuilder().id("uuid-1").build());

        User result = userUseCase.saveUser(validUser());

        assertThat(result.getId()).isEqualTo("uuid-1");
        // El correo se guarda normalizado en minúsculas
        assertThat(result.getCorreoElectronico()).isEqualTo("ana@mail.com");
    }

    @Test
    void shouldIgnoreIdSentByClient() {
        when(userRepository.existsByCorreoElectronico(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userUseCase.saveUser(validUser().toBuilder().id("id-del-cliente").build());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    void shouldFailWhenEmailAlreadyRegistered() {
        when(userRepository.existsByCorreoElectronico("ana@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userUseCase.saveUser(validUser()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorType").isEqualTo(BusinessErrorType.EMAIL_ALREADY_REGISTERED);

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldFailWhenRequiredTextFieldsAreBlank(String value) {
        User user = validUser().toBuilder().nombres(value).apellidos(value).correoElectronico(value).build();

        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nombres")
                .hasMessageContaining("apellidos")
                .hasMessageContaining("correo_electronico")
                .extracting("errorType").isEqualTo(BusinessErrorType.INVALID_DATA);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenSalaryIsNull() {
        User user = validUser().toBuilder().salarioBase(null).build();

        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("salario_base es obligatorio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "15000000.01", "20000000"})
    void shouldFailWhenSalaryIsOutOfRange(String salary) {
        User user = validUser().toBuilder().salarioBase(new BigDecimal(salary)).build();

        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("entre 0 y 15000000");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "15000000"})
    void shouldAcceptSalaryLimits(String salary) {
        when(userRepository.existsByCorreoElectronico(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userUseCase.saveUser(validUser().toBuilder().salarioBase(new BigDecimal(salary)).build());

        assertThat(result.getSalarioBase()).isEqualByComparingTo(salary);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@", "ana@mail", "@mail.com", "ana mail@mail.com"})
    void shouldFailWhenEmailFormatIsInvalid(String email) {
        User user = validUser().toBuilder().correoElectronico(email).build();

        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("formato válido");
    }

    @Test
    void shouldFailWhenUserIsNull() {
        assertThatThrownBy(() -> userUseCase.saveUser(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorType").isEqualTo(BusinessErrorType.INVALID_DATA);
    }
}
