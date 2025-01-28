package es.iesjandula.reaktor.booking_server.models.reservas_puntuales;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ClassroomId 
{
	@ManyToOne
	private Teacher teacher;
	

}
