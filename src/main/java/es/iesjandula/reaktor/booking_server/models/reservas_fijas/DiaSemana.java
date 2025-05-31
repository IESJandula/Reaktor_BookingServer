package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un día de la semana para las reservas fijas.
 * <p>
 * Cada instancia almacena el nombre del día de la semana y un identificador
 * único.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@Entity
@NoArgsConstructor
public class DiaSemana
{

	/** Identificador único del día de la semana */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/** Nombre del día de la semana (ej. Lunes, Martes, etc.) */
	@Column(length = 10)
	private String diaSemana;
}
