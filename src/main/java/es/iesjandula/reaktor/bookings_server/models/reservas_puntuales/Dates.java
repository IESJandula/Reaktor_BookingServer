package es.iesjandula.reaktor.bookings_server.models.reservas_puntuales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor

public class Dates 
{
	@Id
	@Column(length =9)
	private String fechaInicio;
	@Column(length = 9)
	private String fechaFin;

}
