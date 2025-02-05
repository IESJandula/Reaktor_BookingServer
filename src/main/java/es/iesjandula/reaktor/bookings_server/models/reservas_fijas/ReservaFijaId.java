package es.iesjandula.reaktor.bookings_server.models.reservas_fijas;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
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
public class ReservaFijaId implements Serializable
{
	private static final long serialVersionUID = 4705657948307458266L;

	@ManyToOne
	private Profesor profesor;

	@ManyToOne
	@JoinColumn(name = "recurso_previo_id", referencedColumnName = "id")
	private RecursoPrevio recursoPrevio;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "dia_semana_id", referencedColumnName = "id")
	private DiaSemana diaSemana;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tramo_horario_id", referencedColumnName = "id")
	private TramoHorario tramoHorario;
}