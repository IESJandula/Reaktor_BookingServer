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
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursoFinal;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursoPrevio;
import es.iesjandula.reaktor.bookings_server.repository.IRecursoFinalRepository;
import es.iesjandula.reaktor.bookings_server.repository.IRecursoPrevioRepository;
import es.iesjandula.reaktor.bookings_server.utils.Constants;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/admin")
@RestController
@Log4j2
public class ReservasAdminRest
{
	@Autowired
	private IRecursoPrevioRepository recursoPrevioRepository;

	@Autowired
	private IRecursoFinalRepository recursoFinalRepository;

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/resources")
	public ResponseEntity<?> crearRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "switchStatus", required = true) boolean switchStatus,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "cantidad", required = true) Integer cantidad)
	{
		try
		{
			if (recursoPrevioRepository.encontrarRecurso(recurso).isPresent())
			{
				String mensajeError = "Ya existe un recurso con esos datos";

				log.error(mensajeError);
				throw new ReservaException(Constants.RECURSO_YA_EXISTE, mensajeError);
			}

			if (recursoFinalRepository.encontrarRecurso(recurso).isPresent())
			{
				String mensajeError = "Ya existe un recurso con esos datos";

				log.error(mensajeError);
				throw new ReservaException(Constants.RECURSO_YA_EXISTE, mensajeError);
			}

			// Comprobación del tipo de recurso
			if (switchStatus)
			{
				RecursoFinal recursoFinal = new RecursoFinal(recurso, cantidad);
				recursoFinalRepository.saveAndFlush(recursoFinal);
				return ResponseEntity.ok().body(recursoFinal);
			}
			else
			{
				RecursoPrevio recursoPrevio = new RecursoPrevio(recurso, cantidad);
				recursoPrevioRepository.saveAndFlush(recursoPrevio);
				return ResponseEntity.ok().body(recursoPrevio);
			}

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
			@RequestHeader(value = "switchStatus", required = true) boolean switchStatus,
			@RequestHeader(value = "recurso", required = true) String recurso)
	{
		try
		{
			Optional<RecursoFinal> optinalRecursoFinal = this.recursoFinalRepository.findById(recurso);
			Optional<RecursoPrevio> optinalRecursoPrevio = this.recursoPrevioRepository.findById(recurso);

			if (!optinalRecursoFinal.isPresent() && switchStatus)
			{
				String mensajeError = "El recurso que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_ELIMINANDO_RECURSO, mensajeError);
			}
			if (!optinalRecursoPrevio.isPresent() && !switchStatus)
			{
				String mensajeError = "El recurso que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_ELIMINANDO_RECURSO, mensajeError);
			}

			if (switchStatus)
			{
				// Si la reserva existe en la base de datos, se borrará
				this.recursoFinalRepository.deleteById(recurso);
			}
			else
			{
				// Si la reserva existe en la base de datos, se borrará
				this.recursoPrevioRepository.deleteById(recurso);
			}

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
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO, "Error inesperado al borrar el recurso",
					exception);
			log.error("Error inesperado al borrar el recurso: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
}
