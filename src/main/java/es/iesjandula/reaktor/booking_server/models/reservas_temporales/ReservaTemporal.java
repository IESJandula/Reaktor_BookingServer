package es.iesjandula.reaktor.booking_server.models.reservas_temporales;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa una reserva temporal en el sistema.
 * <p>
 * Contiene la clave compuesta para identificar la reserva, el número de
 * alumnos, el motivo del curso y si la reserva es semanal.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@Entity
public class ReservaTemporal {
	@EmbeddedId
	private ReservaTemporalId reservaTemporalId;

	@Column(nullable = false)
	private int nAlumnos;

	@Column(nullable = false)
	private String motivoCurso;

	@Column(nullable = false)
	private boolean esSemanal;
}
