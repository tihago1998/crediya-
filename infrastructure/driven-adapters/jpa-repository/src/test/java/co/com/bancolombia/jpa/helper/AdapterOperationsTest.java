package co.com.bancolombia.jpa.helper;

import co.com.bancolombia.jpa.JPARepository;
import co.com.bancolombia.jpa.JPARepositoryAdapter;
import co.com.bancolombia.jpa.entity.UserEntity;
import co.com.bancolombia.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las operaciones genéricas heredadas de {@link AdapterOperations},
 * usando el adaptador real de usuarios.
 */
class AdapterOperationsTest {

    @Mock
    private JPARepository repository;

    @Mock
    private ObjectMapper objectMapper;

    private JPARepositoryAdapter adapter;

    private final UserEntity entity = UserEntity.builder().id("id").correoElectronico("ana@mail.com").build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new JPARepositoryAdapter(repository, objectMapper);
    }

    @Test
    void testSaveAllEntities() {
        when(repository.saveAll(anyList())).thenReturn(List.of(entity, entity));

        List<User> result = adapter.saveAllEntities(List.of(new User(), new User()));

        assertThat(result).hasSize(2).allMatch(u -> "id".equals(u.getId()));
    }

    @Test
    void testFindById() {
        when(repository.findById("id")).thenReturn(Optional.of(entity));

        User result = adapter.findById("id");

        assertThat(result.getCorreoElectronico()).isEqualTo("ana@mail.com");
    }

    @Test
    void testFindByIdNotFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThat(adapter.findById("x")).isNull();
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(entity, entity));

        assertThat(adapter.findAll()).hasSize(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFindByExample() {
        when(repository.findAll(any(Example.class))).thenReturn(List.of(entity));

        List<User> result = adapter.findByExample(User.builder().correoElectronico("ana@mail.com").build());

        assertThat(result).hasSize(1);
    }
}
