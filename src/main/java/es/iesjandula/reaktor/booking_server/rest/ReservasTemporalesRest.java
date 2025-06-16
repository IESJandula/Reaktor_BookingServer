package es.iesjandula.reaktor.booking_server.rest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.booking_server.dto.ReservasPuntualesDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.LogReservas;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservaFija;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramoHorario;
import es.iesjandula.reaktor.booking_server.models.reservas_temporales.ReservaTemporal;
import es.iesjandula.reaktor.booking_server.models.reservas_temporales.ReservaTemporalId;
import es.iesjandula.reaktor.booking_server.repository.IDiaSemanaRepository;
import es.iesjandula.reaktor.booking_server.repository.IProfesorRepository;
import es.iesjandula.reaktor.booking_server.repository.IRecursoRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaRepository;
import es.iesjandula.reaktor.booking_server.repository.ITramoHorarioRepository;
import es.iesjandula.reaktor.booking_server.repository.LogReservasRepository;
import es.iesjandula.reaktor.booking_server.repository.reservas_temporales.IReservaTemporalRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;
import es.iesjandula.reaktor.booking_server.utils.Utilities;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/temporary")
@RestController
@Log4j2
public class ReservasTemporalesRest
{
	@Autowired
	private IRecursoRepository recursoRepository;

	@Autowired
	private IProfesorRepository profesoresRepository;
	
	@Autowired
	private IReservaRepository reservaFijaRepository;

	@Autowired
	private IReservaTemporalRepository reservaTemporalRepository;

	@Autowired
	private IDiaSemanaRepository diasSemanaRepository;

	@Autowired
	private ITramoHorarioRepository tramosHorariosRepository;

	@Autowired
	private LogReservasRepository logReservasRepository;

	@Value("${reaktor.firebase_server_url}")
	private String firebaseServerUrl;

	@Value("${reaktor.users_timeout}")
	private long usersTimeout;

	@Value("${reaktor.http_connection_timeout}")
	private int httpConnectionTimeout;

	@Autowired
	private Utilities utilities;

	/**
	 * Endpoint de tipo GET que devuelve una lista de reservas puntuales organizadas
	 * por día de la semana y tramo horario.
	 * <p>
	 * A partir del identificador del recurso (aula o carrito) y el número de semana
	 * proporcionados por cabecera, consulta las reservas asociadas y las estructura
	 * para facilitar su visualización. Si no se encuentra el recurso o si ocurre
	 * algún error durante la consulta, se lanza una {@link ReservaException}.
	 * <p>
	 * Solo accesible para usuarios con el rol de PROFESOR.
	 *
	 * @param recurso   Identificador del recurso (aula o carrito).
	 * @param numSemana Número de semana para la que se desean obtener las reservas.
	 * @return Lista de {@link ReservasPuntualesDto} con las reservas encontradas,
	 *         organizadas por día y tramo horario.
	 * @throws ReservaException si no se encuentra el recurso o si ocurre un error
	 *                          inesperado al acceder a la base de datos.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/bookings")
	public ResponseEntity<?> obtenerReservasDto(@RequestHeader(value = "aulaYCarritos") String recurso,
			@RequestHeader(value = "numSemana") Integer numSemana)
	{
		try
		{
			// Comprueba si la base de datos tiene registros de los recurso
			if (this.recursoRepository.count() == 0)
			{
				String mensajeError = "No se ha encontrado ningun recurso con esos datos: " + recurso;
				log.error(mensajeError);
				throw new ReservaException(Constants.RECURSO_NO_ENCONTRADO, mensajeError);
			}

			// Buscamos las reservas por el recurso y num semana
			List<Object[]> resultados = this.reservaTemporalRepository.encontrarReservaPorRecurso(recurso, numSemana);

			@SuppressWarnings("deprecation")
			Recurso recursoSeleccionado = this.recursoRepository.getById(recurso);

			List<ReservasPuntualesDto> listaReservas = this.utilities.procesarResultadosReservas(resultados,
					recursoSeleccionado);

			return ResponseEntity.ok(listaReservas);
		}
		catch (ReservaException reservaException)
		{
			// Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception)
		{
			// Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);

			log.error("Error al acceder a la base de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint POST para crear una reserva temporal de un recurso (aula o carrito).
	 * <p>
	 * Este método permite a un usuario con rol de PROFESOR registrar una reserva
	 * temporal indicando el correo del profesor, el recurso, día de la semana,
	 * tramo horario, número de alumnos, si la reserva es semanal, el motivo del
	 * curso y la semana correspondiente.
	 * <p>
	 * Realiza diversas validaciones, como:
	 * <ul>
	 * <li>Verificar si ya existe una reserva con los mismos parámetros.</li>
	 * <li>Comprobar si el recurso es compartible o no.</li>
	 * <li>Validar el número de alumnos (debe ser mayor que 0 y no exceder la
	 * capacidad del recurso).</li>
	 * <li>Controlar permisos y consistencia del correo del profesor en función del
	 * rol del usuario autenticado.</li>
	 * </ul>
	 * En caso de éxito, se guarda la reserva en la base de datos y se registra un
	 * log de la acción.
	 *
	 * @param usuario        Usuario autenticado que realiza la petición.
	 * @param email          Email del profesor que realiza la reserva.
	 * @param recurso        Identificador del recurso a reservar.
	 * @param diaDeLaSemana  Día de la semana en que se desea hacer la reserva (ID).
	 * @param tramosHorarios Tramo horario en que se desea hacer la reserva (ID).
	 * @param nAlumnos       Número de alumnos que utilizarán el recurso.
	 * @param esSemanal      Indica si la reserva se repetirá semanalmente.
	 * @param motivoCurso    Motivo o descripción de la reserva.
	 * @param numSemana      Número de semana para la cual se realiza la reserva.
	 * @return {@link ResponseEntity} con un mensaje de éxito o detalle del error.
	 * @throws ReservaException si la reserva ya existe, si el recurso no es
	 *                          compartible y ya está reservado, si el número de
	 *                          alumnos es inválido o si ocurre un error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/bookings")
	public ResponseEntity<?> realizarReservaTemporal(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramosHorarios", required = true) Long tramosHorarios,
			@RequestHeader(value = "nAlumnos", required = true) Integer nAlumnos,
			@RequestHeader(value = "esSemanal", required = true) Boolean esSemanal,
			@RequestHeader(value = "motivoCurso", required = true) String motivoCurso,
			@RequestHeader(value = "numSemana", required = true) Integer numSemana)
	{
		try
		{
			// Validaciones previas a la reserva
			this.validacionesGlobalesPreviasReservaTemporal(usuario);

			// Si el role del usuario es Administrador o Dirección, creará la reserva con el
			// email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador o Dirección, se verificará primero
			// que el
			// email coincide con el que viene en DtoUsuario.
			// Enviando excepción si no es correcto

			// Verifica si ya existe una reserva con los mismos datos
			Optional<ReservaTemporal> optionalReserva = this.reservaTemporalRepository.encontrarReserva(email, recurso,
					diaDeLaSemana, tramosHorarios, numSemana);

			if (optionalReserva.isPresent())
			{
				String mensajeError = "Ya existe una la reserva con esos datos";

				log.error(mensajeError);
				throw new ReservaException(Constants.RESERVA_YA_EXISTE, mensajeError);
			}

			Optional<Recurso> optinalRecurso = this.recursoRepository.findById(recurso);

			if (nAlumnos <= 0)
			{
				String mensajeError = "El numero de Alumnos no puede ser 0 o menor que 0";

				log.error(mensajeError);
				throw new ReservaException(Constants.NUMERO_ALUMNOS_NO_VALIDO, mensajeError);
			}

			if (!optinalRecurso.get().isEsCompartible())
			{
				Optional<ReservaTemporal> optionalReservaNoCompartido = this.reservaTemporalRepository
						.encontrarReservaNoCompartible(recurso, diaDeLaSemana, tramosHorarios, numSemana);
				if (optionalReservaNoCompartido.isPresent())
				{
					String mensajeError = "Ya existe una reserva del recurso: " + recurso + " ese dia, tramo y semana";

					log.error(mensajeError);
					throw new ReservaException(Constants.RESERVA_YA_EXISTE, mensajeError);
				}
			}

			if (nAlumnos > optinalRecurso.get().getCantidad())
			{
				String mensajeError = "El numero de Alumnos no puede ser mayor que la cantidad maxima del Recurso";

				log.error(mensajeError);
				throw new ReservaException(Constants.NUMERO_ALUMNOS_NO_VALIDO, mensajeError);
			}

			if (optinalRecurso.get().isBloqueado())
			{
				String mensajeError = "El recurso esta bloqueado";

				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_RECURSO_BLOQUEADO, mensajeError);
			}

			// Creamos la instancia de reserva
			ReservaTemporal reserva = this.crearInstanciaDeReserva(usuario, email, recurso, diaDeLaSemana,
					tramosHorarios, nAlumnos, numSemana, esSemanal);

			reserva.setMotivoCurso(motivoCurso);

			// Si no existe una reserva previa, se guarda la nueva reserva en la base de
			// datos
			this.reservaTemporalRepository.saveAndFlush(reserva);

			log.info("Se ha reservado correctamente");

			Optional<DiaSemana> diaString = this.diasSemanaRepository.findById(diaDeLaSemana.toString());
			Optional<TramoHorario> tramoHorarioString = this.tramosHorariosRepository
					.findById(tramosHorarios.toString());

			String profesor = this.profesoresRepository.getNombreProfesor(email);

			String usuarioRealizaAccion = "";

			if (usuario.getEmail().equals(email)
					&& (!usuario.getRoles().contains("ADMINISTRADOR") || !usuario.getRoles().contains("DIRECCION")))
			{
				usuarioRealizaAccion = "-";
			}
			else
			{
				usuarioRealizaAccion = usuario.getNombre() + " " + usuario.getApellidos();
			}

			LocalDate fecha = LocalDate.now().with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, numSemana)
					.with(ChronoField.DAY_OF_WEEK, diaDeLaSemana);
			DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			String fechaFormateada = fecha.format(formato);

			LogReservas log = new LogReservas(
					new Date(), profesor, "Crear", "Temporal", recurso, fechaFormateada + " | "
							+ diaString.get().getDiaSemana() + " - " + tramoHorarioString.get().getTramoHorario(),
					usuarioRealizaAccion);

			this.logReservasRepository.saveAndFlush(log);

			return ResponseEntity.ok().build();

		}
		catch (ReservaException reservaException)
		{

			// Captura la excepcion personalizada y retorna un 409 ya que existe un
			// conflicto,
			// que existe una reserva con los mismos datos
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception)
		{
			// Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error inesperado al realizar la reserva", exception);

			log.error("Error inesperado al realizar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}

	}

	/**
	 * Crea una instancia de {@link ReservaTemporal} con los datos recibidos.
	 * <p>
	 * Este método construye el objeto de reserva temporal asociando el recurso, día
	 * de la semana, tramo horario, semana, profesor y otros datos como el número de
	 * alumnos y si la reserva es semanal. Se encarga también de buscar el profesor
	 * correspondiente en función del rol del usuario autenticado.
	 * <p>
	 * Es utilizado por el endpoint para registrar una nueva reserva en la base de
	 * datos.
	 *
	 * @param usuario       Usuario autenticado que realiza la solicitud.
	 * @param email         Email del profesor que solicita la reserva (puede
	 *                      diferir del usuario autenticado).
	 * @param recursoString Identificador del recurso a reservar (aula o carrito).
	 * @param diaSemana     ID del día de la semana en que se desea realizar la
	 *                      reserva.
	 * @param tramoHorario  ID del tramo horario en que se desea realizar la
	 *                      reserva.
	 * @param nAlumnos      Número de alumnos que utilizarán el recurso.
	 * @param numSemana     Número de la semana para la que se realiza la reserva.
	 * @param esSemanal     Indica si la reserva se repetirá semanalmente.
	 * @return Instancia completamente construida de {@link ReservaTemporal}.
	 * @throws ReservaException Si ocurre un error al recuperar la información del
	 *                          profesor.
	 */
	private ReservaTemporal crearInstanciaDeReserva(DtoUsuarioExtended usuario, String email, String recursoString,
			Long diaSemana, Long tramoHorario, int nAlumnos, Integer numSemana, Boolean esSemanal)
			throws ReservaException
	{
		Recurso recurso = this.utilities.crearInstanciaRecurso(recursoString);
		DiaSemana diasSemana = this.utilities.crearInstanciaDiaSemana(diaSemana);
		TramoHorario tramos = this.utilities.crearInstanciaTramoHorario(tramoHorario);
		Profesor profesor = this.buscarProfesor(usuario, email);

		ReservaTemporalId reservaId = new ReservaTemporalId();
		reservaId.setProfesor(profesor);
		reservaId.setRecurso(recurso);
		reservaId.setDiaSemana(diasSemana);
		reservaId.setTramoHorario(tramos);
		reservaId.setNumSemana(numSemana);

		ReservaTemporal reservaTemporal = new ReservaTemporal();
		reservaTemporal.setReservaTemporalId(reservaId);
		reservaTemporal.setNAlumnos(nAlumnos);
		reservaTemporal.setEsSemanal(esSemanal);

		return reservaTemporal;
	}

	/**
	 * Recupera la entidad {@link Profesor} correspondiente al email indicado,
	 * utilizando la información del usuario autenticado y sus roles.
	 * <p>
	 * Si el usuario tiene rol de ADMINISTRADOR o DIRECCIÓN, el método intenta
	 * recuperar el profesor desde la base de datos local. Si no se encuentra, se
	 * consulta a Firebase.
	 * <p>
	 * Si el usuario no tiene rol privilegiado, se construye el objeto
	 * {@link Profesor} directamente a partir de los datos del usuario autenticado,
	 * y se persiste en base de datos si no existía previamente.
	 *
	 * @param usuario Usuario autenticado que realiza la solicitud.
	 * @param email   Email del profesor asociado a la reserva.
	 * @return El objeto {@link Profesor} correspondiente al email indicado.
	 * @throws ReservaException Si ocurre un error al consultar Firebase u obtener
	 *                          los datos del profesor.
	 */
	private Profesor buscarProfesor(DtoUsuarioExtended usuario, String email) throws ReservaException
	{
		Profesor profesor = null;

		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				|| usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			Optional<Profesor> optionalProfesor = this.profesoresRepository.findById(email);

			if (optionalProfesor.isPresent())
			{
				profesor = optionalProfesor.get();
			}
			else
			{
				profesor = this.utilities.buscarProfesorEnFirebase(usuario.getJwt(), email, this.firebaseServerUrl,
						this.httpConnectionTimeout);
			}
		}
		else
		{
			Optional<Profesor> optionalProfesor = this.profesoresRepository.findById(usuario.getEmail());

			if (optionalProfesor.isPresent())
			{
				profesor = optionalProfesor.get();
			}
			else
			{
				profesor = new Profesor();
				profesor.setNombre(usuario.getNombre());
				profesor.setApellidos(usuario.getApellidos());
				profesor.setEmail(usuario.getEmail());

				this.profesoresRepository.saveAndFlush(profesor);
			}
		}

		return profesor;
	}

	/**
	 * Elimina una reserva temporal existente según los parámetros especificados.
	 * <p>
	 * Este endpoint permite a un profesor cancelar su propia reserva, o a un
	 * usuario con rol de ADMINISTRADOR o DIRECCIÓN cancelar reservas de cualquier
	 * profesor.
	 * <p>
	 * Si la reserva es semanal, se eliminan todas las instancias semanales
	 * correlativas hacia atrás y hacia adelante mientras compartan el atributo
	 * {@code esSemanal = true}.
	 * <p>
	 * Se registra la acción de borrado en la tabla de logs.
	 *
	 * @param usuario       Usuario autenticado que realiza la petición.
	 * @param email         Email del profesor cuya reserva se desea cancelar.
	 * @param aulaYCarritos Identificador del recurso reservado.
	 * @param diaDeLaSemana Día de la semana en que se hizo la reserva.
	 * @param tramoHorario  Tramo horario de la reserva.
	 * @param numSemana     Número de semana correspondiente a la reserva.
	 * @param esSemanal     Indicador de si la reserva es de tipo semanal
	 *                      (opcional).
	 * @return {@link ResponseEntity} con estado 200 si se elimina correctamente,
	 *         404 si la reserva no existe, o 500 ante errores inesperados.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/bookings")
	public ResponseEntity<?> borrarReserva(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String aulaYCarritos,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramoHorario", required = true) Long tramoHorario,
			@RequestHeader(value = "numSemana", required = true) Integer numSemana,
			@RequestHeader(value = "esSemanal", required = false) Boolean esSemanal)
	{
		try
		{
			// Validaciones previas a la reserva
			this.validacionesGlobalesPreviasReservaTemporal(usuario);
			
			if (usuario.getRoles() != null 	   && 
				usuario.getRoles().size() == 1 &&
				usuario.getRoles().contains(BaseConstants.ROLE_PROFESOR) &&
				!email.equals(usuario.getEmail()))
			{
				String mensajeError = "No puedes borrar reservas de otras personas";
				
				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_CANCELANDO_RESERVA, mensajeError);
			}

			// Si el role del usuario es Administrador o Dirección, borrará la reserva con
			// el email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador o Dirección, se verificará primero
			// que el
			// email coincide con el que viene en DtoUsuario. Enviando excepción si no es
			// correcto

			// Antes de borrar la reserva verifica si existe una reserva con los mismos
			// datos
			Optional<ReservaTemporal> optinalReserva = this.reservaTemporalRepository.encontrarReserva(email,
					aulaYCarritos, diaDeLaSemana, tramoHorario, numSemana);

			if (!optinalReserva.isPresent())
			{
				String mensajeError = "La reserva que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(Constants.RESERVA_NO_ENCONTRADA, mensajeError);
			}

			// Creamos instancia de ReservaId para luego borrar por este id
			ReservaTemporalId reservaId = this.crearInstanciaDeReservaId(usuario, email, aulaYCarritos, diaDeLaSemana, tramoHorario, numSemana);

			Integer semanaInicial = numSemana;
			ReservaTemporal reservaIterable = new ReservaTemporal();
			List<ReservaTemporal> listaReservasBorrado = new ArrayList<ReservaTemporal>();

			if (esSemanal)
			{
				do
				{
					Optional<ReservaTemporal> optinalReservaIterable = this.reservaTemporalRepository
							.encontrarReserva(email, aulaYCarritos, diaDeLaSemana, tramoHorario, numSemana);

					if (optinalReservaIterable.isEmpty())
					{
						break;
					}
					reservaIterable = optinalReservaIterable.get();
					listaReservasBorrado.add(reservaIterable);
					numSemana--;
				}
				while (reservaIterable.isEsSemanal());

				numSemana = semanaInicial;
				do
				{
					numSemana++;
					Optional<ReservaTemporal> optinalReservaIterable = this.reservaTemporalRepository
							.encontrarReserva(email, aulaYCarritos, diaDeLaSemana, tramoHorario, numSemana);
					if (optinalReservaIterable.isEmpty())
					{
						break;
					}
					reservaIterable = optinalReservaIterable.get();
					listaReservasBorrado.add(reservaIterable);
				}

				while (reservaIterable.isEsSemanal());

				if ((usuario.getRoles().contains("PROFESOR") && email.equals(usuario.getEmail())
						&& !usuario.getRoles().contains("ADMINISTRADOR") && !usuario.getRoles().contains("DIRECCION"))
						|| (usuario.getRoles().contains("ADMINISTRADOR") || usuario.getRoles().contains("DIRECCION")))
				{
					this.reservaTemporalRepository.deleteAll(listaReservasBorrado);
				}
				else
				{
					String mensajeError = "No puedes borrar reservas de otras personas";
					log.error(mensajeError);
					throw new ReservaException(Constants.ERROR_CANCELANDO_RESERVA, mensajeError);
				}

				// Borramos las reservas
				this.reservaTemporalRepository.deleteAll(listaReservasBorrado);
			}
			else
			{
				if ((usuario.getRoles().contains("PROFESOR") && email.equals(usuario.getEmail())
						&& !usuario.getRoles().contains("ADMINISTRADOR") && !usuario.getRoles().contains("DIRECCION"))
						|| (usuario.getRoles().contains("ADMINISTRADOR") || usuario.getRoles().contains("DIRECCION")))
				{
					this.reservaTemporalRepository.deleteById(reservaId);
				}
				else
				{
					String mensajeError = "No puedes borrar reservas de otras personas";
					log.error(mensajeError);
					throw new ReservaException(Constants.ERROR_CANCELANDO_RESERVA, mensajeError);
				}
			}

			log.info("La reserva se ha borrado correctamente");

			Optional<DiaSemana> diaString = this.diasSemanaRepository.findById(diaDeLaSemana.toString());
			Optional<TramoHorario> tramoHorarioString = this.tramosHorariosRepository.findById(tramoHorario.toString());

			String profesor = this.profesoresRepository.getNombreProfesor(email);

			String usuarioRealizaAccion = "";

			if (usuario.getEmail().equals(email)
					&& (!usuario.getRoles().contains("ADMINISTRADOR") || !usuario.getRoles().contains("DIRECCION")))
			{
				usuarioRealizaAccion = "-";
			}
			else
			{
				usuarioRealizaAccion = usuario.getNombre() + " " + usuario.getApellidos();
			}

			LocalDate fecha = LocalDate.now().with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, numSemana)
					.with(ChronoField.DAY_OF_WEEK, diaDeLaSemana);
			DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			String fechaFormateada = fecha.format(formato);

			LogReservas logBorrado = new LogReservas(
					new Date(), profesor, "Borrar", "Temporal", aulaYCarritos, fechaFormateada + " | "
							+ diaString.get().getDiaSemana() + " - " + tramoHorarioString.get().getTramoHorario(),
					usuarioRealizaAccion);

			this.logReservasRepository.saveAndFlush(logBorrado);

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
					"Error inesperado al cancelar la reserva", exception);
			log.error("Error inesperado al cancelar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Crea una instancia de {@link ReservaTemporalId} a partir de los parámetros
	 * proporcionados.
	 * <p>
	 * Esta función determina qué profesor debe asociarse con la reserva:
	 * <ul>
	 * <li>Si el usuario tiene rol ADMINISTRADOR o DIRECCIÓN, se usa el email
	 * proporcionado.</li>
	 * <li>En caso contrario, se usa el email del usuario autenticado.</li>
	 * </ul>
	 *
	 * @param usuario       Usuario autenticado que realiza la solicitud.
	 * @param email         Email del profesor titular de la reserva.
	 * @param aulaYCarritos Identificador del recurso reservado.
	 * @param diaDeLaSemana Día de la semana asociado a la reserva.
	 * @param tramoHorario  Tramo horario en que se realiza la reserva.
	 * @param numSemana     Número de semana correspondiente a la reserva.
	 * @return Instancia de {@link ReservaTemporalId} representando la reserva.
	 */
	private ReservaTemporalId crearInstanciaDeReservaId(DtoUsuarioExtended usuario, String email, String aulaYCarritos,
			Long diaDeLaSemana, Long tramoHorario, Integer numSemana)
	{
		Recurso recurso = new Recurso();
		recurso.setId(aulaYCarritos);

		DiaSemana diasSemana = new DiaSemana();
		diasSemana.setId(diaDeLaSemana);

		TramoHorario tramosHorarios = new TramoHorario();
		tramosHorarios.setId(tramoHorario);

		Optional<Profesor> profesor = null;

		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				|| usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			profesor = this.profesoresRepository.findById(email);
		}
		else
		{
			profesor = this.profesoresRepository.findById(usuario.getEmail());
		}

		ReservaTemporalId reservaId = new ReservaTemporalId();

		if (profesor.isPresent())
		{
			reservaId.setProfesor(profesor.get());
		}

		reservaId.setRecurso(recurso);
		reservaId.setDiaSemana(diasSemana);
		reservaId.setTramoHorario(tramosHorarios);
		reservaId.setNumSemana(numSemana);
		return reservaId;
	}

	/**
	 * Realiza validaciones previas a la creación o cancelación de una reserva
	 * temporal.
	 * <p>
	 * Esta validación aplica únicamente a los usuarios que no tienen roles de
	 * ADMINISTRADOR ni DIRECCIÓN. Verifica si el sistema de reservas temporales se
	 * encuentra deshabilitado mediante una constante de configuración (clave
	 * {@link Constants#TABLA_CONST_RESERVAS_TEMPORALES}). Si el sistema está
	 * deshabilitado, se lanza una {@link ReservaException} con el motivo.
	 *
	 * @param usuario Usuario autenticado que intenta realizar la operación de
	 *                reserva.
	 * @throws ReservaException Si ocurre un error al obtener los parámetros o si la
	 *                          funcionalidad está deshabilitada.
	 */
	private void validacionesGlobalesPreviasReservaTemporal(DtoUsuarioExtended usuario) throws ReservaException
	{
		this.utilities.validacionesGlobalesPreviasReserva(usuario, "TEMPORALES");
	}

	/**
	 * Endpoint que permite comprobar si un recurso está disponible para ser
	 * reservado en un día específico, en un tramo horario determinado, durante una
	 * o varias semanas, teniendo en cuenta la cantidad de alumnos.
	 * <p>
	 * Si el recurso no tiene reservas en alguna de las semanas indicadas o si es
	 * compartible y hay capacidad suficiente, se considerará disponible.
	 *
	 * @param diaDeLaSemana Día de la semana (ej. lunes = 1, martes = 2, etc.).
	 * @param recurso       Identificador del recurso que se desea comprobar (aula,
	 *                      carrito, etc.).
	 * @param tramoHorario  Identificador del tramo horario solicitado.
	 * @param numAlumnos    Número de alumnos que requieren el recurso.
	 * @param semanas       Lista de números de semana en los que se quiere
	 *                      comprobar la disponibilidad.
	 * @return {@code ResponseEntity<Boolean>} que indica si el recurso está
	 *         disponible ({@code true}) o no ({@code false}).
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/bookings/available")
	public ResponseEntity<?> comprobarDisponibilidad(
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "tramoHorario", required = true) Long tramoHorario,
			@RequestHeader(value = "numAlumnos", required = true) Integer numAlumnos,
			@RequestHeader(value = "semanas") List<Integer> semanas)
	{
		try
		{
			Boolean disponible = true;
			if (!semanas.isEmpty())
			{
				Set<Integer> sinRepetir = new HashSet<>();

				// Eliminar elementos duplicados
				semanas.removeIf(num -> !sinRepetir.add(num));
				
				Optional<List<ReservaFija>> optionalListFijas = this.reservaFijaRepository.encontrarReservasFijasPorDiaTramo(recurso, diaDeLaSemana, tramoHorario);
				
				int contadorReservaFija = 0;
				
				Recurso recursoInstancia = this.recursoRepository.findById(recurso).get();
				
				if(optionalListFijas.isPresent())
				{
					disponible = recursoInstancia.isEsCompartible();
					if(disponible)
					{
						List<ReservaFija> listReservaFija = optionalListFijas.get();
						
						for (ReservaFija reservaFija : listReservaFija)
						{
							contadorReservaFija = contadorReservaFija + reservaFija.getNAlumnos();
						}
					}
				}
				
				Iterator<Integer> iterator = semanas.iterator();
				
				while (iterator.hasNext() && disponible)
				{
					int contadorActualReserva = contadorReservaFija;
					
					Optional<List<ReservaTemporal>> optionalListTemporales = this.reservaTemporalRepository
							.encontrarReservasPorDiaTramo(recurso, diaDeLaSemana, tramoHorario, iterator.next());

					if (optionalListTemporales.isPresent())
					{
						disponible = recursoInstancia.isEsCompartible();
						if(disponible)
						{
							List<ReservaTemporal> listReservaTemporales = optionalListTemporales.get();
							
							for (ReservaTemporal reservaTemporal : listReservaTemporales)
							{
								contadorActualReserva = contadorActualReserva + reservaTemporal.getNAlumnos();
							}
						}
					}
					
					disponible = (recursoInstancia.getCantidad()) - (contadorActualReserva + numAlumnos) >= 0;
				}
			}
			return ResponseEntity.ok(disponible);
		}
		catch (Exception exception)
		{
			// Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);

			log.error("Error al acceder a la base de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
}
