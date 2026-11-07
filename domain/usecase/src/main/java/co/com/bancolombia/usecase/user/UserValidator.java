package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exceptions.BusinessErrorType;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de validación de negocio para el registro de usuarios.
 *
 * <p>Se implementa en Java puro (sin anotaciones de frameworks) para que la regla
 * de negocio viva en el dominio y no dependa de la infraestructura.</p>
 */
final class UserValidator {

    /** Salario mínimo permitido (inclusive). */
    static final BigDecimal SALARIO_MINIMO = BigDecimal.ZERO;

    /** Salario máximo permitido (inclusive). */
    static final BigDecimal SALARIO_MAXIMO = new BigDecimal("15000000");

    /** Formato de email: usuario@dominio.ext */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UserValidator() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Valida los datos del usuario. Acumula todos los errores encontrados para
     * informarlos juntos al consumidor de la API.
     *
     * @param user usuario a validar
     * @throws BusinessException con tipo {@link BusinessErrorType#INVALID_DATA} si hay errores
     */
    static void validate(User user) {
        if (user == null) {
            throw new BusinessException(BusinessErrorType.INVALID_DATA, "Los datos del usuario son obligatorios");
        }

        List<String> errores = new ArrayList<>();

        // Criterio: nombres, apellidos, correo_electronico y salario_base no pueden ser nulos o vacíos
        if (isBlank(user.getNombres())) {
            errores.add("El campo nombres es obligatorio");
        }
        if (isBlank(user.getApellidos())) {
            errores.add("El campo apellidos es obligatorio");
        }

        // Criterio: el correo debe tener un formato de email válido
        if (isBlank(user.getCorreoElectronico())) {
            errores.add("El campo correo_electronico es obligatorio");
        } else if (!EMAIL_PATTERN.matcher(user.getCorreoElectronico().trim()).matches()) {
            errores.add("El campo correo_electronico no tiene un formato válido");
        }

        // Criterio: el salario base debe estar entre 0 y 15.000.000
        BigDecimal salario = user.getSalarioBase();
        if (salario == null) {
            errores.add("El campo salario_base es obligatorio");
        } else if (salario.compareTo(SALARIO_MINIMO) < 0 || salario.compareTo(SALARIO_MAXIMO) > 0) {
            errores.add("El campo salario_base debe estar entre 0 y 15000000");
        }

        if (!errores.isEmpty()) {
            throw new BusinessException(BusinessErrorType.INVALID_DATA, String.join("; ", errores));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
