package es.iesjandula.reaktor.booking_server.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una constante del sistema almacenada en la base de
 * datos.
 * <p>
 * Cada constante se identifica por una clave única y tiene un valor asociado.
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
@Table(name = "constantes")
public class Constantes
{

	/** Clave única que identifica la constante */
	@Id
	private String clave;

	/** Valor asociado a la constante */
	@Column
	private String valor;

}
