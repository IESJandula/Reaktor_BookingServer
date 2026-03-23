package es.iesjandula.reaktor.booking_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO para la estadística de recursos más reservados.
 * <p>
 * Este DTO se utiliza para representar la estadística de recursos más reservados en el sistema.
 * </p>
 */
@Data
@AllArgsConstructor
public class EstadisticaRecursoMasReservadoDto
{
	/** Nombre o identificador del recurso */
	private String recurso;

	/** Total de reservas del recurso */
	private Long totalReservas;
}