package es.iesjandula.reaktor.booking_server.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Objeto de Transferencia de Datos) que representa un recurso con su
 * cantidad máxima permitida.
 * <p>
 * Este objeto se utiliza para enviar o recibir información sobre la cantidad
 * máxima disponible o permitida de un determinado recurso, como puede ser un
 * aula, un equipo o cualquier otro elemento gestionado por el sistema.
 * </p>
 * 
 * <p>
 * El valor de cantidad máxima se representa con un {@link BigDecimal} para
 * permitir mayor precisión.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecursoCantMaxDto {

	/** Nombre o identificador del recurso */
	private String recurso;

	/** Cantidad máxima permitida del recurso */
	private BigDecimal cantMax;

}
