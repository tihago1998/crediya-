package co.com.bancolombia.api.user.mapper;

import co.com.bancolombia.api.user.dto.UserRequestDTO;
import co.com.bancolombia.api.user.dto.UserResponseDTO;
import co.com.bancolombia.model.user.User;

/**
 * Convierte entre los DTOs de la API y el modelo de dominio {@link User}.
 *
 * <p>Mantiene al dominio aislado del formato JSON expuesto al exterior.</p>
 */
public final class UserDTOMapper {

    private UserDTOMapper() {
        // Clase utilitaria: no se instancia
    }

    /** Petición HTTP -> modelo de dominio. */
    public static User toDomain(UserRequestDTO request) {
        return User.builder()
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .fechaNacimiento(request.fechaNacimiento())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .correoElectronico(request.correoElectronico())
                .salarioBase(request.salarioBase())
                .build();
    }

    /** Modelo de dominio -> respuesta HTTP. */
    public static UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNombres(),
                user.getApellidos(),
                user.getFechaNacimiento(),
                user.getDireccion(),
                user.getTelefono(),
                user.getCorreoElectronico(),
                user.getSalarioBase()
        );
    }
}
