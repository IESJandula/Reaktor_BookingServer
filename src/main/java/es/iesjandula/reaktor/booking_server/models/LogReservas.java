package es.iesjandula.reaktor.booking_server.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.AllArgsConstructor;
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
	/**
	 * Número identificador del registro
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Usuario que realizó la acción
	 */
	@Column
	private String usuario;

	/**
	 * Acción realizada sobre la reserva
	 */
	@Column
	private String accion;

	/**
	 * Tipo de la acción
	 */
	@Column
	private String tipo;

	/**
	 * Recurso afectado por la acción
	 */
	@Column
	private String recurso;

	/**
	 * Fecha de la reserva
	 */
	@Column
	private Date fechaReserva;

	/**
	 * Día de la semana de la reserva
	 */
	@Column
	private String diaSemana;

	/**
	 * Tramo horario de la reserva
	 */
	@Column
	private String tramoHorario;

	/**
	 * Usuario con privilegios de superusuario que realizó la acción
	 */
	@Column
	private String superUsuario;
}
