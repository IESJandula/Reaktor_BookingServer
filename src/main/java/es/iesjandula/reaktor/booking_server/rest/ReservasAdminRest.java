package es.iesjandula.reaktor.booking_server.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.booking_server.dto.RecursoCantMaxDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.LogReservas;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.booking_server.repository.IRecursoRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaFijaRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaTemporalRepository;
import es.iesjandula.reaktor.booking_server.repository.LogReservasRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;

/**
 * Controlador REST para operaciones administrativas relacionadas con la gestión
 * de recursos y reservas en el sistema de reservas.
 * 
 * <p>
 * Permite a usuarios con roles de administrador o dirección realizar acciones
 * como crear, eliminar o modificar recursos, obtener estadísticas de uso,
 * revisar logs de reservas, entre otros.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@RequestMapping(value = "/bookings/admin")
@RestController
public class ReservasAdminRest
{
	/**
	 * Logger of the class
	 */
	private static final Logger log = LoggerFactory.getLogger(ReservasAdminRest.class);

	@Autowired
	private IRecursoRepository recursoRepository;

	@Autowired
	private IReservaTemporalRepository reservaTemporalRepository;

	@Autowired
	private LogReservasRepository logReservasRepository;

	@Autowired
	private IReservaFijaRepository reservaFijaRepository;

	/**
	 * Endpoint para crear o actualizar un recurso. Si el recurso ya existe con la
	 * misma cantidad y configuración de compartibilidad, se lanza una excepción. Si
	 * el recurso existe pero con diferentes parámetros, se actualiza.
	 * 
	 * @param usuario       Usuario autenticado que realiza la operación
	 * @param esCompartible Indica si el recurso puede ser compartido
	 * @param recurso       Nombre del recurso
	 * @param cantidad      Cantidad del recurso
	 * @return ResponseEntity con el recurso creado o actualizado
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/resources")
	public ResponseEntity<?> crearRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "esCompartible", required = true) boolean esCompartible,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "cantidad", required = true) Integer cantidad)
	{
		try
		{
			Recurso recursoFinal = new Recurso(recurso, cantidad, esCompartible, false);

			if (this.recursoRepository.encontrarRecurso(recurso).isPresent())
			{
				Optional<Recurso> recursoOptional = this.recursoRepository.encontrarRecurso(recurso);
				Recurso recursoAntiguo = recursoOptional.get();

				if (recursoAntiguo.getCantidad() == recursoFinal.getCantidad())
				{
					if (recursoAntiguo.isEsCompartible() == recursoFinal.isEsCompartible())
					{
						String mensajeError = "Ya existe un recurso con esos datos: " + recurso;
						log.error(mensajeError);
						throw new ReservaException(Constants.RECURSO_YA_EXISTE, mensajeError);
					}
				}

				recursoFinal = new Recurso(recursoAntiguo.getId(), cantidad, esCompartible, false);
			}
			else
			{
				recursoFinal = new Recurso(recurso, cantidad, esCompartible, false);
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
			String mensajeError = "Error inesperado al crear el recurso";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(100, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint para eliminar un recurso si existe.
	 * 
	 * @param usuario Usuario autenticado que realiza la operación
	 * @param recurso Nombre del recurso a eliminar
	 * @return ResponseEntity con estado OK si se elimina correctamente, o error si
	 *         no existe
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
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

			// Si el recurso existe en la base de datos, se borrará
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
			String mensajeError = "Error inesperado al borrar el recurso";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Verifica si un recurso tiene reservas asociadas antes de intentar eliminarlo.
	 * 
	 * @param usuario Usuario autenticado
	 * @param recurso Nombre del recurso a comprobar
	 * @return ResponseEntity con un booleano indicando si el recurso puede ser
	 *         eliminado
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/checkDelete")
	public ResponseEntity<?> comprobarEliminacionRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "recurso", required = true) String recurso)
	{
		try
		{
			boolean borrado = true;

			List<String> lista = this.recursoRepository.encontrarReservasPorRecurso(recurso);

			if (!lista.isEmpty())
			{
				borrado = false;
				String mensajeError = "El recurso que quiere borrar tiene reservas: " + recurso;
				log.error(mensajeError);
			}

			return ResponseEntity.ok().body(borrado);
		}
		catch (Exception exception)
		{
			String mensajeError = "Error inesperado al comprobar el borrado de recurso";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Obtiene la cantidad máxima reservada para cada recurso combinando reservas
	 * fijas y temporales.
	 * 
	 * @param usuario Usuario autenticado
	 * @return ResponseEntity con un mapa de recurso y su cantidad máxima total
	 *         reservada
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/resources/cantMax")
	public ResponseEntity<?> obtenerCantidadMaximaRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario)
	{
		try
		{
			List<Object[]> reservaFijaMax = this.reservaFijaRepository.reservaFijaMax();
			List<Object[]> reservaTemporalMax = this.reservaTemporalRepository.reservaTemporalMax();

			List<RecursoCantMaxDto> listaRecursoFija = new ArrayList<RecursoCantMaxDto>();
			List<RecursoCantMaxDto> listaRecursoPuntuales = new ArrayList<RecursoCantMaxDto>();

			List<RecursoCantMaxDto> listaFinal = new ArrayList<RecursoCantMaxDto>();

			for (Object[] reservaTemporal : reservaTemporalMax)
			{
				RecursoCantMaxDto cantMaxDto = new RecursoCantMaxDto();
				cantMaxDto.setRecurso((String) reservaTemporal[0]);
				cantMaxDto.setCantMax((BigDecimal) reservaTemporal[1]);
				listaRecursoPuntuales.add(cantMaxDto);
			}

			for (Object[] reservaFija : reservaFijaMax)
			{
				RecursoCantMaxDto cantMaxDto = new RecursoCantMaxDto();
				cantMaxDto.setRecurso((String) reservaFija[0]);
				cantMaxDto.setCantMax((BigDecimal) reservaFija[1]);
				listaRecursoFija.add(cantMaxDto);
			}

			if (!listaRecursoPuntuales.isEmpty() && !listaRecursoFija.isEmpty())
			{
				for (RecursoCantMaxDto puntual : listaRecursoPuntuales)
				{
					for (RecursoCantMaxDto fija : listaRecursoFija)
					{
						if (fija.getRecurso().equals(puntual.getRecurso()))
						{
							Integer cantidadPuntual = puntual.getCantMax().intValue();
							Integer cantidadFija = fija.getCantMax().intValue();
							BigDecimal suma;
							if (cantidadPuntual > cantidadFija)
							{
								suma = puntual.getCantMax().add(fija.getCantMax());
								puntual.setCantMax(suma);
								listaFinal.add(puntual);
							}
							else
							{
								suma = puntual.getCantMax().add(fija.getCantMax());
								fija.setCantMax(suma);
								listaFinal.add(fija);
							}
						}
						else if (listaRecursoFija.contains(puntual) && !listaRecursoPuntuales.contains(puntual))
						{
							listaFinal.add(puntual);
						}
						else
						{
							listaFinal.add(fija);
						}
					}
				}
			}
			else
			{

				if (listaRecursoPuntuales.isEmpty())
				{
					for (RecursoCantMaxDto fija : listaRecursoFija)
					{
						listaFinal.add(fija);
					}
				}

				if (listaRecursoFija.isEmpty())
				{
					for (RecursoCantMaxDto puntual : listaRecursoPuntuales)
					{
						listaFinal.add(puntual);
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
			String mensajeError = "Error inesperado al crear el recurso";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(100, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Elimina todas las reservas (fijas y temporales) asociadas a un recurso
	 * específico.
	 * 
	 * @param usuario Usuario autenticado
	 * @param recurso Nombre del recurso del cual se eliminarán todas las reservas
	 * @return ResponseEntity con estado OK si las reservas son eliminadas
	 *         exitosamente
	 */
	@Modifying
	@Transactional
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/resources/bookings")
	public ResponseEntity<?> eliminarReservasRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "recurso", required = true) String recurso)
	{
		try
		{
			this.reservaFijaRepository.deleteReservas(recurso);
			this.reservaTemporalRepository.deleteReservas(recurso);

			log.info("Las reservas del recurso se han borrado correctamente: " + recurso);
			return ResponseEntity.ok().build();

		}
		catch (Exception exception)
		{
			String mensajeError = "Error inesperado al borrar el recurso";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Modifica el estado de bloqueo de un recurso (habilitado o deshabilitado para
	 * reservas).
	 * 
	 * @param usuario   Usuario autenticado
	 * @param bloqueado Nuevo estado de bloqueo del recurso
	 * @param recurso   Nombre del recurso a modificar
	 * @return ResponseEntity con estado OK si el recurso fue modificado
	 *         correctamente
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.PUT, value = "/resources")
	public ResponseEntity<?> modificarBloqueoRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "bloqueado", required = true) boolean bloqueado,
			@RequestHeader(value = "recurso", required = true) String recurso)
	{
		try
		{
			Optional<Recurso> optinalRecurso = this.recursoRepository.findById(recurso);

			if (!optinalRecurso.isPresent())
			{
				String mensajeError = "El recurso que quiere modificar no existe: " + recurso;
				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_ELIMINANDO_RECURSO, mensajeError);
			}

			optinalRecurso.get().setBloqueado(bloqueado);
			this.recursoRepository.saveAndFlush(optinalRecurso.get());

			log.info("El recurso se ha modificado correctamente: " + recurso);
			return ResponseEntity.ok().build();

		}
		catch (ReservaException reservaException)
		{
			// Si la reserva no existe, devolverá un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception)
		{
			String mensajeError = "Error inesperado al modificar el bloqueo del recurso";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Obtiene logs paginados del sistema de reservas. Cada página contiene un
	 * conjunto de logs a partir del número de página indicado.
	 * 
	 * @param usuario Usuario autenticado
	 * @param pagina  Número de página a recuperar (debe ser mayor o igual a 0)
	 * @return ResponseEntity con la lista de logs o error si no existen registros
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/logs")
	public ResponseEntity<?> getPaginatedLogs(@AuthenticationPrincipal DtoUsuarioExtended usuario, Pageable pageable)
	{
		try
		{
			if (pageable.getPageNumber() < 0)
			{
				String mensajeError = "No existen logs";
				log.error(mensajeError);
				throw new ReservaException(Constants.ERR_CODE_LOG_RESERVA, mensajeError);
			}

			Page<LogReservas> listaLogs = this.logReservasRepository.getPaginacionLogs(pageable);

			if (listaLogs.isEmpty())
			{
				String mensajeError = "No existen logs";
				log.error(mensajeError);
				throw new ReservaException(Constants.ERR_CODE_LOG_RESERVA, mensajeError);
			}

			return ResponseEntity.ok().body(listaLogs);

		}
		catch (ReservaException reservaException)
		{
			// Si la reserva no existe, devolverá un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception)
		{
			String mensajeError = "Error inesperado al obtener los logs";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
}
