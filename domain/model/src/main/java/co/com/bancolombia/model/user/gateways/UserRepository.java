package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.User;

/**
 * Puerto (gateway) de salida para la persistencia de usuarios.
 *
 * <p>El dominio define QUÉ necesita; la infraestructura (adaptador JPA) define CÓMO
 * se hace. De esta forma el caso de uso no depende de la base de datos.</p>
 */
public interface UserRepository {

    /**
     * Guarda un usuario de forma permanente.
     *
     * @param user usuario a registrar
     * @return el usuario guardado, incluyendo su identificador generado
     */
    User save(User user);

    /**
     * Indica si ya existe un usuario registrado con el correo electrónico dado.
     *
     * @param correoElectronico correo a verificar
     * @return {@code true} si el correo ya está registrado
     */
    boolean existsByCorreoElectronico(String correoElectronico);
}
