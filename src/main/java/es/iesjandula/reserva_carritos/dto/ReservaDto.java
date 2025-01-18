package es.iesjandula.reserva_carritos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaDto
{
	private Long diaSemana;

	private Long tramoHorario;

	private int nAlumnos;

	private String email;

	private String nombreYapellidos;
	
	private String recurso;

	public ReservaDto(Long diaSemana, Long tramoHorario) {
		super();
		this.diaSemana = diaSemana;
		this.tramoHorario = tramoHorario;

	}	
}
