package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa a un profesor en el sistema de reservas fijas.
 * <p>
 * Contiene información básica como email, nombre y apellidos.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profesor {

	/** Email único del profesor, que actúa como identificador */
	@Id
	@Column(length = 100)
	private String email;

	/** Nombre del profesor */
	@Column(length = 100)
	private String nombre;

	/** Apellidos del profesor */
	@Column(length = 150)
	private String apellidos;
}
