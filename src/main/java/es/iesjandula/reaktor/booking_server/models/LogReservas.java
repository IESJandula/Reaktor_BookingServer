package es.iesjandula.reaktor.booking_server.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un registro de log de reservas en el sistema.
 * <p>
 * Cada instancia guarda información sobre una acción realizada sobre una
 * reserva, incluyendo la fecha, usuario que realizó la acción, tipo de acción,
 * recurso afectado y otros detalles relevantes.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log_reservas")
public class LogReservas
{

	/** Fecha y hora en la que se realizó la acción */
	@Id
	private Date fecha;

	/** Usuario que realizó la acción */
	@Column
	private String usuario;

	/** Acción realizada sobre la reserva */
	@Column
	private String accion;

	/** Tipo de la acción */
	@Column
	private String tipo;

	/** Recurso afectado por la acción */
	@Column
	private String recurso;

	/** Localización o descripción de la reserva */
	@Column
	private String locReserva;

	/** Usuario con privilegios de superusuario que realizó la acción */
	@Column
	private String superusuario;

	/** Número identificador del registro */
	@Column
	private Long numRegistro;

	/** Número máximo relacionado con la acción (contexto variable) */
	@Column
	private Long countMax;

	/**
	 * Constructor simplificado sin los campos numRegistro y countMax.
	 * 
	 * @param fecha        Fecha y hora de la acción
	 * @param usuario      Usuario que realizó la acción
	 * @param accion       Acción realizada
	 * @param tipo         Tipo de acción
	 * @param recurso      Recurso afectado
	 * @param locReserva   Localización o descripción de la reserva
	 * @param superusuario Usuario con privilegios de superusuario
	 */
	public LogReservas(Date fecha, String usuario, String accion, String tipo, String recurso, String locReserva,
			String superusuario)
	{
		super();
		this.fecha = fecha;
		this.usuario = usuario;
		this.accion = accion;
		this.tipo = tipo;
		this.recurso = recurso;
		this.locReserva = locReserva;
		this.superusuario = superusuario;
	}
}
