package es.iesjandula.reaktor.bookings_server.rest;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
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
import es.iesjandula.reaktor.base_client.utils.HttpClientUtils;
import es.iesjandula.reaktor.bookings_server.dto.ReservasFijasDto;
import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.models.Constantes;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.DiasSemana;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.Profesores;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursosPrevios;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.ReservaFijas;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.ReservasFijasId;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.TramosHorarios;
import es.iesjandula.reaktor.bookings_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.bookings_server.repository.IDiasSemanaRepository;
import es.iesjandula.reaktor.bookings_server.repository.IProfesoresRepository;
import es.iesjandula.reaktor.bookings_server.repository.IRecursosRepository;
import es.iesjandula.reaktor.bookings_server.repository.IReservasRepository;
import es.iesjandula.reaktor.bookings_server.repository.ITramosHorariosRepository;
import es.iesjandula.reaktor.bookings_server.utils.Constants;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/fixed", produces =
{ "application/json" })
@RestController
@Log4j2
public class ReservasFijasRest
{
	@Autowired
	private IRecursosRepository recursosRepository;

	@Autowired
	private IProfesoresRepository profesoresRepository;

	@Autowired
	private IReservasRepository reservasRepository;

	@Autowired
	private IDiasSemanaRepository diasSemanaRepository;

	@Autowired
	private ITramosHorariosRepository tramosHorariosRepository;

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
	@RequestMapping(method = RequestMethod.GET, value = "/resources")
	public ResponseEntity<?> obtenerRecurso()
	{
		try
		{
//			Creacion de una lista para almacenar los recursos
			List<RecursosPrevios> listaRecursosPrevios;

//			Comprueba si la base de datos tiene registros de los recurso
			if (this.recursosRepository.findAll().isEmpty())
			{
				String mensajeError = "No se ha encontrado ningun recurso";
				log.error(mensajeError);
				throw new ReservaException(1, mensajeError);
			}

//			Encontramos todos los recursos y los introducimos en una lista para mostrarlos más adelante
			listaRecursosPrevios = this.recursosRepository.findAll();

			return ResponseEntity.ok(listaRecursosPrevios);
		} catch (ReservaException reservaException)
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error al acceder a la bade de datos",
					exception);
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
//			Comprueba si la base de datos tiene registros de los tramos horarios
			if (this.tramosHorariosRepository.findAll().isEmpty())
			{
				String mensajeError = "No se ha encontrado ningun tramo horario";
				log.error(mensajeError);
				throw new ReservaException(2, mensajeError);
			}
//			Encontramos todos los tramos y los introducimos en una lista para mostrarlos más adelante
			List<TramosHorarios> listaTramos = this.tramosHorariosRepository.findAll();
			return ResponseEntity.ok(listaTramos);
		} catch (ReservaException reservaException)
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error al acceder a la bade de datos",
					exception);
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
//			Creacion de una lista para almacenar los dias de la semana
			List<DiasSemana> listaDias;

//			Comprueba si la base de datos tiene registros de los días de la semana
			if (this.diasSemanaRepository.findAll().isEmpty())
			{

				String mensajeError = "Error al obtener los días de la semana";
				log.error(mensajeError);
				throw new ReservaException(3, mensajeError);
			}

//			Encontramos todos los dias y los introducimos en una lista para mostrarlos más adelante
			listaDias = this.diasSemanaRepository.findAll();

			return ResponseEntity.ok(listaDias);
		} catch (ReservaException reservaException)
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error al acceder a la bade de datos",
					exception);
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
	public ResponseEntity<?> obtenerReservasDto(@RequestHeader(value = "aulaYCarritos") String recursoA)
	{
		try
		{
//			Creacion de una lista para almacenar los recursos
			List<ReservasFijasDto> listaReservas = new ArrayList<ReservasFijasDto>();
			List<Object[]> resultados = reservasRepository.encontrarReservaPorRecurso(recursoA);

//			Comprueba si la base de datos tiene registros de los recurso
			if (this.recursosRepository.findAll().isEmpty())
			{
				String mensajeError = "No se ha encontrado ningun recurso";
				log.error(mensajeError);
				throw new ReservaException(1, mensajeError);
			}

			for (Object[] row : resultados)
			{
				Long diaSemana = (Long) row[0];
				Long tramoHorario = (Long) row[1];
				Integer nAlumnos = (row[2] != null) ? (Integer) row[2] : 0;
				String email = (String) row[3];
				String nombreYapellidos = (String) row[4];
				String recurso = (String) row[5];

				// Mapeo a ReservaDto
				listaReservas
						.add(new ReservasFijasDto(diaSemana, tramoHorario, nAlumnos, email, nombreYapellidos, recurso));
			}
//			Encontramos todos los recursos y los introducimos en una lista para mostrarlos más adelante

			return ResponseEntity.ok(listaReservas);
		} catch (ReservaException reservaException)
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error al acceder a la bade de datos",
					exception);
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
			@RequestHeader(value = "recurso", required = true) String aulaYCarritos,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramosHorarios", required = true) Long tramosHorarios,
			@RequestHeader(value = "nAlumnos", required = true) int nAlumnos)
	{
		try
		{
			String errorReserva = this.validacionesGlobalesPreviasReservaFija();

			if (errorReserva != null)
			{
				log.error(errorReserva);
				throw new ReservaException(22, errorReserva);
			}
			// Si el role del usuario es Administrador, creará la reserva con el email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador, se verificará primero que el
			// email coincide con el que viene en DtoUsuario. Enviando excepción si no es
			// correcto

			// Verifica si ya existe una reserva con los mismos datos
			Optional<ReservaFijas> optinalReserva = this.reservasRepository.encontrarReserva(aulaYCarritos,
					diaDeLaSemana, tramosHorarios);

			if (optinalReserva.isPresent())
			{
				String mensajeError = "Ya existe una la reserva con esos datos";
				log.error(mensajeError);
				throw new ReservaException(6, mensajeError);
			}

			RecursosPrevios recurso = new RecursosPrevios();
			recurso.setAulaYCarritos(aulaYCarritos);

			DiasSemana diasSemana = new DiasSemana();
			diasSemana.setId(diaDeLaSemana);

			TramosHorarios tramos = new TramosHorarios();
			tramos.setId(tramosHorarios);

			Profesores profesor = null;
			Optional<Profesores> optionalProfesor = null;

			if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR))
			{
				optionalProfesor = this.profesoresRepository.findById(usuario.getEmail());

				if (optionalProfesor.isEmpty())
				{
					profesor = new Profesores(usuario.getEmail(), usuario.getNombre(), usuario.getApellidos());
					this.profesoresRepository.saveAndFlush(profesor);
				}
				else
				{
					// Creamos un HTTP Client con Timeout
					CloseableHttpClient closeableHttpClient = HttpClientUtils
							.crearHttpClientConTimeout(this.httpConnectionTimeout);

					CloseableHttpResponse closeableHttpResponse = null;

					try
					{
						HttpGet httpGet = new HttpGet(this.firebaseServerUrl + "/firebase/queries/user");

						// Añadimos el token a la llamada
						httpGet.addHeader("Authorization", "Bearer " + usuario.getJwt());

						// Hacemos la peticion
						closeableHttpResponse = closeableHttpClient.execute(httpGet);

						// Comprobamos si viene la cabecera. En caso afirmativo, es porque trae un
						// fichero a imprimir
						if (closeableHttpResponse.containsHeader(Constants.HEADER_PRINT_CONTENT_DISPOSITION))
						{
							// Convertimos la respuesta en un objeto DtoInfoUsuario
							ObjectMapper objectMapper = new ObjectMapper();
							DtoUsuarioBase dtoUsuarioBase = objectMapper
									.readValue(closeableHttpResponse.getEntity().getContent(), DtoUsuarioBase.class);

							profesor.setNombre(dtoUsuarioBase.getNombre()) ;
							profesor.setApellidos(dtoUsuarioBase.getApellidos());
							profesor.setEmail(dtoUsuarioBase.getEmail()) ;
							
							this.profesoresRepository.saveAndFlush(profesor);
						}
					} catch (SocketTimeoutException socketTimeoutException)
					{
						String errorString = "SocketTimeoutException de lectura o escritura al comunicarse con el servidor (búsqueda de tarea de impresión)";

						log.error(errorString, socketTimeoutException);
						throw new ReservaException(1, errorString, socketTimeoutException);
					} catch (ConnectTimeoutException connectTimeoutException)
					{
						String errorString = "ConnectTimeoutException al intentar conectar con el servidor (búsqueda de tarea de impresión)";

						log.error(errorString, connectTimeoutException);
						throw new ReservaException(2, errorString, connectTimeoutException);
					} catch (IOException ioException)
					{
						String errorString = "IOException mientras se buscaba la tarea para imprimir en el servidor";

						log.error(errorString, ioException);
						throw new ReservaException(3, errorString, ioException);
					}
					finally
					{
						// Cierre de flujos
						this.cierreFlujos(closeableHttpResponse);
					}
				}
			}
			else
			{
				optionalProfesor = this.profesoresRepository.findById(email);
				profesor = optionalProfesor.get();
			}

			ReservasFijasId reservaId = new ReservasFijasId();

			reservaId.setProfesor(profesor);
			reservaId.setAulaYCarritos(recurso);
			reservaId.setDiasDeLaSemana(diasSemana);
			reservaId.setTramosHorarios(tramos);

			ReservaFijas reserva = new ReservaFijas();
			reserva.setReservaId(reservaId);
			reserva.setNAlumnos(nAlumnos);

			log.info("Se ha reservado correctamente");

			reserva.setReservaId(reservaId);

//			Si no existe una reserva previa, se guarda la nueva reserva en la base de datos
			this.reservasRepository.saveAndFlush(reserva);

			return ResponseEntity.ok().body("Reserva realizada correctamente");

		} catch (ReservaException reservaException)
		{

//			Captura la excepcion personalizada y retorna un 409 ya que existe un conflicto,
//			que existe una reserva con los mismos datos
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
//			Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error inesperado al realizar la reserva",
					exception);

			log.error("Error inesperado al realizar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}

	}

	/**
	 * @param closeableHttpResponse closeable HTTP response
	 * @throws PrinterClientException printer client exception
	 */
	private void cierreFlujos(CloseableHttpResponse closeableHttpResponse) throws ReservaException
	{
		if (closeableHttpResponse != null)
		{
			try
			{
				closeableHttpResponse.close();
			} catch (IOException ioException)
			{
				String errorString = "IOException mientras se cerraba el closeableHttpResponse en el método que busca la tarea para imprimir en el servidor";

				log.error(errorString, ioException);
				throw new ReservaException(4, errorString, ioException);
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
			@RequestHeader(value = "tramoHorario", required = true) Long tramoHorario,
			@RequestHeader(value = "numeroSemna", required = true) Integer numeroSemana)
	{
		try
		{
			String errorReserva = this.validacionesGlobalesPreviasReservaFija();

			if (errorReserva != null)
			{
				log.error(errorReserva);
				throw new ReservaException(22, errorReserva);
			}
			// Si el role del usuario es Administrador, borrará la reserva con el email
			// recibido en la cabecera
			// Si el role del usuario no es Administrador, se verificará primero que el
			// email coincide con el que viene en DtoUsuario. Enviando excepción si no es
			// correcto

			// Antes de borrar la reserva verifica si existe una reserva con los mismos
			// datos
			Optional<ReservaFijas> optinalReserva = this.reservasRepository.encontrarReserva(aulaYCarritos,
					diaDeLaSemana, tramoHorario);

			if (!optinalReserva.isPresent())
			{
				String mensajeError = "La reserva que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(7, mensajeError);
			}

			RecursosPrevios recurso = new RecursosPrevios();
			recurso.setAulaYCarritos(aulaYCarritos);

			DiasSemana diasSemana = new DiasSemana();
			diasSemana.setId(diaDeLaSemana);

			TramosHorarios tramosHorarios = new TramosHorarios();
			tramosHorarios.setId(tramoHorario);

			Optional<Profesores> profesor = null;

			if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR))
			{
				profesor = this.profesoresRepository.findById(email);
			} else
			{
				profesor = this.profesoresRepository.findById(usuario.getEmail());
			}

			ReservasFijasId reservaId = new ReservasFijasId();

			if (profesor.isPresent())
			{
				reservaId.setProfesor(profesor.get());
			}

			reservaId.setAulaYCarritos(recurso);
			reservaId.setDiasDeLaSemana(diasSemana);
			reservaId.setTramosHorarios(tramosHorarios);

			log.info("La reserva se ha borrado correctamente");

			// Si la reserva existe en la base de datos, se borrará
			this.reservasRepository.deleteById(reservaId);

			return ResponseEntity.ok().build();

		} catch (ReservaException reservaException)
		{
//			Si la reserva no existe, devolverá un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		} catch (Exception exception)
		{
//			Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(100, "Error inesperado al cancelar la reserva",
					exception);
			log.error("Error inesperado al cancelar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * @return error global si existiera
	 * @throws ReservaException con un error
	 */
	private String validacionesGlobalesPreviasReservaFija() throws ReservaException
	{

		String outcome = null;

		// Vemos si la reserva está deshabilitada
		Optional<Constantes> optionalAppDeshabilitada = this.constanteRepository
				.findByClave(Constants.TABLA_CONST_RESERVAS_FIJAS);
		if (!optionalAppDeshabilitada.isPresent())
		{
			String errorString = "Error obteniendo parametros";

			log.error(errorString + ". " + Constants.TABLA_CONST_RESERVAS_FIJAS);
			throw new ReservaException(21, errorString);
		}

		if (!optionalAppDeshabilitada.get().getValor().isEmpty())
		{
			outcome = optionalAppDeshabilitada.get().getValor();
		}

		return outcome;
	}

}
