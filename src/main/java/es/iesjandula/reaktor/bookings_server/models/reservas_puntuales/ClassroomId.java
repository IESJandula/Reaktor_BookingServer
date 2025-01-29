package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ClassroomId implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8610909425495271630L;
	@ManyToOne
	private Teacher teacher;
	

}
