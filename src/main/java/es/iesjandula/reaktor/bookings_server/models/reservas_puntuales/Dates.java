package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor

public class Dates 
{
	@Column(length =9)
	private String fechaInicio;
	@Column(length = 9)
	private String fechaFin;

}
