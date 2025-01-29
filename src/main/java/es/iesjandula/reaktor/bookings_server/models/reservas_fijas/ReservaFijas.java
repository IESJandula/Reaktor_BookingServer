package es.iesjandula.reaktor.bookings_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class ReservaFijas
{

	@EmbeddedId
	private ReservasFijasId reservaId;

	@Column(nullable = false)
	private int nAlumnos;

}