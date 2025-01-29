package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Manuel y Miguel
 */

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
/**
 * classroom Class
 */
public class Classroom extends Booking
{
	/**
	 * classroom location
	 */
	@Id
	private String aula;

	public Classroom(Teacher teacher, String date, String aula)
	{
		super(teacher, date);
		this.aula = aula;
	}
}
