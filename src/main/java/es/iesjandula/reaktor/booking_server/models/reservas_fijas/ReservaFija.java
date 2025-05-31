package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una reserva fija en el sistema.
 * <p>
 * Contiene el identificador compuesto de la reserva, número de alumnos, si la
 * reserva es fija y el motivo del curso asociado.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@Entity
public class ReservaFija
{

	/** Identificador compuesto de la reserva fija */
	@EmbeddedId
	private ReservaFijaId reservaFijaId;

	/** Número de alumnos para la reserva */
	@Column(nullable = false)
	private int nAlumnos;

	/** Indica si la reserva es fija */
	@Column(nullable = false)
	private boolean esFija;

	/** Motivo del curso asociado a la reserva */
	@Column(nullable = false)
	private String motivoCurso;

}
