package es.iesjandula.reaktor.booking_server.models.reservas_puntuales;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Manuel y Miguel
 */

@Data
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
	private String aula;

	public Classroom(Teacher teacher, String date, String aula)
	{
		super(teacher, date);
		this.aula = aula;
	}
}
