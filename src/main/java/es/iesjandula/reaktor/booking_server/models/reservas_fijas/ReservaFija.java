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
 */
@Data
@NoArgsConstructor
@Entity
public class ReservaFija
{
	@EmbeddedId
	private ReservaFijaId reservaFijaId;

	@Column(nullable = false)
	private int nAlumnos;

	@Column(nullable = false)
	private boolean esFija;

	@Column(nullable = false)
	private String motivoCurso;

	/**
	 * Fecha de creación automática. 
	 * Se usa para calcular las semanas restantes hasta fin de curso
	 */
	@Column(name = "fecha_creacion", nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime fechaCreacion;

	/**
	 * Calcula la semana del curso escolar Curso: 1 Septiembre - 30 Junio
	 */
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

		// Calcular semanas desde inicio de curso
		long semanas = java.time.temporal.ChronoUnit.WEEKS.between(inicioCurso, fecha);
		return (int) semanas + 1; // Semana 1 = primera semana de curso
	}
}