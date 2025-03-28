package es.iesjandula.reaktor.booking_server.rest;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
import es.iesjandula.reaktor.booking_server.repository.IReservaRepository;
import es.iesjandula.reaktor.booking_server.repository.ITramoHorarioRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/fixed")
@RestController
@Log4j2
public class ReservasFijasRest
{
	@Autowired
	private IRecursoRepository recursoRepository;

	@Autowired
	private IProfesorRepository profesoresRepository;

	@Autowired
	private IReservaRepository reservasRepository;

	@Autowired
	private IDiaSemanaRepository diasSemanaRepository;

	@Autowired
	private ITramoHorarioRepository tramosHorariosRepository;

	@Autowired
	private ConstantesRepository constanteRepository;

	@Value("${reaktor.firebase_server_url}")
	private String firebaseServerUrl;

	@Value("${reaktor.users_timeout}")
	private long usersTimeout;

	@Value("${reaktor.http_connection_timeout}")
	private int httpConnectionTimeout;

	/*
	 * Endpoint de tipo get para mostar una lista con los recursos
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

	/*
	 * Endpoint de tipo get para mostar una lista con los recursos
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

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/*
	 * Endpoint de tipo get para mostar una lista con los tramos horarios
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

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/*
	 * Endpoint de tipo get para mostar una lista con los días de la semana
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

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Recibe un recurso y devuelve una lista de recursos organizados por días y
	 * tramos horarios, para mostrarlos
	 * 
	 * @param recursos
	 * @return
	 * @throws ReservaException
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
			List<Object[]> resultados = this.reservasRepository.encontrarReservaPorRecurso(recurso);

			@SuppressWarnings("deprecation")
			Recurso recursoSeleccionado = this.recursoRepository.getById(recurso);

			Long diaSemana = 0l;
			Long tramoHorario = 0l;
			List<String> emails = new ArrayList<String>();
			List<Integer> nAlumnosLista = new ArrayList<Integer>();
			List<String> nombresYApellidos = new ArrayList<String>();
			ReservasFijasDto reserva = new ReservasFijasDto();
			Integer plazasRestantes = recursoSeleccionado.getCantidad();

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

					listaReservas.remove(reservaAntigua);

					listaReservas.add(reserva);
				}
				else
				{
					plazasRestantes = recursoSeleccionado.getCantidad();
					diaSemana = (Long) row[0];
					tramoHorario = (Long) row[1];
					Integer nAlumnos = (row[2] != null) ? (Integer) row[2] : 0;
					String email = (String) row[3];
					String nombreYapellidos = (String) row[4];
					String recursos = (String) row[5];
					plazasRestantes = plazasRestantes - nAlumnos;

					emails = new ArrayList<String>();
					emails.add(email);
					nombresYApellidos = new ArrayList<String>();
					nombresYApellidos.add(nombreYapellidos);
					nAlumnosLista = new ArrayList<Integer>();
					nAlumnosLista.add(nAlumnos);

					reserva = new ReservasFijasDto(diaSemana, tramoHorario, nAlumnosLista, emails, nombresYApellidos,
							recursos, plazasRestantes);
					// Mapeo a ReservaDto
					listaReservas.add(reserva);
				}

			}
			// Encontramos todos los recursos y los introducimos en una lista para
			// mostrarlos más adelante

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
					"Error al acceder a la bade de datos", exception);

			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo post para realizar una reserva con un correo de un profesor,
	 * un recurso, un día de la semana, un tramo horario, un profesor y un número de
	 * alumnos
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/bookings")
	public ResponseEntity<?> realizarReservaFija(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String recurso,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramosHorarios", required = true) Long tramosHorarios,
			@RequestHeader(value = "nAlumnos", required = true) int nAlumnos)
	{
		try
		{
			// Validaciones previas a la reserva
			this.validacionesGlobalesPreviasReservaFija(usuario);

			// Si el role del usuario es Administrador o Dirección, creará la reserva con el email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador o Dirección, se verificará primero que el
			// email coincide con el que viene en DtoUsuario.
			// Enviando excepción si no es correcto

			// Verifica si ya existe una reserva con los mismos datos
			Optional<ReservaFija> optionalReserva = this.reservasRepository.encontrarReserva(email, recurso,
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

			// Creamos la instancia de reserva
			ReservaFija reserva = this.crearInstanciaDeReserva(usuario, email, recurso, diaDeLaSemana, tramosHorarios,
					nAlumnos);

			// Si no existe una reserva previa, se guarda la nueva reserva en la base de
			// datos
			this.reservasRepository.saveAndFlush(reserva);

			log.info("Se ha reservado correctamente");

			return ResponseEntity.ok().body("Reserva realizada correctamente");

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
	 * @param usuario       usuario
	 * @param email         email
	 * @param recursoString recurso previo
	 * @param diaSemana     dia de la semana
	 * @param tramoHorario
	 * @param nAlumnos
	 * @return
	 * @throws ReservaException
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
	 * @param usuario usuario
	 * @param email   email
	 * @return el profesor encontrado
	 * @throws ReservaException con un error
	 */
	public Profesor buscarProfesor(DtoUsuarioExtended usuario, String email) throws ReservaException
	{
		Profesor profesor = null;

		// Si el role es administrador o dirección ...
		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR) || usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			// Primero buscamos si ya tenemos a ese profesor en nuestra BBDD
			Optional<Profesor> optionalProfesor = this.profesoresRepository.findById(email);

			// Si lo encontramos ...
			if (!optionalProfesor.isEmpty())
			{
				// Lo cogemos del optional
				profesor = optionalProfesor.get();
			}
			else
			{
				// Si no lo encontramos, le pedimos a Firebase que nos lo dé
				profesor = this.buscarProfesorEnFirebase(usuario.getJwt(), email);
			}
		}
		else
		{
			// Si el usuario no es administrador o dirección, cogemos la información del usuario
			profesor = new Profesor(usuario.getEmail(), usuario.getNombre(), usuario.getApellidos());

			// Lo almacenamos en BBDD en caso de que no exista
			this.profesoresRepository.saveAndFlush(profesor);
		}

		return profesor;
	}

	/**
	 * @param jwtAdmin JWT del usuario admin
	 * @param email    email del profesor que va a realizar la reserva
	 * @return el profesor encontrado enfirebase
	 * @throws ReservaException con un error
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

			// Comprobamos si viene la cabecera. En caso afirmativo, es porque trae un profesor
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
		}
		catch (SocketTimeoutException socketTimeoutException)
		{
			String errorString = "SocketTimeoutException de lectura o escritura al comunicarse con el servidor (búsqueda del profesor asociado a la reserva)";

			log.error(errorString, socketTimeoutException);
			throw new ReservaException(Constants.ERROR_CONEXION_FIREBASE, errorString, socketTimeoutException);
		}
		catch (ConnectTimeoutException connectTimeoutException)
		{
			String errorString = "ConnectTimeoutException al intentar conectar con el servidor (búsqueda del profesor asociado a la reserva)";

			log.error(errorString, connectTimeoutException);
			throw new ReservaException(Constants.TIMEOUT_CONEXION_FIREBASE, errorString, connectTimeoutException);
		}
		catch (IOException ioException)
		{
			String errorString = "IOException mientras se buscaba el profesor asociado a la reserva";

			log.error(errorString, ioException);
			throw new ReservaException(Constants.IO_EXCEPTION_FIREBASE, errorString, ioException);
		}
		finally
		{
			// Cierre de flujos
			this.buscarProfesorEnFirebaseCierreFlujos(closeableHttpResponse);
		}

		return profesor;
	}

	/**
	 * @param closeableHttpResponse closeable HTTP response
	 * @throws PrinterClientException printer client exception
	 */
	private void buscarProfesorEnFirebaseCierreFlujos(CloseableHttpResponse closeableHttpResponse)
			throws ReservaException
	{
		if (closeableHttpResponse != null)
		{
			try
			{
				closeableHttpResponse.close();
			}
			catch (IOException ioException)
			{
				String errorString = "IOException mientras se cerraba el closeableHttpResponse en el método que busca al profesor de la reserva";

				log.error(errorString, ioException);
				throw new ReservaException(Constants.IO_EXCEPTION_FIREBASE, errorString, ioException);
			}
		}
	}

	/**
	 * Endpoint de tipo post para cancelar una reserva con un correo de un profesor,
	 * un recurso, un día de la semana, un tramo horario
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/bookings")
	public ResponseEntity<?> cancelarRecurso(@AuthenticationPrincipal DtoUsuarioExtended usuario,
			@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String aulaYCarritos,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramoHorario", required = true) Long tramoHorario)
	{
		try
		{
			// Validaciones previas a la reserva
			this.validacionesGlobalesPreviasReservaFija(usuario);

			// Si el role del usuario es Administrador o Dirección, borrará la reserva con el email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador o Dirección, se verificará primero que el
			// email coincide con el que viene en DtoUsuario. Enviando excepción si no es
			// correcto

			// Antes de borrar la reserva verifica si existe una reserva con los mismos
			// datos
			Optional<ReservaFija> optinalReserva = this.reservasRepository.encontrarReserva(email, aulaYCarritos,
					diaDeLaSemana, tramoHorario);

			if (!optinalReserva.isPresent())
			{
				String mensajeError = "La reserva que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(Constants.RESERVA_NO_ENCONTRADA, mensajeError);
			}

			// Creamos instancia de ReservaId para luego borrar por este id
			ReservaFijaId reservaId = this.crearInstanciaDeReservaId(usuario, email, aulaYCarritos, diaDeLaSemana,
					tramoHorario);

			// Si la reserva existe en la base de datos, se borrará
			this.reservasRepository.deleteById(reservaId);

			log.info("La reserva se ha borrado correctamente");
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
	 * @param usuario
	 * @param email
	 * @param aulaYCarritos
	 * @param diaDeLaSemana
	 * @param tramoHorario
	 * @return
	 */
	private ReservaFijaId crearInstanciaDeReservaId(DtoUsuarioExtended usuario, String email, String aulaYCarritos,
			Long diaDeLaSemana, Long tramoHorario)
	{
		Recurso recurso = new Recurso();
		recurso.setId(aulaYCarritos);

		DiaSemana diasSemana = new DiaSemana();
		diasSemana.setId(diaDeLaSemana);

		TramoHorario tramosHorarios = new TramoHorario();
		tramosHorarios.setId(tramoHorario);

		Optional<Profesor> profesor = null;

		if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR) || usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{
			profesor = this.profesoresRepository.findById(email);
		}
		else
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
	 * @throws ReservaException con un error
	 */
	private void validacionesGlobalesPreviasReservaFija(DtoUsuarioExtended usuario) throws ReservaException
	{
		if (!usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR) && !usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
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
