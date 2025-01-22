package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class ReservaPuntual 
{
	@EmbeddedId
	private ReservasFijasId reservaId;

	@Column(nullable = false)
	private int nAlumnos;
	
	@Column(nullable = false)
	private int nSemana;

}
