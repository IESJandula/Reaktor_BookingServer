package es.iesjandula.reaktor.booking_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadisticaDiaTramoMasReservadoDto
{
	private String diaSemana;
	private String tramoHorario;
	private Long totalReservas;
}