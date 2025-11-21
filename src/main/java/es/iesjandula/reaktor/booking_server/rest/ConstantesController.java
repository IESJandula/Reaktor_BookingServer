package es.iesjandula.reaktor.booking_server.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.booking_server.dto.DtoConstantes;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.Constantes;
import es.iesjandula.reaktor.booking_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;

/**
 * Controlador REST para gestionar las constantes del sistema. Permite obtener y
 * actualizar las constantes almacenadas en la base de datos.
 *
 * Funcionalidades principales:
 * <ul>
 * <li>Obtener la lista de constantes como objetos DTO mediante método GET.</li>
 * <li>Actualizar o insertar nuevas constantes en la base de datos mediante
 * método POST.</li>
 * </ul>
 *
 * Solo los usuarios con roles de administrador o dirección pueden acceder a
 * estos endpoints, gracias a la protección mediante anotaciones de seguridad
 * (@PreAuthorize).
 *
 * Las respuestas son entregadas en formato JSON y cualquier excepción es
 * gestionada devolviendo una respuesta adecuada con código de error y mensaje
 * personalizado.
 *
 * Ruta base del controlador: {@code /bookings/constants}
 *
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@RequestMapping(value = "/bookings/constants", produces =
{ "application/json" })
@RestController
public class ConstantesController
{
	/**
	 * Logger of the class
	 */
	private static final Logger log = LoggerFactory.getLogger(ConstantesController.class);

	@Autowired
	private ConstantesRepository constanteRepository;

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> actualizarConstantes()
	{
		try
		{
			List<DtoConstantes> dtoConstantesList = this.constanteRepository.encontrarTodoComoDto();

			return ResponseEntity.ok(dtoConstantesList);
		}
		catch (Exception exception)
		{

			ReservaException reservaException = new ReservaException(Constants.CONSTANTE_NO_ENCONTRADA,
					"Excepción genérica al obtener las costantes", exception);

			log.error("Excepción genérica al obtener las costantes", reservaException);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> actualizarConstantes(@RequestBody(required = true) List<DtoConstantes> dtoConstantesList)
	{
		try
		{
			for (DtoConstantes dtoConstantes : dtoConstantesList)
			{
				Constantes constantes = new Constantes(dtoConstantes.getClave(), dtoConstantes.getValor());

				this.constanteRepository.saveAndFlush(constantes);
			}

			return ResponseEntity.ok().build();
		}
		catch (Exception exception)
		{
			ReservaException reservaException = new ReservaException(Constants.CONSTANTE_NO_ENCONTRADA,
					"Excepción genérica al obtener las costantes", exception);

			log.error("Excepción genérica al actualizar las costantes");
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

}
