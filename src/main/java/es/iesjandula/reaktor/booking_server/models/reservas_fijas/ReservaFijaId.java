package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase embebida que representa la clave compuesta para la entidad ReservaFija.
 * <p>
 * Contiene las referencias a Profesor, Recurso, DiaSemana y TramoHorario que
 * forman la clave primaria compuesta.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReservaFijaId implements Serializable {
	private static final long serialVersionUID = 4705657948307458266L;

	/** Profesor asociado a la reserva */
	@ManyToOne
	private Profesor profesor;

	/** Recurso asociado a la reserva */
	@ManyToOne
	@JoinColumn(name = "recurso_id", referencedColumnName = "id")
	private Recurso recurso;

	/** Día de la semana asociado a la reserva */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "dia_semana_id", referencedColumnName = "id")
	private DiaSemana diaSemana;

	/** Tramo horario asociado a la reserva */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tramo_horario_id", referencedColumnName = "id")
	private TramoHorario tramoHorario;
}
