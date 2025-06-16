package es.iesjandula.reaktor.booking_server.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Objeto de Transferencia de Datos) que representa una reserva puntual en
 * el sistema.
 * <p>
 * Contiene la información necesaria para gestionar reservas que se realizan de
 * forma ocasional o individual, incluyendo detalles como día, tramo horario,
 * recurso, participantes y motivos.
 * </p>
 * 
 * <p>
 * Además, incluye listas que indican si la reserva es fija o semanal, mediante
 * identificadores asociados.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservasPuntualesDto
{

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

	/** Lista de identificadores que indican si la reserva está marcada como fija */
	private List<Long> esfija;

	/** Lista de motivos o cursos asociados a la reserva */
	private List<String> motivoCurso;

	/** Lista de identificadores que indican si la reserva es semanal */
	private List<Long> esSemanal;

	/**
	 * Constructor parcial que permite instanciar una reserva puntual con solo el
	 * día de la semana y el tramo horario.
	 * 
	 * @param diaSemana    día de la semana
	 * @param tramoHorario tramo horario
	 */
	public ReservasPuntualesDto(Long diaSemana, Long tramoHorario)
	{
		super();
		this.diaSemana = diaSemana;
		this.tramoHorario = tramoHorario;
	}
}
