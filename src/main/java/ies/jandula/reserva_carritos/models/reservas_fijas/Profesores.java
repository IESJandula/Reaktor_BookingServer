package ies.jandula.reserva_carritos.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profesores
{
	@Id
	@Column(length = 100)
	private String email;

	@Column(length = 100)
	private String nombre;

	@Column(length = 150)
	private String apellidos;

}
