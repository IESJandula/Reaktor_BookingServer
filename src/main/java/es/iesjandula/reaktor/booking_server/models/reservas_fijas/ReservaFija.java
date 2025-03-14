package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	
}