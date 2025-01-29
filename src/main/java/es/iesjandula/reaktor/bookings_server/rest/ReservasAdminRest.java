package es.iesjandula.reaktor.bookings_server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursosPrevios;
import es.iesjandula.reaktor.bookings_server.repository.IRecursosRepository;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/admin")
@RestController
@Log4j2
public class ReservasAdminRest
{
	@Autowired
	private IRecursosRepository recursosRepository;	
	
	/**
	 * Endpoint de tipo post para añadir un recurso con un recurso
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/resources")
	public ResponseEntity<?> implementarRecurso(@RequestHeader(value = "recursos", required = true) String recursos)
	{

		try
		{
//			Comprueba si existe un recurso con esos datos
			if (this.recursosRepository.existsById(recursos))
			{

				String mensajeError = "El recurso que quieres añadir ya existe";
				log.error(mensajeError);
				throw new ReservaException(5, mensajeError);
			}

			RecursosPrevios nuevoRecurso = new RecursosPrevios();
			nuevoRecurso.setAulaYCarritos(recursos);

//			Si no existen esos recursos, se guardaran en base de datos
			this.recursosRepository.saveAndFlush(nuevoRecurso);

			return ResponseEntity.ok().build();
		} 
		catch (ReservaException reservaException)
		{
//			Captura la excepcion personalizada y retorna un 409 ya que existe un conflicto,
//			que existe un recurso con los mismos datos
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		} 
		catch (Exception exception)
		{
//			Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(
					100, "Error inesperado al añadir un recurso", exception
			);
			log.error("Error inesperado al añadir un recurso: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint que recive un nombre de recurso y lo borra de la tabla de recursos
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/resources")
	public ResponseEntity<?> borrarRecurso(@RequestHeader(value = "recursos", required = true) String recursos)
	{

		try
		{
//			Comprueba si existe un recurso con esos datos
			if (!this.recursosRepository.existsById(recursos))
			{

				String mensajeError = "El recurso que quieres eliminar no existe";
				log.error(mensajeError);
				throw new ReservaException(5, mensajeError);
			}

			RecursosPrevios nuevoRecurso = new RecursosPrevios();
			nuevoRecurso.setAulaYCarritos(recursos);

//			Si no existen esos recursos, se guardaran en base de datos
			this.recursosRepository.delete(nuevoRecurso);

			return ResponseEntity.ok().build();
		} catch (ReservaException reservaException)
		{
//			Captura la excepcion personalizada y retorna un 409 ya que existe un conflicto,
//			que existe un recurso con los mismos datos
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
//			Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(
					100, "Error inesperado al añadir un recurso", exception
			);

			log.error("Error inesperado al añadir un recurso: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
}
