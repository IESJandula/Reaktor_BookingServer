package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un recurso disponible en el sistema de reservas fijas.
 * <p>
 * Contiene información sobre la cantidad, si es compartible y si está
 * bloqueado.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Recurso {

	/** Identificador único del recurso */
	@Id
	@Column(length = 150)
	private String id;

	/** Cantidad disponible del recurso */
	@Column(length = 3)
	private Integer cantidad;

	/** Indica si el recurso es compartible */
	@Column
	private boolean esCompartible;

	/** Indica si el recurso está bloqueado para su uso */
	@Column
	private boolean bloqueado;
}
