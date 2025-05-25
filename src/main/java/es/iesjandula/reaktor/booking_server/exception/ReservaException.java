package es.iesjandula.reaktor.booking_server.exception;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Clase que representa una excepción personalizada para errores relacionados
 * con reservas.
 * <p>
 * Extiende de {@link Exception} y añade un código de error, mensaje descriptivo
 * y la excepción original opcional.
 * </p>
 * <p>
 * Proporciona un método para obtener un mapa con la información del error,
 * ideal para mostrar mensajes ordenados al usuario.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
public class ReservaException extends Exception {

	/** Serial Version UID para serialización */
	private static final long serialVersionUID = 4514190388643824325L;

	/** Código identificativo del error */
	private int code;

	/** Mensaje descriptivo del error */
	private String message;

	/** Excepción original que causó este error, si existe */
	private Exception exception;

	/**
	 * Constructor con código y mensaje.
	 * 
	 * @param code    código del error
	 * @param message mensaje descriptivo del error
	 */
	public ReservaException(int code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	/**
	 * Constructor con código, mensaje y excepción original.
	 * 
	 * @param code      código del error
	 * @param message   mensaje descriptivo del error
	 * @param exception excepción original que causó el error
	 */
	public ReservaException(int code, String message, Exception exception) {
		super(message, exception);
		this.code = code;
		this.message = message;
		this.exception = exception;
	}

	/**
	 * Obtiene un mapa con la información del error para mostrarla ordenadamente al
	 * usuario.
	 * <ul>
	 * <li>code: código del error</li>
	 * <li>message: mensaje del error</li>
	 * <li>stackTrace: traza de pila de la excepción original (si existe)</li>
	 * </ul>
	 * 
	 * @return mapa con detalles del error
	 */
	public Map<String, String> getBodyMesagge() {
		Map<String, String> getBodyMesagge = new HashMap<>();

		getBodyMesagge.put("code", String.valueOf(code));
		getBodyMesagge.put("message", message);

		if (this.exception != null) {
			String stackTrace = ExceptionUtils.getStackTrace(this.exception);
			getBodyMesagge.put("stackTrace", stackTrace);
		}
		return getBodyMesagge;
	}

}
