package es.iesjandula.reaktor.booking_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadisticaRecursoMasReservadoDto
{
	private String recurso;
	private Long totalReservas;
}