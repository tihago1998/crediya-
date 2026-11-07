package co.com.bancolombia.jpa;

import co.com.bancolombia.jpa.entity.UserEntity;
import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del adaptador JPA: mapeo dominio/entidad y traducción de errores de BD.
 */
@ExtendWith(MockitoExtension.class)
class JPARepositoryAdapterTest {

    @Mock
    private JPARepository repository;

    @Mock
    private ObjectMapper mapper;

    private JPARepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JPARepositoryAdapter(repository, mapper);
    }

    private static User user() {
        return User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .fechaNacimiento(LocalDate.of(1990, 5, 20))
                .direccion("Calle 10")
                .telefono("3001234567")
                .correoElectronico("ana@mail.com")
                .salarioBase(new BigDecimal("2500000"))
                .build();
    }

    @Test
    void shouldMapAndSaveUser() {
        when(repository.saveAndFlush(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity entity = inv.getArgument(0);
            entity.setId("uuid-1");
            return entity;
        });

        User saved = adapter.save(user());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        UserEntity entity = captor.getValue();
        assertThat(entity.getCorreoElectronico()).isEqualTo("ana@mail.com");
        assertThat(entity.getSalarioBase()).isEqualByComparingTo("2500000");
        assertThat(entity.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 5, 20));

        assertThat(saved.getId()).isEqualTo("uuid-1");
        assertThat(saved.getNombres()).isEqualTo("Ana");
        assertThat(saved.getApellidos()).isEqualTo("Pérez");
        assertThat(saved.getDireccion()).isEqualTo("Calle 10");
        assertThat(saved.getTelefono()).isEqualTo("3001234567");
    }

    @Test
    void shouldTranslateUniqueConstraintViolationToBusinessException() {
        when(repository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key correo_electronico"));

        assertThatThrownBy(() -> adapter.save(user()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorType").isEqualTo(BusinessErrorType.EMAIL_ALREADY_REGISTERED);
    }

    @Test
    void shouldDelegateExistsByEmail() {
        when(repository.existsByCorreoElectronico("ana@mail.com")).thenReturn(true);

        assertThat(adapter.existsByCorreoElectronico("ana@mail.com")).isTrue();
    }
}
