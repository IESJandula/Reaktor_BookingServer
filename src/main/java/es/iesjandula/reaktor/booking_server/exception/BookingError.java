package es.iesjandula.reaktor.booking_server.exception;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Clase que representa un error personalizado en el sistema de reservas
 * (Booking).
 * <p>
 * Extiende de {@link Exception} y añade un código de error identificativo, un
 * mensaje y una excepción original opcional para facilitar la trazabilidad y
 * manejo de errores específicos.
 * </p>
 * <p>
 * Además, permite obtener un mapa con la información del error, incluyendo el
 * stack trace en caso de que haya una excepción asociada.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BookingError extends Exception
{

	/**
	 * Identificador único de la versión serializada.
	 */
	private static final long serialVersionUID = 5216111150976621274L;

	/** Código identificativo del error */
	private int id;

	/** Mensaje descriptivo del error */
	private String message;

	/** Excepción original que causó este error, si existe */
	private Exception exception;

	/**
	 * Constructor con id, mensaje y excepción original.
	 * 
	 * @param id        código del error
	 * @param message   mensaje descriptivo
	 * @param exception excepción original que causó el error
	 */
	public BookingError(int id, String message, Exception exception)
	{
		super();
		this.id = id;
		this.message = message;
		this.exception = exception;
	}

	/**
	 * Constructor con id y mensaje, sin excepción original.
	 * 
	 * @param id      código del error
	 * @param message mensaje descriptivo
	 */
	public BookingError(int id, String message)
	{
		super();
		this.id = id;
		this.message = message;
	}

	/**
	 * Obtiene un mapa con la información del error, que incluye:
	 * <ul>
	 * <li>id: código del error</li>
	 * <li>message: mensaje del error</li>
	 * <li>exception: stack trace de la excepción original (si existe)</li>
	 * </ul>
	 * 
	 * @return mapa con detalles del error para facilitar su tratamiento o registro
	 */
	public Map<String, String> getMapError()
	{
		Map<String, String> mapError = new HashMap<String, String>();

		mapError.put("id", "" + id);
		mapError.put("message", this.message);

		if (this.exception != null)
		{
			String stacktrace = ExceptionUtils.getStackTrace(this.exception);
			mapError.put("exception", stacktrace);
		}

		return mapError;
	}
}
