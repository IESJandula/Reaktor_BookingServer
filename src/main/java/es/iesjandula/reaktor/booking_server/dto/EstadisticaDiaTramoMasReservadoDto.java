package es.iesjandula.reaktor.booking_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO para la estadística de días y tramos más reservados.
 * <p>
 * Este DTO se utiliza para representar la estadística de días y tramos más reservados en el sistema.
 * </p>
 */
@Data
@AllArgsConstructor
public class EstadisticaDiaTramoMasReservadoDto
{
	/** Día de la semana */
	private String diaSemana;

	/** Tramo horario */
	private String tramoHorario;

	/** Total de reservas */
	private Long totalReservas;
}
