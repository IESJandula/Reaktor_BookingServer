package es.iesjandula.reaktor.bookings_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class RecursoPrevio
{
	@Id
	@Column(length = 30)
	private String id ;
	
	@Column(length = 3)
	private Integer cantidad;
}