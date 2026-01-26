package es.iesjandula.reaktor.booking_server.rest;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.base.utils.HttpClientUtils;
import es.iesjandula.reaktor.booking_server.dto.ReservasFijasDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.Constantes;
import es.iesjandula.reaktor.booking_server.models.LogReservas;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservaFija;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservaFijaId;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramoHorario;
import es.iesjandula.reaktor.booking_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.booking_server.repository.IDiaSemanaRepository;
import es.iesjandula.reaktor.booking_server.repository.IProfesorRepository;
import es.iesjandula.reaktor.booking_server.repository.IRecursoRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaFijaRepository;
import es.iesjandula.reaktor.booking_server.repository.ITramoHorarioRepository;
import es.iesjandula.reaktor.booking_server.repository.LogReservasRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;

@RequestMapping(value = "/bookings/fixed")
@RestController
public class ReservasFijasRest
{
	/**
	 * Logger of the class
	 */
	private static final Logger log = LoggerFactory.getLogger(ReservasFijasRest.class);

	@Autowired
	private IRecursoRepository recursoRepository;

	@Autowired
	private IProfesorRepository profesoresRepository;

	@Autowired
	private IReservaFijaRepository reservaFijaRepository;

	@Autowired
	private IDiaSemanaRepository diasSemanaRepository;

	@Autowired
	private ITramoHorarioRepository tramosHorariosRepository;

	@Autowired
	private ConstantesRepository constanteRepository;

	@Autowired
	private LogReservasRepository logReservasRepository;

	@Value("${reaktor.firebase_server_url}")
	private String firebaseServerUrl;

	@Value("${reaktor.http_connection_timeout}")
	private int httpConnectionTimeout;

	/**
	 * Endpoint de tipo GET que permite obtener una lista de recursos, filtrando si
	 * son compartibles o no. Solo accesible para usuarios con rol de PROFESOR.
	 * 
	 * @param esCompartible Indica si se deben incluir únicamente los recursos
	 *                      marcados como compartibles.
	 * @return Lista de recursos que cumplen con el filtro, o un mensaje de error en
	 *         caso de fallo.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/resourcesCompartible")
	public ResponseEntity<?> obtenerRecurso(
			@RequestHeader(value = "esCompartible", required = true) boolean esCompartible)
	{
		try
		{
			// Encontramos todos los recurso diferenciando si son compartibles o no y los
			// introducimos en una lista para mostrarlos
			List<Recurso> listaRecursos = this.recursoRepository.encontrarRecursoCompartible(esCompartible);

			return ResponseEntity.ok(listaRecursos);
		}

		catch (Exception exception)
		{
			// Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo GET que permite obtener todos los recursos existentes en la
	 * base de datos. Solo accesible para usuarios con rol de PROFESOR.
	 * 
	 * @return Lista completa de recursos, o mensaje de error si no se encuentra
	 *         ninguno o si hay un fallo de base de datos.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/resources")
	public ResponseEntity<?> obtenerRecursos()
	{
		try
		{
			List<Recurso> listaRecursos = this.recursoRepository.findAll();

			// Encontramos todos los recursos y los introducimos en una lista para
			// mostrarlos más adelante

			// Comprueba si la base de datos tiene registros de los recurso

			if (listaRecursos.isEmpty())
			{
				String mensajeError = "No se ha encontrado ningun recurso";

				log.error(mensajeError);
				throw new ReservaException(Constants.RECURSO_NO_ENCONTRADO, mensajeError);
			}

			return ResponseEntity.ok(listaRecursos);
		} catch (ReservaException reservaException)
		{
			// Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			// Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo GET que permite obtener todos los tramos horarios
	 * registrados. Solo accesible para usuarios con rol de PROFESOR.
	 * 
	 * @return Lista de tramos horarios, o mensaje de error si no se encuentran o
	 *         ocurre un fallo al acceder a la base de datos.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/timeslots")
	public ResponseEntity<?> obtenerTramosHorarios()
	{
		try
		{
			// Encontramos todos los tramos y los introducimos en una lista para mostrarlos
			// más adelante
			List<TramoHorario> listaTramos = this.tramosHorariosRepository.findAll();

			// Comprueba si la base de datos tiene registros de los tramos horarios
			if (listaTramos.isEmpty())
			{
				String mensajeError = "No se ha encontrado ningun tramo horario";

				log.error(mensajeError);
				throw new ReservaException(Constants.TRAMO_HORARIO_NO_ENCONTRADO, mensajeError);
			}

			return ResponseEntity.ok(listaTramos);
		} catch (ReservaException reservaException)
		{
			// Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			// Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo GET que permite obtener todos los días de la semana
	 * registrados. Solo accesible para usuarios con rol de PROFESOR.
	 * 
	 * @return Lista de días de la semana, o mensaje de error si no se encuentran o
	 *         ocurre un fallo al acceder a la base de datos.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/days_week")
	public ResponseEntity<?> obtenerDiasSemana()
	{
		try
		{
			// Encontramos todos los dias y los introducimos en una lista para mostrarlos
			// más adelante
			List<DiaSemana> listaDias = this.diasSemanaRepository.findAll();

			// Comprueba si la base de datos tiene registros de los días de la semana
			if (listaDias.isEmpty())
			{
				String mensajeError = "Error al obtener los días de la semana";
				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_OBTENIENDO_DIAS_SEMANA, mensajeError);
			}

			return ResponseEntity.ok(listaDias);
		} catch (ReservaException reservaException)
		{
			// Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			// Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo GET que recibe el nombre de un recurso (como aula o carrito)
	 * por cabecera y devuelve una lista de reservas agrupadas por día de la semana
	 * y tramo horario. Solo accesible para usuarios con rol de PROFESOR.
	 * 
	 * @param recurso Nombre del recurso para el cual se desean obtener las
	 *                reservas.
	 * @return Lista de objetos DTO con la información agrupada de las reservas, o
	 *         mensaje de error si el recurso no existe o falla el acceso a la base
	 *         de datos.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/bookings")
	public ResponseEntity<?> obtenerReservasDto(@RequestHeader(value = "aulaYCarritos") String recurso)
	{
		try
		{
			// Creacion de una lista para almacenar los recursos
			List<ReservasFijasDto> listaReservas = new ArrayList<ReservasFijasDto>();

			// Comprueba si la base de datos tiene registros de los recurso
			if (this.recursoRepository.count() == 0)
			{
				String mensajeError = "No se ha encontrado ningun recurso con esos datos: " + recurso;
				log.error(mensajeError);
				throw new ReservaException(Constants.RECURSO_NO_ENCONTRADO, mensajeError);
			}

			// Buscamos las reservas por el recurso
			List<Object[]> resultados = this.reservaFijaRepository.encontrarReservaPorRecurso(recurso);

			@SuppressWarnings("deprecation")
			Recurso recursoSeleccionado = this.recursoRepository.getById(recurso);

			Long diaSemana = 0l;
			Long tramoHorario = 0l;
			List<String> emails = new ArrayList<String>();
			List<Integer> nAlumnosLista = new ArrayList<Integer>();
			List<String> nombresYApellidos = new ArrayList<String>();
			ReservasFijasDto reserva = new ReservasFijasDto();
			Integer plazasRestantes = recursoSeleccionado.getCantidad();
			List<String> motivoCursoLista = new ArrayList<String>();

			for (Object[] row : resultados)
			{

				if (diaSemana == (Long) row[0] && tramoHorario == (Long) row[1])
				{

					ReservasFijasDto reservaAntigua = reserva;

					emails = reserva.getEmail();
					nombresYApellidos = reserva.getNombreYapellidos();
					nAlumnosLista = reserva.getNAlumnos();
					plazasRestantes = reserva.getPlazasRestantes();

					emails.add((String) row[3]);
					nombresYApellidos.add((String) row[4]);
					nAlumnosLista.add((row[2] != null) ? (Integer) row[2] : 0);
					plazasRestantes = plazasRestantes - ((row[2] != null) ? (Integer) row[2] : 0);

					reserva.setEmail(emails);
					reserva.setNombreYapellidos(nombresYApellidos);
					reserva.setNAlumnos(nAlumnosLista);
					reserva.setPlazasRestantes(plazasRestantes);
					motivoCursoLista.add((String) row[6]);
					reserva.setMotivoCurso(motivoCursoLista);

					listaReservas.remove(reservaAntigua);

					listaReservas.add(reserva);
				} else
				{
					plazasRestantes = recursoSeleccionado.getCantidad();
					diaSemana = (Long) row[0];
					tramoHorario = (Long) row[1];
					Integer nAlumnos = (row[2] != null) ? (Integer) row[2] : 0;
					String email = (String) row[3];
					String nombreYapellidos = (String) row[4];
					String recursos = (String) row[5];
					String motivoCurso = (String) row[6];
					plazasRestantes = plazasRestantes - nAlumnos;

					emails = new ArrayList<String>();
					emails.add(email);
					nombresYApellidos = new ArrayList<String>();
					nombresYApellidos.add(nombreYapellidos);
					nAlumnosLista = new ArrayList<Integer>();
					nAlumnosLista.add(nAlumnos);
					motivoCursoLista = new ArrayList<String>();
					motivoCursoLista.add(motivoCurso);

					reserva = new ReservasFijasDto(diaSemana, tramoHorario, nAlumnosLista, emails, nombresYApellidos,
							recursos, plazasRestantes, motivoCursoLista);
					// Mapeo a ReservaDto
					listaReservas.add(reserva);
				}

			}
			// Encontramos todos los recursos y los introducimos en una lista para
			// mostrarlos más adelante

			return ResponseEntity.ok(listaReservas);
		} catch (ReservaException reservaException)
		{
			// Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			// Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);

			log.error("Error al acceder a la base de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo POST que permite a un profesor realizar una reserva fija de
	 * un recurso.
	 * <p>
	 * Esta reserva se realiza para un día concreto de la semana y un tramo horario
	 * específico. El profesor debe proporcionar su correo electrónico, el
	 * identificador del recurso, el motivo del curso asociado a la reserva, el día
	 * de la semana (como ID), el tramo horario (como ID) y el número de alumnos que
	 * utilizarán el recurso.
	 * <p>
	 * Requiere autenticación y autorización, permitiendo el acceso solo a usuarios
	 * con el rol de profesor.
	 * <p>
	 * Validaciones aplicadas:
	 * <ul>
	 * <li>Verificación de que no exista una reserva previa con los mismos
	 * datos.</li>
	 * <li>Validación del número de alumnos (debe ser mayor que cero y no superar la
	 * capacidad del recurso).</li>
	 * <li>Verificación de permisos del usuario según su rol: si el rol no es
	 * administrador o dirección, se asegura que el correo proporcionado coincide
	 * con el del usuario autenticado.</li>
	 * </ul>
	 * 
	 * Si la reserva se crea correctamente, se registra también en el sistema de
	 * logs.
	 * 
	 * @param usuario        Objeto que representa al usuario autenticado (extraído
	 *                       del token).
	 * @param email          Correo electrónico del profesor que realiza la reserva.
	 * @param recurso        Identificador del recurso a reservar.
	 * @param motivoCurso    Motivo del curso o asignatura asociada a la reserva.
	 * @param diaDeLaSemana  Día de la semana en que se desea realizar la reserva
	 *                       (ID).
	 * @param tramosHorarios Identificador del tramo horario en que se desea
	 *                       realizar la reserva.
	 * @param nAlumnos       Número de alumnos que harán uso del recurso.
	 * @return ResponseEntity con un mensaje indicando si la reserva fue realizada
	 *         correctamente o con el detalle del error en caso de fallo.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/bookings")
	public ResponseEntity<?> realizarReservaFija(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "motivoCurso", required = true) String motivoCurso,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramosHorarios", required = true) Long tramosHorarios,
			@RequestHeader(value = "nAlumnos", required = true) int nAlumnos)
	{
		try
		{
			// Validaciones previas a la reserva
			this.validacionesGlobalesPreviasReservaFija(usuario);

			// Si el role del usuario es Administrador o Dirección, creará la reserva con el
			// email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador o Dirección, se verificará primero
			// que el
			// email coincide con el que viene en DtoUsuario.
			// Enviando excepción si no es correcto

			// Verifica si ya existe una reserva con los mismos datos
			Optional<ReservaFija> optionalReserva = this.reservaFijaRepository.encontrarReserva(email, recurso,
					diaDeLaSemana, tramosHorarios);

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
			ReservaFija reserva = this.crearInstanciaDeReserva(usuario, email, recurso, diaDeLaSemana, tramosHorarios,
					nAlumnos);

			reserva.setMotivoCurso(motivoCurso);

			// Si no existe una reserva previa, se guarda la nueva reserva en la base de
			// datos
			this.reservaFijaRepository.saveAndFlush(reserva);

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
			} else
			{
				usuarioRealizaAccion = usuario.getNombre() + " " + usuario.getApellidos();
			}

			// Creamos una instancia de LogReservas para la creación de la reserva
			LogReservas logReservasCreacion = new LogReservas();

			// Añadimos los datos de la reserva al LogReservas
			logReservasCreacion.setUsuario(profesor);
			logReservasCreacion.setAccion("Crear");
			logReservasCreacion.setTipo("Fija");
			logReservasCreacion.setRecurso(recurso);
			logReservasCreacion.setFechaReserva(new Date());
			logReservasCreacion.setDiaSemana(diaString.get().getDiaSemana());
			logReservasCreacion.setTramoHorario(tramoHorarioString.get().getTramoHorario());
			logReservasCreacion.setSuperUsuario(usuarioRealizaAccion);

			// Guardamos la reserva en la base de datos
			this.logReservasRepository.saveAndFlush(logReservasCreacion);

			// Logueamos
			log.info("La reserva fija se ha creado correctamente");

			return ResponseEntity.ok().build();

		} catch (ReservaException reservaException)
		{

			// Captura la excepcion personalizada y retorna un 409 ya que existe un
			// conflicto,
			// que existe una reserva con los mismos datos
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			// Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error inesperado al realizar la reserva", exception);

			log.error("Error inesperado al realizar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}

	}

	/**
	 * Crea una instancia de una reserva fija a partir de los datos proporcionados.
	 * <p>
	 * Este método construye un objeto {@code ReservaFija} con los elementos
	 * necesarios para una reserva: profesor, recurso, día de la semana, tramo
	 * horario y número de alumnos.
	 * <p>
	 * Se obtiene el profesor mediante el método {@code buscarProfesor}, el cual
	 * valida si el usuario tiene permisos para actuar en nombre de otro profesor o
	 * si debe utilizar sus propios datos.
	 * 
	 * @param usuario       Objeto que representa al usuario autenticado (con roles
	 *                      y datos personales).
	 * @param email         Correo electrónico del profesor al que se asignará la
	 *                      reserva.
	 * @param recursoString Identificador del recurso a reservar.
	 * @param diaSemana     Día de la semana (ID) en que se desea realizar la
	 *                      reserva.
	 * @param tramoHorario  Tramo horario (ID) en que se desea realizar la reserva.
	 * @param nAlumnos      Número de alumnos que utilizarán el recurso.
	 * @return Objeto {@code ReservaFija} listo para ser persistido.
	 * @throws ReservaException Si ocurre un error al obtener los datos del
	 *                          profesor.
	 */
	private ReservaFija crearInstanciaDeReserva(DtoUsuarioExtended usuario, String email, String recursoString,
			Long diaSemana, Long tramoHorario, int nAlumnos) throws ReservaException
	{
		Recurso recurso = new Recurso();

		recurso.setId(recursoString);

		DiaSemana diasSemana = new DiaSemana();
		diasSemana.setId(diaSemana);

		TramoHorario tramos = new TramoHorario();
		tramos.setId(tramoHorario);

		Profesor profesor = this.buscarProfesor(usuario, email);

		ReservaFijaId reservaId = new ReservaFijaId();

		reservaId.setProfesor(profesor);
		reservaId.setRecurso(recurso);
		reservaId.setDiaSemana(diasSemana);
		reservaId.setTramoHorario(tramos);

		ReservaFija reservaFija = new ReservaFija();

		reservaFija.setReservaFijaId(reservaId);
		reservaFija.setNAlumnos(nAlumnos);

		return reservaFija;
	}

	/**
	 * Obtiene la información de un profesor a partir del usuario autenticado y el
	 * correo electrónico proporcionado.
	 * <p>
	 * Si el usuario tiene rol de administrador o dirección, se intenta buscar el
	 * profesor en la base de datos local. Si no se encuentra, se consulta a un
	 * servicio externo (Firebase) y se almacena el resultado en la base de datos.
	 * <p>
	 * Si el usuario no tiene rol de administrador o dirección, se crea el objeto
	 * {@code Profesor} directamente a partir de los datos del usuario autenticado y
	 * se guarda en la base de datos si no existe.
	 * 
	 * @param usuario Objeto que representa al usuario autenticado (incluye roles y
	 *                JWT).
	 * @param email   Correo electrónico del profesor que se desea buscar o
	 *                registrar.
	 * @return Objeto {@code Profesor} correspondiente al correo proporcionado.
	 * @throws ReservaException Si ocurre un error durante la búsqueda o creación
	 *                          del profesor.
	 */
	public Profesor buscarProfesor(DtoUsuarioExtended usuario, String email) throws ReservaException
	{
		Profesor profesor = null;

		// Si el role es administrador o dirección ...
		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				|| usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			// Primero buscamos si ya tenemos a ese profesor en nuestra BBDD
			Optional<Profesor> optionalProfesor = this.profesoresRepository.findById(email);

			// Si lo encontramos ...
			if (!optionalProfesor.isEmpty())
			{
				// Lo cogemos del optional
				profesor = optionalProfesor.get();
			} else
			{
				// Si no lo encontramos, le pedimos a Firebase que nos lo dé
				profesor = this.buscarProfesorEnFirebase(usuario.getJwt(), email);
			}
		} else
		{
			// Si el usuario no es administrador o dirección, cogemos la información del
			// usuario
			profesor = new Profesor(usuario.getEmail(), usuario.getNombre(), usuario.getApellidos());

			// Lo almacenamos en BBDD en caso de que no exista
			this.profesoresRepository.saveAndFlush(profesor);
		}

		return profesor;
	}

	/**
	 * Realiza una consulta al servidor externo (Firebase) para obtener los datos de
	 * un profesor a partir de su correo electrónico, utilizando un JWT de un
	 * administrador como autenticación.
	 * <p>
	 * Si la respuesta es válida, se convierte en un objeto {@code Profesor} y se
	 * guarda en la base de datos. Maneja excepciones relacionadas con tiempos de
	 * espera y errores de entrada/salida durante la comunicación.
	 * 
	 * @param jwtAdmin JWT del usuario administrador que autoriza la solicitud al
	 *                 servidor externo.
	 * @param email    Correo electrónico del profesor que se desea buscar.
	 * @return Objeto {@code Profesor} con los datos obtenidos de Firebase.
	 * @throws ReservaException Si ocurre un error durante la comunicación con
	 *                          Firebase o al procesar la respuesta.
	 */
	private Profesor buscarProfesorEnFirebase(String jwtAdmin, String email) throws ReservaException
	{
		Profesor profesor = null;

		// Creamos un HTTP Client con Timeout
		CloseableHttpClient closeableHttpClient = HttpClientUtils.crearHttpClientConTimeout(this.httpConnectionTimeout);

		CloseableHttpResponse closeableHttpResponse = null;

		try
		{
			HttpGet httpGet = new HttpGet(this.firebaseServerUrl + "/firebase/queries/user");

			// Añadimos el jwt y el email a la llamada
			httpGet.addHeader("Authorization", "Bearer " + jwtAdmin);
			httpGet.addHeader("email", email);

			// Hacemos la peticion
			closeableHttpResponse = closeableHttpClient.execute(httpGet);

			// Comprobamos si viene la cabecera. En caso afirmativo, es porque trae un
			// profesor
			if (closeableHttpResponse.getEntity() == null)
			{
				String mensajeError = "Profesor no encontrado en BBDD Global";

				log.error(mensajeError);
				throw new ReservaException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError);
			}

			// Convertimos la respuesta en un objeto DtoInfoUsuario
			ObjectMapper objectMapper = new ObjectMapper();

			// Obtengo la respuesta completa como String
			String responseContent = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);

			// Y parseo el DTO
			DtoUsuarioBase dtoUsuarioBase = objectMapper.readValue(responseContent, DtoUsuarioBase.class);

			// Creamos una instancia de profesor con la respuesta de Firebase
			profesor = new Profesor();
			profesor.setNombre(dtoUsuarioBase.getNombre());
			profesor.setApellidos(dtoUsuarioBase.getApellidos());
			profesor.setEmail(dtoUsuarioBase.getEmail());

			// Almacenamos al profesor en nuestra BBDD
			this.profesoresRepository.saveAndFlush(profesor);
		} catch (SocketTimeoutException socketTimeoutException)
		{
			String errorString = "SocketTimeoutException de lectura o escritura al comunicarse con el servidor (búsqueda del profesor asociado a la reserva)";

			log.error(errorString, socketTimeoutException);
			throw new ReservaException(Constants.ERROR_CONEXION_FIREBASE, errorString, socketTimeoutException);
		} catch (ConnectTimeoutException connectTimeoutException)
		{
			String errorString = "ConnectTimeoutException al intentar conectar con el servidor (búsqueda del profesor asociado a la reserva)";

			log.error(errorString, connectTimeoutException);
			throw new ReservaException(Constants.TIMEOUT_CONEXION_FIREBASE, errorString, connectTimeoutException);
		} catch (IOException ioException)
		{
			String errorString = "IOException mientras se buscaba el profesor asociado a la reserva";

			log.error(errorString, ioException);
			throw new ReservaException(Constants.IO_EXCEPTION_FIREBASE, errorString, ioException);
		} finally
		{
			// Cierre de flujos
			this.buscarProfesorEnFirebaseCierreFlujos(closeableHttpResponse);
		}

		return profesor;
	}

	/**
	 * Cierra de forma segura la respuesta HTTP recibida tras consultar a Firebase.
	 * <p>
	 * Si ocurre un error al cerrar el flujo, se lanza una excepción personalizada.
	 * 
	 * @param closeableHttpResponse Objeto de respuesta HTTP que se desea cerrar.
	 * @throws ReservaException Si ocurre un error de entrada/salida al cerrar el
	 *                          flujo de respuesta.
	 */
	private void buscarProfesorEnFirebaseCierreFlujos(CloseableHttpResponse closeableHttpResponse)
			throws ReservaException
	{
		if (closeableHttpResponse != null)
		{
			try
			{
				closeableHttpResponse.close();
			} catch (IOException ioException)
			{
				String errorString = "IOException mientras se cerraba el closeableHttpResponse en el método que busca al profesor de la reserva";

				log.error(errorString, ioException);
				throw new ReservaException(Constants.IO_EXCEPTION_FIREBASE, errorString, ioException);
			}
		}
	}

	/**
	 * Endpoint HTTP de tipo DELETE que permite cancelar una reserva fija de un
	 * recurso (como un aula o carrito) en un día y tramo horario específicos.
	 * <p>
	 * El usuario debe tener el rol de PROFESOR para poder acceder. Si el usuario
	 * tiene rol de ADMINISTRADOR o DIRECCIÓN, puede cancelar reservas de otros
	 * profesores. En caso contrario, solo puede cancelar sus propias reservas.
	 *
	 * @param usuario       Usuario autenticado extraído del token JWT.
	 * @param email         Email del profesor cuya reserva se desea cancelar.
	 * @param aulaYCarritos Identificador del recurso reservado.
	 * @param diaDeLaSemana Día de la semana en que se realizó la reserva.
	 * @param tramoHorario  Tramo horario de la reserva.
	 * @return 200 OK si la reserva se canceló correctamente, 404 si no se encontró
	 *         la reserva, 500 en caso de error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/bookings")
	public ResponseEntity<?> cancelarRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramoHorario", required = true) Long tramoHorario)
	{
		try
		{
			// Validaciones previas a la reserva
			this.validacionesGlobalesPreviasReservaFija(usuario);

			// Si el role del usuario es Administrador o Dirección, borrará la reserva con
			// el email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador o Dirección, se verificará primero
			// que el
			// email coincide con el que viene en DtoUsuario. Enviando excepción si no es
			// correcto

			// Antes de borrar la reserva verifica si existe una reserva con los mismos
			// datos
			Optional<ReservaFija> optinalReserva = this.reservaFijaRepository.encontrarReserva(email, recurso, diaDeLaSemana, tramoHorario);

			if (!optinalReserva.isPresent())
			{
				String mensajeError = "La reserva que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(Constants.RESERVA_NO_ENCONTRADA, mensajeError);
			}

			// Creamos instancia de ReservaId para luego borrar por este id
			ReservaFijaId reservaId = this.crearInstanciaDeReservaId(usuario, email, recurso, diaDeLaSemana, tramoHorario);

			// Si la reserva existe en la base de datos, se borrará
			this.reservaFijaRepository.deleteById(reservaId);

			Optional<DiaSemana> diaString = this.diasSemanaRepository.findById(diaDeLaSemana.toString());
			Optional<TramoHorario> tramoHorarioString = this.tramosHorariosRepository.findById(tramoHorario.toString());

			String profesor = this.profesoresRepository.getNombreProfesor(email);

			String usuarioRealizaAccion = "";

			if (usuario.getEmail().equals(email)
					&& (!usuario.getRoles().contains("ADMINISTRADOR") || !usuario.getRoles().contains("DIRECCION")))
			{
				usuarioRealizaAccion = "-";
			} else
			{
				usuarioRealizaAccion = usuario.getNombre() + " " + usuario.getApellidos();
			}

			// Creamos la instancia de LogReservas para el borrado de la reserva
			LogReservas logReservasBorrado = new LogReservas();

			// Añadimos los datos de la reserva al LogReservas
			logReservasBorrado.setUsuario(profesor);
			logReservasBorrado.setAccion("Borrar");
			logReservasBorrado.setTipo("Fija");
			logReservasBorrado.setRecurso(recurso);
			logReservasBorrado.setFechaReserva(new Date());
			logReservasBorrado.setDiaSemana(diaString.get().getDiaSemana());
			logReservasBorrado.setTramoHorario(tramoHorarioString.get().getTramoHorario());
			logReservasBorrado.setSuperUsuario(usuarioRealizaAccion);

			// Guardamos la reserva en la base de datos
			this.logReservasRepository.saveAndFlush(logReservasBorrado);

			// Logueamos
			log.info("La reserva fija se ha borrado correctamente");

			return ResponseEntity.ok().build();

		} catch (ReservaException reservaException)
		{
			// Si la reserva no existe, devolverá un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			// Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error inesperado al cancelar la reserva", exception);
			log.error("Error inesperado al cancelar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Crea una instancia de {@link ReservaFijaId} a partir de los datos del
	 * usuario, el recurso, el día de la semana y el tramo horario. Este
	 * identificador se usa para operaciones sobre reservas fijas (por ejemplo,
	 * cancelación).
	 * <p>
	 * Si el usuario tiene rol de ADMINISTRADOR o DIRECCIÓN, se utiliza el email
	 * proporcionado como referencia del profesor. En caso contrario, se toma el
	 * email del propio usuario autenticado.
	 *
	 * @param usuario       Usuario autenticado (DTO extendido con roles y JWT).
	 * @param email         Email del profesor asociado a la reserva (solo usado si
	 *                      el usuario es admin o dirección).
	 * @param recursoId Identificador del recurso reservado.
	 * @param diaDeLaSemana Día de la semana en que se realiza la reserva (ID).
	 * @param tramoHorario  Tramo horario en el que se realiza la reserva (ID).
	 * @return Objeto {@link ReservaFijaId} construido con los datos indicados.
	 */
	private ReservaFijaId crearInstanciaDeReservaId(DtoUsuarioExtended usuario, String email, String recursoId,
			Long diaDeLaSemana, Long tramoHorario)
	{
		Recurso recurso = new Recurso();
		recurso.setId(recursoId);

		DiaSemana diasSemana = new DiaSemana();
		diasSemana.setId(diaDeLaSemana);

		TramoHorario tramosHorarios = new TramoHorario();
		tramosHorarios.setId(tramoHorario);

		Optional<Profesor> profesor = null;

		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				|| usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			profesor = this.profesoresRepository.findById(email);
		} else
		{
			profesor = this.profesoresRepository.findById(usuario.getEmail());
		}

		ReservaFijaId reservaId = new ReservaFijaId();

		if (profesor.isPresent())
		{
			reservaId.setProfesor(profesor.get());
		}

		reservaId.setRecurso(recurso);
		reservaId.setDiaSemana(diasSemana);
		reservaId.setTramoHorario(tramosHorarios);
		return reservaId;
	}

	/**
	 * Realiza validaciones previas a la creación o eliminación de una reserva fija.
	 * <p>
	 * Si el usuario autenticado no tiene el rol de ADMINISTRADOR ni de DIRECCIÓN,
	 * se verifica si la funcionalidad de reservas fijas está deshabilitada mediante
	 * una entrada en la tabla de constantes del sistema. En caso de estar
	 * deshabilitada o si ocurre un error al consultar los parámetros, se lanza una
	 * {@link ReservaException}.
	 *
	 * @param usuario Usuario autenticado que intenta realizar la operación.
	 * @throws ReservaException si ocurre un error al obtener la configuración o si
	 *                          la aplicación está deshabilitada para reservas
	 *                          fijas.
	 */
	private void validacionesGlobalesPreviasReservaFija(DtoUsuarioExtended usuario) throws ReservaException
	{
		if (!usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				&& !usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			// Vemos si la reserva está deshabilitada
			Optional<Constantes> optionalAppDeshabilitada = this.constanteRepository
					.findByClave(Constants.TABLA_CONST_RESERVAS_FIJAS);

			if (!optionalAppDeshabilitada.isPresent())
			{
				String errorString = "Error obteniendo parametros";

				log.error(errorString + ". " + Constants.TABLA_CONST_RESERVAS_FIJAS);
				throw new ReservaException(Constants.ERROR_OBTENIENDO_PARAMETROS, errorString);
			}

			if (!optionalAppDeshabilitada.get().getValor().isEmpty())
			{
				String infoAppDeshabilitada = optionalAppDeshabilitada.get().getValor();
				if (infoAppDeshabilitada != null)
				{
					log.error(infoAppDeshabilitada);
					throw new ReservaException(Constants.ERROR_APP_DESHABILITADA, infoAppDeshabilitada);
				}
			}
		}
	}
}
