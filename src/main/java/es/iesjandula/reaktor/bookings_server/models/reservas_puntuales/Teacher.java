package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Manuel y Miguel
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * teacher class
 */
public class Teacher
{
	@Id
	@Column(length = 100)
	private String dni;
	@Column(length = 100)
	private String name;
	@Column(length = 100)
	private String lastname;
	
}
