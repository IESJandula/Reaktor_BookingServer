package ies.jandula.reserva_carritos.models.reservas_puntuales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class RecursosActuales 
{
	@Id
	@Column(length = 9)
	private String nombre;
	@Column(nullable = false)
	private int capacidad;

}
