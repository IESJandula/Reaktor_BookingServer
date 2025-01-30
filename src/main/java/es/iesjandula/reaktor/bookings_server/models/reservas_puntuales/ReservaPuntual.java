package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.ReservaFijaId;
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
	private ReservaFijaId reservaId;

	@Column(nullable = false)
	private int nAlumnos;
	
	@Column(nullable = false)
	private int nSemana;

}
