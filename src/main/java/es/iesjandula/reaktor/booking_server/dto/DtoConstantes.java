package es.iesjandula.reaktor.booking_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Objeto de Transferencia de Datos) utilizado para representar constantes
 * del sistema.
 * <p>
 * Este objeto se emplea en operaciones donde se requiere transferir información
 * sobre constantes entre diferentes capas de la aplicación, como por ejemplo
 * entre el backend y el frontend.
 * </p>
 * 
 * <p>
 * Cada constante está compuesta por una clave identificadora y su
 * correspondiente valor.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoConstantes {

	/** Clave identificadora de la constante */
	private String clave;

	/** Valor asociado a la constante */
	private String valor;
}
