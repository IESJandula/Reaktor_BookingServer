package es.iesjandula.reaktor.bookings_server.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursoPrevio;
import es.iesjandula.reaktor.bookings_server.repository.IRecursoPrevioRepository;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/admin")
@RestController
@Log4j2
public class ReservasAdminRest
{
	@Autowired
	private IRecursoPrevioRepository recursosRepository;

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/resources")
	public ResponseEntity<?> crearRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "cantidad", required = true) Integer cantidad)
	{
		try
		{
			if (recursosRepository.encontrarRecurso(recurso).isPresent())
			{
				String mensajeError = "Ya existe un recurso con esos datos";

				log.error(mensajeError);
				throw new ReservaException(5, mensajeError);
			}
			RecursoPrevio recursoPrevio = new RecursoPrevio(recurso, cantidad);
			recursosRepository.saveAndFlush(recursoPrevio);

			return ResponseEntity.ok().body(recursoPrevio);
		}
		catch (ReservaException reservaException)
		{

			// Captura la excepcion personalizada y retorna un 409 ya que existe un
			// conflicto,
			// que existe un recurso con los mismos datos
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception)
		{
			// Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error inesperado al crear el recurso",
					exception);

			log.error("Error inesperado al crear el recurso: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo post para cancelar una reserva con un correo de un profesor,
	 * un recurso, un día de la semana, un tramo horario
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/resources")
	public ResponseEntity<?> eliminarRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "recurso", required = true) String recurso)
	{
		try
		{
			Optional<RecursoPrevio> optinalRecurso = this.recursosRepository.findById(recurso);

			if (!optinalRecurso.isPresent())
			{
				String mensajeError = "El recurso que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(10, mensajeError);
			}

			// Si la reserva existe en la base de datos, se borrará
			this.recursosRepository.deleteById(recurso);

			log.info("El recurso se ha borrado correctamente");
			return ResponseEntity.ok().build();

		}
		catch (ReservaException reservaException)
		{
			// Si la reserva no existe, devolverá un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception)
		{
			// Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error inesperado al borrar el recurso",
					exception);
			log.error("Error inesperado al borrar el recurso: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
}
