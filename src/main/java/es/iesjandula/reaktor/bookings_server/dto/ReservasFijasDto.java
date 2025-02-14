package es.iesjandula.reaktor.bookings_server.dto;

import java.util.List;

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

	private List<Integer> nAlumnos;

	private List<String> email;

	private List<String> nombreYapellidos;

	private String recurso;

	private Integer plazasRestantes;

	public ReservasFijasDto(Long diaSemana, Long tramoHorario)
	{
		super();
		this.diaSemana = diaSemana;
		this.tramoHorario = tramoHorario;

	}
}
