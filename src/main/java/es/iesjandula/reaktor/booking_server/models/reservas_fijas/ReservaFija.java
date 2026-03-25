package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
	 * Método para calcular la semana del curso escolar
	 * @return La semana del curso escolar
	 */
	public Integer getSemanaCurso()
	{
		Integer outcome = null;

		if (this.fechaCreacion != null)
		{
			// Obtenemos la fecha de creación de la reserva fija y la convertimos a LocalDate
			LocalDate fecha = this.fechaCreacion.toLocalDate();

			// Obtenemos la fecha de inicio del curso escolar
			LocalDate inicioCurso = LocalDate.of(fecha.getYear(), 9, 1);

			// Si es antes de septiembre ... 
			if (fecha.getMonthValue() < 9)
			{
				// .. el curso empezó el año anterior
				inicioCurso = LocalDate.of(fecha.getYear() - 1, 9, 1);
			}

			// Calculamos las semanas desde el inicio de curso
			long semanas = ChronoUnit.WEEKS.between(inicioCurso, fecha);

			// Convertimos el resultado a Integer y le sumamos 1,
			// porque la semana 1 es la primera semana de curso
			outcome = (int) semanas + 1;
		}

		return outcome;
	}
}
