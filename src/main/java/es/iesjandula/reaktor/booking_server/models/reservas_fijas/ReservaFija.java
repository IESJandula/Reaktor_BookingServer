package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una reserva fija en el sistema.
 * <p>
 * Contiene el identificador compuesto de la reserva, número de alumnos, si la
 * reserva es fija y el motivo del curso asociado.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@Entity
public class ReservaFija
{

	/** Identificador compuesto de la reserva fija */
	@EmbeddedId
	private ReservaFijaId reservaFijaId;

	/** Número de alumnos para la reserva */
	@Column(nullable = false)
	private int nAlumnos;

	/** Indica si la reserva es fija */
	@Column(nullable = false)
	private boolean esFija;

	/** Motivo del curso asociado a la reserva */
	@Column(nullable = false)
	private String motivoCurso;

	/** Fecha de creación de la reserva fija para calcular la semana del curso */
	@Column(name = "fecha_creacion", nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime fechaCreacion;

	/** Método para calcular la semana del curso escolar */
	public Integer getSemanaCurso()
	{
		if (this.fechaCreacion == null)
		{
			return null;
		}

		java.time.LocalDate fecha = this.fechaCreacion.toLocalDate();
		java.time.LocalDate inicioCurso = java.time.LocalDate.of(fecha.getYear(), 9, 1);

		// Si es antes de septiembre, el curso empezó el año anterior
		if (fecha.getMonthValue() < 9)
		{
			inicioCurso = java.time.LocalDate.of(fecha.getYear() - 1, 9, 1);
		}

		// Calcular las semanas desde el inicio de curso
		long semanas = java.time.temporal.ChronoUnit.WEEKS.between(inicioCurso, fecha);
		return (int) semanas + 1; // Semana 1 = primera semana de curso
	}
}
