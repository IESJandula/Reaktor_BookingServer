package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Recurso
{
	@Id
	@Column(length = 150)
	private String id;

	@Column(length = 3)
	private Integer cantidad;

	@Column
	private boolean esCompartible;
}