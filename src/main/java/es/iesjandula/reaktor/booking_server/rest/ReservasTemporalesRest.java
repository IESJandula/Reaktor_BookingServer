package es.iesjandula.reaktor.booking_server.rest;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.base.utils.HttpClientUtils;
import es.iesjandula.reaktor.booking_server.dto.ReservasPuntualesDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.Constantes;
import es.iesjandula.reaktor.booking_server.models.LogReservas;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservaFija;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramoHorario;
import es.iesjandula.reaktor.booking_server.models.reservas_temporales.ReservaTemporal;
import es.iesjandula.reaktor.booking_server.models.reservas_temporales.ReservaTemporalId;
import es.iesjandula.reaktor.booking_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.booking_server.repository.IDiaSemanaRepository;
import es.iesjandula.reaktor.booking_server.repository.IProfesorRepository;
import es.iesjandula.reaktor.booking_server.repository.IRecursoRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaFijaRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaTemporalRepository;
import es.iesjandula.reaktor.booking_server.repository.ITramoHorarioRepository;
import es.iesjandula.reaktor.booking_server.repository.LogReservasRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;

@RequestMapping(value = "/bookings/temporary")
@RestController
public class ReservasTemporalesRest
{
	private static final Logger log = LoggerFactory.getLogger(ReservasTemporalesRest.class);

	@Autowired
	private IRecursoRepository recursoRepository;

	@Autowired
	private IProfesorRepository profesoresRepository;

	@Autowired
	private IReservaFijaRepository reservaFijaRepository;

	@Autowired
	private IReservaTemporalRepository reservaTemporalRepository;

	@Autowired
	private ConstantesRepository constanteRepository;

	@Autowired
	private IDiaSemanaRepository diasSemanaRepository;

	@Autowired
	private ITramoHorarioRepository tramosHorariosRepository;

	@Autowired
	private LogReservasRepository logReservasRepository;

	@Value("${reaktor.firebase_server_url}")
	private String firebaseServerUrl;

	@Value("${reaktor.http_connection_timeout}")
	private int httpConnectionTimeout;

	/**
	 * Endpoint de tipo GET que devuelve una lista de reservas puntuales organizadas
	 * por día de la semana y tramo horario.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/bookings")
	public ResponseEntity<?> obtenerReservasDto(@RequestHeader(value = "aulaYCarritos") String recurso,
			@RequestHeader(value = "numSemana") Integer numSemana)
	{
		try
		{
			List<ReservasPuntualesDto> listaReservas = new ArrayList<ReservasPuntualesDto>();

			if (this.recursoRepository.count() == 0)
			{
				String mensajeError = "No se ha encontrado ningun recurso con esos datos: " + recurso;
				log.error(mensajeError);
				throw new ReservaException(Constants.RECURSO_NO_ENCONTRADO, mensajeError);
			}

			List<Object[]> resultados = this.reservaTemporalRepository.encontrarReservaPorRecurso(recurso, numSemana);

			@SuppressWarnings("deprecation")
			Recurso recursoSeleccionado = this.recursoRepository.getById(recurso);

			Long diaSemana = 0l;
			Long tramoHorario = 0l;
			List<String> emails = new ArrayList<String>();
			List<Integer> nAlumnosLista = new ArrayList<Integer>();
			List<String> nombresYApellidos = new ArrayList<String>();
			ReservasPuntualesDto reserva = new ReservasPuntualesDto();
			Integer plazasRestantes = recursoSeleccionado.getCantidad();
			List<Long> esFijaLista = new ArrayList<Long>();
			List<String> motivoCursoLista = new ArrayList<String>();
			List<Long> esSemanalLista = new ArrayList<Long>();

			for (Object[] row : resultados)
			{
				if (diaSemana == (Long) row[0] && tramoHorario == (Long) row[1])
				{
					ReservasPuntualesDto reservaAntigua = reserva;

					emails = reserva.getEmail();
					nombresYApellidos = reserva.getNombreYapellidos();
					nAlumnosLista = reserva.getNAlumnos();
					plazasRestantes = reserva.getPlazasRestantes();
					esFijaLista = reserva.getEsfija();
					esSemanalLista = reserva.getEsSemanal();

					emails.add((String) row[3]);
					nombresYApellidos.add((String) row[4]);
					nAlumnosLista.add((row[2] != null) ? (Integer) row[2] : 0);
					plazasRestantes = plazasRestantes - ((row[2] != null) ? (Integer) row[2] : 0);
					esFijaLista.add((Long) row[6]);
					motivoCursoLista.add((String) row[7]);
					BigDecimal esSemanalBD = (BigDecimal) row[8];
					Long esSemanal = esSemanalBD != null ? esSemanalBD.longValue() : null;
					esSemanalLista.add(esSemanal);

					reserva.setEmail(emails);
					reserva.setNombreYapellidos(nombresYApellidos);
					reserva.setNAlumnos(nAlumnosLista);
					reserva.setPlazasRestantes(plazasRestantes);
					reserva.setEsfija(esFijaLista);
					reserva.setMotivoCurso(motivoCursoLista);
					reserva.setEsSemanal(esSemanalLista);

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
					Long esFija = (Long) row[6];
					String motivoCurso = (String) row[7];
					plazasRestantes = plazasRestantes - nAlumnos;

					// Extraer esSemanal (posición 8)
					BigDecimal esSemanalBD = (BigDecimal) row[8];
					Long esSemanal = esSemanalBD != null ? esSemanalBD.longValue() : null;

					// Extraer numSemana (posición 9)
					Integer numSemanaValor = null;
					Object numSemanaObj = row[9];
					if (numSemanaObj != null)
					{
						if (numSemanaObj instanceof Number)
						{
							numSemanaValor = ((Number) numSemanaObj).intValue();
						}
					}

					// Inicializar listas
					emails = new ArrayList<String>();
					emails.add(email);
					nombresYApellidos = new ArrayList<String>();
					nombresYApellidos.add(nombreYapellidos);
					nAlumnosLista = new ArrayList<Integer>();
					nAlumnosLista.add(nAlumnos);
					esFijaLista = new ArrayList<Long>();
					esFijaLista.add(esFija);
					motivoCursoLista = new ArrayList<String>();
					motivoCursoLista.add(motivoCurso);
					esSemanalLista = new ArrayList<Long>();
					esSemanalLista.add(esSemanal);

					// Crear el DTO con todos los campos
					reserva = new ReservasPuntualesDto(diaSemana, tramoHorario, nAlumnosLista, emails,
							nombresYApellidos, recursos, plazasRestantes, esFijaLista, motivoCursoLista, esSemanalLista,
							numSemanaValor);
					listaReservas.add(reserva);

				}
			}
			return ResponseEntity.ok(listaReservas);
		} catch (ReservaException reservaException)
		{
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);
			log.error("Error al acceder a la base de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint POST para crear una reserva temporal de un recurso.
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
			this.validacionesGlobalesPreviasReservaTemporal(usuario);

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

			ReservaTemporal reserva = this.crearInstanciaDeReserva(usuario, email, recurso, diaDeLaSemana,
					tramosHorarios, nAlumnos, numSemana, esSemanal);

			reserva.setMotivoCurso(motivoCurso);

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
			} else
			{
				usuarioRealizaAccion = usuario.getNombre() + " " + usuario.getApellidos();
			}

			LogReservas logReservasCreacion = new LogReservas();
			logReservasCreacion.setUsuario(profesor);
			logReservasCreacion.setAccion("Crear");
			logReservasCreacion.setTipo("Temporal");
			logReservasCreacion.setRecurso(recurso);
			logReservasCreacion.setFechaReserva(new Date());
			logReservasCreacion.setDiaSemana(diaString.get().getDiaSemana());
			logReservasCreacion.setTramoHorario(tramoHorarioString.get().getTramoHorario());
			logReservasCreacion.setSuperUsuario(usuarioRealizaAccion);

			this.logReservasRepository.saveAndFlush(logReservasCreacion);

			log.info("La reserva temporal se ha creado correctamente");
			return ResponseEntity.ok().build();

		} catch (ReservaException reservaException)
		{
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error inesperado al realizar la reserva", exception);
			log.error("Error inesperado al realizar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Crea una instancia de {@link ReservaTemporal} con los datos recibidos.
	 */
	private ReservaTemporal crearInstanciaDeReserva(DtoUsuarioExtended usuario, String email, String recursoString,
			Long diaSemana, Long tramoHorario, int nAlumnos, Integer numSemana, Boolean esSemanal)
			throws ReservaException
	{
		Recurso recurso = new Recurso();
		recurso.setId(recursoString);

		DiaSemana diasSemana = new DiaSemana();
		diasSemana.setId(diaSemana);

		TramoHorario tramos = new TramoHorario();
		tramos.setId(tramoHorario);

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
	 * Recupera la entidad {@link Profesor} correspondiente al email indicado.
	 */
	private Profesor buscarProfesor(DtoUsuarioExtended usuario, String email) throws ReservaException
	{
		Profesor profesor = null;

		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				|| usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			Optional<Profesor> optionalProfesor = this.profesoresRepository.findById(email);
			if (!optionalProfesor.isEmpty())
			{
				profesor = optionalProfesor.get();
			} else
			{
				profesor = this.buscarProfesorEnFirebase(usuario.getJwt(), email);
			}
		} else
		{
			profesor = new Profesor(usuario.getEmail(), usuario.getNombre(), usuario.getApellidos());
			this.profesoresRepository.saveAndFlush(profesor);
		}

		return profesor;
	}

	/**
	 * Consulta a Firebase para recuperar los datos de un profesor.
	 */
	private Profesor buscarProfesorEnFirebase(String jwtAdmin, String email) throws ReservaException
	{
		Profesor profesor = null;
		CloseableHttpClient closeableHttpClient = HttpClientUtils.crearHttpClientConTimeout(this.httpConnectionTimeout);
		CloseableHttpResponse closeableHttpResponse = null;

		try
		{
			HttpGet httpGet = new HttpGet(this.firebaseServerUrl + "/firebase/queries/user");
			httpGet.addHeader("Authorization", "Bearer " + jwtAdmin);
			httpGet.addHeader("email", email);

			closeableHttpResponse = closeableHttpClient.execute(httpGet);

			if (closeableHttpResponse.getEntity() == null)
			{
				String mensajeError = "Profesor no encontrado en BBDD Global";
				log.error(mensajeError);
				throw new ReservaException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError);
			}

			ObjectMapper objectMapper = new ObjectMapper();
			DtoUsuarioBase dtoUsuarioBase = objectMapper.readValue(closeableHttpResponse.getEntity().getContent(),
					DtoUsuarioBase.class);

			profesor = new Profesor();
			profesor.setNombre(dtoUsuarioBase.getNombre());
			profesor.setApellidos(dtoUsuarioBase.getApellidos());
			profesor.setEmail(dtoUsuarioBase.getEmail());

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
			this.buscarProfesorEnFirebaseCierreFlujos(closeableHttpResponse);
		}

		return profesor;
	}

	/**
	 * Cierra de forma segura el flujo {@link CloseableHttpResponse}.
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
	 * Endpoint DELETE corregido para cancelar una reserva temporal. Se ha
	 * añadido @Transactional y eliminado la duplicación de deleteAll.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/bookings")
	@Transactional
	public ResponseEntity<?> borrarReserva(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramoHorario", required = true) Long tramoHorario,
			@RequestHeader(value = "numSemana", required = true) Integer numSemana,
			@RequestHeader(value = "esSemanal", required = false) Boolean esSemanal)
	{
		try
		{
			log.info("Borrar reserva temporal: email={}, recurso={}, dia={}, tramo={}, numSemana={}, esSemanal={}",
	                 email, recurso, diaDeLaSemana, tramoHorario, numSemana, esSemanal);
			
			this.validacionesGlobalesPreviasReservaTemporal(usuario);

			if (usuario.getRoles() != null && usuario.getRoles().size() == 1
					&& usuario.getRoles().contains(BaseConstants.ROLE_PROFESOR) && !email.equals(usuario.getEmail()))
			{
				String mensajeError = "No puedes borrar reservas de otras personas";
				log.error(mensajeError);
				throw new ReservaException(Constants.ERROR_CANCELANDO_RESERVA, mensajeError);
			}

			Optional<ReservaTemporal> optinalReserva = this.reservaTemporalRepository.encontrarReserva(email, recurso,
					diaDeLaSemana, tramoHorario, numSemana);
			
			// Log para saber si encontró la reserva
			if (optinalReserva.isPresent()) {
			    log.info("Reserva encontrada, se procede a borrar");
			} else {
			    log.warn("Reserva NO encontrada con esos parámetros");
			}

			if (!optinalReserva.isPresent())
			{
				String mensajeError = "La reserva que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(Constants.RESERVA_NO_ENCONTRADA, mensajeError);
			}

			ReservaTemporalId reservaId = this.crearInstanciaDeReservaId(usuario, email, recurso, diaDeLaSemana,
					tramoHorario, numSemana);

			Integer semanaInicial = numSemana;
			List<ReservaTemporal> listaReservasBorrado = new ArrayList<>();

			if (esSemanal != null && esSemanal)
			{
				// Buscar hacia atrás
				numSemana = semanaInicial;
				ReservaTemporal reservaIterable = null;
				do
				{
					Optional<ReservaTemporal> opt = this.reservaTemporalRepository.encontrarReserva(email, recurso,
							diaDeLaSemana, tramoHorario, numSemana);
					if (opt.isEmpty())
						break;
					reservaIterable = opt.get();
					listaReservasBorrado.add(reservaIterable);
					numSemana--;
				} while (reservaIterable != null && reservaIterable.isEsSemanal());

				// Buscar hacia adelante
				numSemana = semanaInicial;
				reservaIterable = null;
				do
				{
					numSemana++;
					Optional<ReservaTemporal> opt = this.reservaTemporalRepository.encontrarReserva(email, recurso,
							diaDeLaSemana, tramoHorario, numSemana);
					if (opt.isEmpty())
						break;
					reservaIterable = opt.get();
					listaReservasBorrado.add(reservaIterable);
				} while (reservaIterable != null && reservaIterable.isEsSemanal());

				this.reservaTemporalRepository.deleteAll(listaReservasBorrado);
			} else
			{
				this.reservaTemporalRepository.deleteById(reservaId);
			}

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

			LocalDate fecha = LocalDate.now().with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, numSemana)
					.with(ChronoField.DAY_OF_WEEK, diaDeLaSemana);
			DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			String fechaFormateada = fecha.format(formato);

			LogReservas logBorrado = new LogReservas();
			logBorrado.setUsuario(profesor);
			logBorrado.setAccion("Borrar");
			logBorrado.setTipo("Temporal");
			logBorrado.setRecurso(recurso);
			logBorrado.setFechaReserva(new Date());
			logBorrado.setDiaSemana(diaString.get().getDiaSemana());
			logBorrado.setTramoHorario(tramoHorarioString.get().getTramoHorario());
			logBorrado.setSuperUsuario(usuarioRealizaAccion);

			this.logReservasRepository.saveAndFlush(logBorrado);

			log.info("La reserva temporal se ha borrado correctamente");
			return ResponseEntity.ok().build();

		} catch (ReservaException reservaException)
		{
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error inesperado al cancelar la reserva", exception);
			log.error("Error inesperado al cancelar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Crea una instancia de {@link ReservaTemporalId} a partir de los parámetros.
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
		} else
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
	 * Validaciones globales previas a la creación o cancelación de una reserva
	 * temporal.
	 */
	private void validacionesGlobalesPreviasReservaTemporal(DtoUsuarioExtended usuario) throws ReservaException
	{
		if (!usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				&& !usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			Optional<Constantes> optionalAppDeshabilitada = this.constanteRepository
					.findByClave(Constants.TABLA_CONST_RESERVAS_TEMPORALES);

			if (!optionalAppDeshabilitada.isPresent())
			{
				String errorString = "Error obteniendo parametros";
				log.error(errorString + ". " + Constants.TABLA_CONST_RESERVAS_TEMPORALES);
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

	/**
	 * Endpoint que permite comprobar si un recurso está disponible.
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
				semanas.removeIf(num -> !sinRepetir.add(num));

				Optional<List<ReservaFija>> optionalListFijas = this.reservaFijaRepository
						.encontrarReservasFijasPorDiaTramo(recurso, diaDeLaSemana, tramoHorario);

				int contadorReservaFija = 0;

				Recurso recursoInstancia = this.recursoRepository.findById(recurso).get();

				if (optionalListFijas.isPresent())
				{
					disponible = recursoInstancia.isEsCompartible();
					if (disponible)
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
						if (disponible)
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
		} catch (Exception exception)
		{
			ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO,
					"Error al acceder a la base de datos", exception);
			log.error("Error al acceder a la base de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
}