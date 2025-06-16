package es.iesjandula.reaktor.booking_server.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.base.utils.HttpClientUtils;
import es.iesjandula.reaktor.booking_server.dto.ReservasPuntualesDto;
import es.iesjandula.reaktor.booking_server.exception.BookingError;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.Constantes;
import es.iesjandula.reaktor.booking_server.models.LogReservas;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Recurso;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramoHorario;
import es.iesjandula.reaktor.booking_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.booking_server.repository.IProfesorRepository;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class Utilities
{
	@Autowired
	private ConstantesRepository constanteRepository;

	@Autowired
	private IProfesorRepository profesoresRepository;

	/**
	 * Busca un profesor en Firebase utilizando el JWT y el email proporcionados.
	 * 
	 * @param jwtAdmin              JWT del administrador para autenticación
	 * @param email                 Email del profesor a buscar
	 * @param firebaseServerUrl     URL del servidor Firebase
	 * @param httpConnectionTimeout Timeout para la conexión HTTP
	 * @return Profesor encontrado o null si no existe
	 * @throws ReservaException si ocurre un error durante la búsqueda
	 */
	public Profesor buscarProfesorEnFirebase(String jwtAdmin, String email, String firebaseServerUrl,
			int httpConnectionTimeout) throws ReservaException
	{
		Profesor profesor = null;
		CloseableHttpClient closeableHttpClient = HttpClientUtils.crearHttpClientConTimeout(httpConnectionTimeout);
		CloseableHttpResponse closeableHttpResponse = null;

		try
		{
			HttpGet httpGet = new HttpGet(firebaseServerUrl + "/firebase/queries/user");
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
			cerrarFlujoHttpResponse(closeableHttpResponse);
		}

		return profesor;
	}

	/**
	 * Cierra de forma segura un CloseableHttpResponse.
	 * 
	 * @param closeableHttpResponse Respuesta HTTP a cerrar
	 * @throws ReservaException si ocurre un error al cerrar el flujo
	 */
	public void cerrarFlujoHttpResponse(CloseableHttpResponse closeableHttpResponse) throws ReservaException
	{
		if (closeableHttpResponse != null)
		{
			try
			{
				closeableHttpResponse.close();
			}
			catch (IOException ioException)
			{
				String errorString = "IOException mientras se cerraba el closeableHttpResponse";
				log.error(errorString, ioException);
				throw new ReservaException(Constants.IO_EXCEPTION_FIREBASE, errorString, ioException);
			}
		}
	}

	/**
	 * Realiza validaciones globales previas a operaciones de reserva.
	 * 
	 * @param usuario     Usuario que realiza la operación
	 * @param tipoReserva Tipo de reserva ("FIJAS" o "TEMPORALES")
	 * @throws ReservaException si la validación falla
	 */
	public void validacionesGlobalesPreviasReserva(DtoUsuarioExtended usuario, String tipoReserva)
			throws ReservaException
	{
		if (!usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR)
				&& !usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
		{

			String claveConstante = tipoReserva.equals("FIJAS") ? Constants.TABLA_CONST_RESERVAS_FIJAS
					: Constants.TABLA_CONST_RESERVAS_TEMPORALES;

			Optional<Constantes> optionalAppDeshabilitada = this.constanteRepository.findByClave(claveConstante);

			if (!optionalAppDeshabilitada.isPresent())
			{
				String errorString = "Error obteniendo parametros";
				log.error(errorString + ". " + claveConstante);
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
	 * Crea una instancia de Recurso con el ID proporcionado.
	 * 
	 * @param recursoId ID del recurso
	 * @return Instancia de Recurso
	 */
	public Recurso crearInstanciaRecurso(String recursoId)
	{
		Recurso recurso = new Recurso();
		recurso.setId(recursoId);
		return recurso;
	}

	/**
	 * Crea una instancia de DiaSemana con el ID proporcionado.
	 * 
	 * @param diaSemanaId ID del día de la semana
	 * @return Instancia de DiaSemana
	 */
	public DiaSemana crearInstanciaDiaSemana(Long diaSemanaId)
	{
		DiaSemana diaSemana = new DiaSemana();
		diaSemana.setId(diaSemanaId);
		return diaSemana;
	}

	/**
	 * Crea una instancia de TramoHorario con el ID proporcionado.
	 * 
	 * @param tramoHorarioId ID del tramo horario
	 * @return Instancia de TramoHorario
	 */
	public TramoHorario crearInstanciaTramoHorario(Long tramoHorarioId)
	{
		TramoHorario tramoHorario = new TramoHorario();
		tramoHorario.setId(tramoHorarioId);
		return tramoHorario;
	}

	/**
	 * Carga datos desde un archivo CSV y los convierte en una lista de objetos.
	 * 
	 * @param filePath   Ruta del archivo CSV
	 * @param skipHeader Si se debe saltar la primera línea (cabecera)
	 * @return Lista de objetos procesados
	 * @throws BookingError si ocurre un error al leer el archivo
	 */
	public List<Object> cargarDatosDesdeCSV(String filePath, boolean skipHeader) throws BookingError
	{
		List<Object> resultados = new ArrayList<>();
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new FileReader(ResourceUtils.getFile(filePath), Charset.forName("UTF-8")));

			if (skipHeader)
			{
				reader.readLine();
			}

			String linea = reader.readLine();
			while (linea != null)
			{
				String[] valores = linea.split(",");
				if (valores != null && valores.length > 0)
				{
					resultados.add(valores);
				}
				linea = reader.readLine();
			}
		}
		catch (IOException ioException)
		{
			String errorString = "IOException mientras se leía el archivo CSV: " + filePath;
			log.error(errorString, ioException);
			throw new BookingError(Constants.ERROR_INESPERADO, errorString, ioException);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					log.error("Error al cerrar el reader", e);
				}
			}
		}

		return resultados;
	}

	/**
	 * Crea un log de reserva con la información proporcionada.
	 * 
	 * @param profesor             Nombre del profesor
	 * @param accion               Acción realizada (Crear/Borrar)
	 * @param tipoReserva          Tipo de reserva (Fija/Temporal)
	 * @param recurso              Recurso reservado
	 * @param fecha                Fecha de la reserva
	 * @param usuarioRealizaAccion Usuario que realiza la acción
	 * @return Objeto LogReservas creado
	 */
	public LogReservas crearLogReserva(String profesor, String accion, String tipoReserva, String recurso, String fecha,
			String usuarioRealizaAccion)
	{
		return new LogReservas(new Date(), profesor, accion, tipoReserva, recurso, fecha, usuarioRealizaAccion);
	}

	/**
	 * Maneja una excepción y devuelve una respuesta HTTP apropiada.
	 * 
	 * @param exception    Excepción a manejar
	 * @param mensajeError Mensaje de error personalizado
	 * @param codigoError  Código de error HTTP
	 * @return ResponseEntity con el mensaje de error
	 */
	public ResponseEntity<?> manejarError(Exception exception, String mensajeError, int codigoError)
	{
		ReservaException reservaException = new ReservaException(Constants.ERROR_INESPERADO, mensajeError, exception);
		log.error(mensajeError + ": ", exception);
		return ResponseEntity.status(codigoError).body(reservaException.getBodyMesagge());
	}

	/**
	 * Procesa los resultados de las reservas y los convierte en una lista de DTOs.
	 * 
	 * @param resultados          Lista de resultados de la consulta
	 * @param recursoSeleccionado Recurso seleccionado
	 * @return Lista de ReservasPuntualesDto
	 */
	public List<ReservasPuntualesDto> procesarResultadosReservas(List<Object[]> resultados, Recurso recursoSeleccionado)
	{
		List<ReservasPuntualesDto> listaReservas = new ArrayList<>();
		Long diaSemana = 0l;
		Long tramoHorario = 0l;
		ReservasPuntualesDto reserva = null;

		for (Object[] row : resultados)
		{
			if (diaSemana == (Long) row[0] && tramoHorario == (Long) row[1])
			{
				ReservasPuntualesDto reservaAntigua = reserva;
				reserva = actualizarReservaExistente(reserva, row);
				listaReservas.remove(reservaAntigua);
				listaReservas.add(reserva);
			}
			else
			{
				diaSemana = (Long) row[0];
				tramoHorario = (Long) row[1];
				reserva = crearNuevaReserva(row, recursoSeleccionado);
				listaReservas.add(reserva);
			}
		}

		return listaReservas;
	}

	/**
	 * Actualiza una reserva existente con nuevos datos.
	 * 
	 * @param reserva Reserva actual a actualizar
	 * @param row     Datos de la nueva reserva
	 * @return Reserva actualizada
	 */
	public ReservasPuntualesDto actualizarReservaExistente(ReservasPuntualesDto reserva, Object[] row)
	{
		List<String> emails = reserva.getEmail();
		List<String> nombresYApellidos = reserva.getNombreYapellidos();
		List<Integer> nAlumnosLista = reserva.getNAlumnos();
		Integer plazasRestantes = reserva.getPlazasRestantes();
		List<Long> esFijaLista = reserva.getEsfija();
		List<Long> esSemanalLista = reserva.getEsSemanal();
		List<String> motivoCursoLista = reserva.getMotivoCurso();

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

		return reserva;
	}

	/**
	 * Crea una nueva reserva puntual con los datos proporcionados.
	 * 
	 * @param row                 Datos de la reserva
	 * @param recursoSeleccionado Recurso seleccionado
	 * @return Nueva reserva creada
	 */
	public ReservasPuntualesDto crearNuevaReserva(Object[] row, Recurso recursoSeleccionado)
	{
		Integer plazasRestantes = recursoSeleccionado.getCantidad();
		Long diaSemana = (Long) row[0];
		Long tramoHorario = (Long) row[1];
		Integer nAlumnos = (row[2] != null) ? (Integer) row[2] : 0;
		String email = (String) row[3];
		String nombreYapellidos = (String) row[4];
		String recursos = (String) row[5];
		Long esFija = (Long) row[6];
		String motivoCurso = (String) row[7];
		plazasRestantes = plazasRestantes - nAlumnos;
		BigDecimal esSemanalBD = (BigDecimal) row[8];
		Long esSemanal = esSemanalBD != null ? esSemanalBD.longValue() : null;

		List<String> emails = new ArrayList<>();
		emails.add(email);
		List<String> nombresYApellidos = new ArrayList<>();
		nombresYApellidos.add(nombreYapellidos);
		List<Integer> nAlumnosLista = new ArrayList<>();
		nAlumnosLista.add(nAlumnos);
		List<Long> esFijaLista = new ArrayList<>();
		esFijaLista.add(esFija);
		List<String> motivoCursoLista = new ArrayList<>();
		motivoCursoLista.add(motivoCurso);
		List<Long> esSemanalLista = new ArrayList<>();
		esSemanalLista.add(esSemanal);

		return new ReservasPuntualesDto(diaSemana, tramoHorario, nAlumnosLista, emails, nombresYApellidos, recursos,
				plazasRestantes, esFijaLista, motivoCursoLista, esSemanalLista);
	}
}
