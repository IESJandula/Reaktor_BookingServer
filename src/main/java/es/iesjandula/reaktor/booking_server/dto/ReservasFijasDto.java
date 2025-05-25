package es.iesjandula.reaktor.booking_server.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Objeto de Transferencia de Datos) que representa una reserva fija en el
 * sistema.
 * <p>
 * Este objeto encapsula los datos necesarios para gestionar una reserva
 * recurrente en un día y tramo horario determinados. Se incluye información
 * como el recurso reservado, número de alumnos, correos electrónicos, nombres y
 * apellidos, motivo del curso, y plazas restantes.
 * </p>
 * 
 * <p>
 * También puede utilizarse para consultar o filtrar reservas fijas en función
 * del día de la semana y el tramo horario.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservasFijasDto {

	/** Día de la semana asociado a la reserva (por ejemplo, 1 = lunes) */
	private Long diaSemana;

	/** Tramo horario de la reserva (por ejemplo, 2 = segunda hora del día) */
	private Long tramoHorario;

	/** Lista con el número de alumnos participantes en la reserva */
	private List<Integer> nAlumnos;

	/**
	 * Lista de direcciones de correo electrónico de los participantes o
	 * responsables
	 */
	private List<String> email;

	/** Lista de nombres y apellidos de los participantes o responsables */
	private List<String> nombreYapellidos;

	/** Nombre del recurso reservado */
	private String recurso;

	/** Número de plazas que aún quedan disponibles en el recurso reservado */
	private Integer plazasRestantes;

	/** Lista de motivos o cursos asociados a la reserva */
	private List<String> motivoCurso;

	/**
	 * Constructor parcial que permite instanciar una reserva con solo el día de la
	 * semana y el tramo horario.
	 * 
	 * @param diaSemana    día de la semana
	 * @param tramoHorario tramo horario
	 */
	public ReservasFijasDto(Long diaSemana, Long tramoHorario) {
		super();
		this.diaSemana = diaSemana;
		this.tramoHorario = tramoHorario;
	}
}
