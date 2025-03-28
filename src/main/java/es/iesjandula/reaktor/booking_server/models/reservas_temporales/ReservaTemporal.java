package es.iesjandula.reaktor.booking_server.models.reservas_temporales;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class ReservaTemporal
{
	@EmbeddedId
	private ReservaTemporalId reservaTemporalId;

	@Column(nullable = false)
	private int nAlumnos;
	
	@Column(nullable = false)
	private Boolean esSemanal;
}