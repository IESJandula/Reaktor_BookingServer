package es.iesjandula.reaktor.booking_server.dto;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class EstadisticaDiaMasReservadoDto
{
	private String diaSemana;
	
	private Long totalReservas;
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		
		EstadisticaDiaMasReservadoDto other = (EstadisticaDiaMasReservadoDto) obj;
		
		return Objects.equals(diaSemana, other.diaSemana) ;
	}	
}