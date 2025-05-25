package es.iesjandula.reaktor.booking_server.models.reservas_temporales;

import java.io.Serializable;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramoHorario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave compuesta para la entidad ReservaTemporal.
 * <p>
 * Incluye la referencia al profesor, recurso, día de la semana, tramo horario y
 * número de semana.
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
public class ReservaTemporalId implements Serializable {
	private static final long serialVersionUID = -7779056094326676793L;

	@ManyToOne
	private Profesor profesor;

	@ManyToOne
	@JoinColumn(name = "recurso_id", referencedColumnName = "id")
	private Recurso recurso;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "dia_semana_id", referencedColumnName = "id")
	private DiaSemana diaSemana;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tramo_horario_id", referencedColumnName = "id")
	private TramoHorario tramoHorario;

	@Column
	private Integer numSemana;
}
