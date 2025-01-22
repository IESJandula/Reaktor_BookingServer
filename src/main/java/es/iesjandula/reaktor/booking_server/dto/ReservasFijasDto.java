package es.iesjandula.reaktor.booking_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservasFijasDto
{
	private Long diaSemana;

	private Long tramoHorario;

	private int nAlumnos;

	private String email;

	private String nombreYapellidos;
	
	private String recurso;

	public ReservasFijasDto(Long diaSemana, Long tramoHorario) {
		super();
		this.diaSemana = diaSemana;
		this.tramoHorario = tramoHorario;

	}	
}
