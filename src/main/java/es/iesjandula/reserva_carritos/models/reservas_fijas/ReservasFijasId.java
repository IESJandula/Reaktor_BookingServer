package es.iesjandula.reserva_carritos.models.reservas_fijas;

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
public class ReservasFijasId implements Serializable
{
	private static final long serialVersionUID = 4705657948307458266L;

	@ManyToOne
	private Profesores profesor;

	@ManyToOne
	@JoinColumn(name = "recursos_aula_y_carritos", referencedColumnName = "aulaYCarritos")
	private RecursosPrevios aulaYCarritos;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "dias_de_la_semana_id", referencedColumnName = "id")
	private DiasSemana diasDeLaSemana;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tramos_horarios_id", referencedColumnName = "id")
	private TramosHorarios tramosHorarios;

}