package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Classroom2 
{
	@EmbeddedId
	private ClassroomId classroomId;
	@Column(length = 100)
	private String date;
	@Column(length = 100)
	private String aula;
	
	

}
