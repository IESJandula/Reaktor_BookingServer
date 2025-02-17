package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import java.io.Serializable;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.Profesor;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.TramoHorario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReservaPuntualId implements Serializable
{


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