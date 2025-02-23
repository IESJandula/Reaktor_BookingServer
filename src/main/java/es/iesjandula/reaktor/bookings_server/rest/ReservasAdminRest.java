package es.iesjandula.reaktor.bookings_server.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import es.iesjandula.reaktor.bookings_server.dto.RecursoCantMaxDto;
import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.bookings_server.repository.IRecursoRepository;
import es.iesjandula.reaktor.bookings_server.repository.IReservaRepository;
import es.iesjandula.reaktor.bookings_server.repository.reservas_puntuales.IReservaPuntualRepository;
import es.iesjandula.reaktor.bookings_server.utils.Constants;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/admin")
@RestController
@Log4j2
public class ReservasAdminRest
{
	@Autowired
	private IRecursoRepository recursoRepository;
	
	@Autowired
	private IReservaPuntualRepository reservaPuntualRepository;
	
	@Autowired
	private IReservaRepository reservaRepository;
	


	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/resources")
	public ResponseEntity<?> crearRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "esCompartible", required = true) boolean esCompartible,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "cantidad", required = true) Integer cantidad)
	{
		try
		{
			Recurso recursoFinal = new Recurso(recurso, cantidad, esCompartible);

			if (this.recursoRepository.encontrarRecurso(recurso).isPresent())
			{
				Optional<Recurso> recursoOptional = this.recursoRepository.encontrarRecurso(recurso);
				Recurso recursoAntiguo = recursoOptional.get();
				
				if (recursoAntiguo.getCantidad() == recursoFinal.getCantidad())
				{
					String mensajeError = "Ya existe un recurso con esos datos: " + recurso;
					log.error(mensajeError);
					throw new ReservaException(Constants.RECURSO_YA_EXISTE, mensajeError);
				}

				recursoFinal = new Recurso(recursoAntiguo.getId(), cantidad, esCompartible);
			}
			else
			{
				recursoFinal = new Recurso(recurso, cantidad, esCompartible);
			}

			this.recursoRepository.saveAndFlush(recursoFinal);

			log.info("El recurso se ha creado correctamente: " + recurso);

			return ResponseEntity.ok().body(recursoFinal);
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
			Optional<Recurso> optinalRecurso = this.recursoRepository.findById(recurso);

			if (!optinalRecurso.isPresent())
			{
				String mensajeError = "El recurso que quiere borrar no existe: " + recurso;
				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_ELIMINANDO_RECURSO, mensajeError);
			}

			// Si la reserva existe en la base de datos, se borrará
			this.recursoRepository.deleteById(recurso);

			log.info("El recurso se ha borrado correctamente: " + recurso);
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
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error inesperado al borrar el recurso", exception);
			log.error("Error inesperado al borrar el recurso: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/resources/cantMax")
	public ResponseEntity<?> obtenerCantidadMaximaRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario)
	{
		try
		{
			List<Object[]> reservaFijaMax = this.reservaRepository.reservaFijaMax();
			List<Object[]> reservaPuntualMax = this.reservaPuntualRepository.reservaPuntualMax();
			
			List<RecursoCantMaxDto> listaRecursoFija = new ArrayList<RecursoCantMaxDto>();
			List<RecursoCantMaxDto> listaRecursoPuntuales = new ArrayList<RecursoCantMaxDto>();
			
			List<RecursoCantMaxDto> listaFinal = new ArrayList<RecursoCantMaxDto>();
			
			for (Object[] reservaPuntual : reservaPuntualMax)
			{
				RecursoCantMaxDto cantMaxDto = new RecursoCantMaxDto();
				cantMaxDto.setRecurso((String)reservaPuntual[0]);
				cantMaxDto.setCantMax((BigDecimal) reservaPuntual[1]);
				listaRecursoPuntuales.add(cantMaxDto);
			}
			
			for (Object[] reservaFija : reservaFijaMax)
			{
				RecursoCantMaxDto cantMaxDto = new RecursoCantMaxDto();
				cantMaxDto.setRecurso((String)reservaFija[0]);
				cantMaxDto.setCantMax((BigDecimal) reservaFija[1]);
				listaRecursoFija.add(cantMaxDto);
			}
			
			for (RecursoCantMaxDto puntual : listaRecursoPuntuales)
			{
				for (RecursoCantMaxDto fija: listaRecursoFija)
				{
					if(fija.getRecurso().equals(puntual.getRecurso()))
					{
						Integer cantidadPuntual = puntual.getCantMax().intValue();
						Integer cantidadFija = fija.getCantMax().intValue();
						if(cantidadPuntual > cantidadFija)
						{
							listaFinal.add(puntual);
						}
						else
						{
							listaFinal.add(fija);
						}
					}
					else if(listaRecursoFija.contains(puntual) && !listaRecursoPuntuales.contains(puntual))
					{
						listaFinal.add(puntual);
					}
					else
					{
						listaFinal.add(fija);
					}
				}
			}			
			HashMap<String, BigDecimal> mapaFinal = new HashMap<>();
			for (RecursoCantMaxDto recursoFinal : listaFinal)
			{
				mapaFinal.put(recursoFinal.getRecurso(), recursoFinal.getCantMax());
			}
			
			return ResponseEntity.ok().body(mapaFinal);
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
}
