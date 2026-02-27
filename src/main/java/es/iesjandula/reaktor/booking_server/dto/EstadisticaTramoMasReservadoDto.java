package es.iesjandula.reaktor.booking_server.dto;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaTramoMasReservadoDto
{
	private String diaSemana;
	private String tramoHorario;
	private Long totalReservas;

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		EstadisticaTramoMasReservadoDto other = (EstadisticaTramoMasReservadoDto) obj;

		// Comparamos por el tramo horario para saber si es el mismo registro
		return Objects.equals(tramoHorario, other.tramoHorario);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(tramoHorario);
	}
}