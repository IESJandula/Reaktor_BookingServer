package es.iesjandula.reserva_carritos.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reserva_carritos.dto.ReservaDto;
import es.iesjandula.reserva_carritos.exception.ReservaException;
import es.iesjandula.reserva_carritos.models.reservas_fijas.DiasSemana;
import es.iesjandula.reserva_carritos.models.reservas_fijas.Profesores;
import es.iesjandula.reserva_carritos.models.reservas_fijas.RecursosPrevios;
import es.iesjandula.reserva_carritos.models.reservas_fijas.ReservaFijas;
import es.iesjandula.reserva_carritos.models.reservas_fijas.ReservasFijasId;
import es.iesjandula.reserva_carritos.models.reservas_fijas.TramosHorarios;
import es.iesjandula.reserva_carritos.repository.DiasSemanaRepository;
import es.iesjandula.reserva_carritos.repository.ProfesoresRepository;
import es.iesjandula.reserva_carritos.repository.RecursosRepository;
import es.iesjandula.reserva_carritos.repository.ReservasRepository;
import es.iesjandula.reserva_carritos.repository.TramosHorariosRepository;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/previous_resources", produces =
{ "application/json" })
@RestController
@Log4j2
public class ReservaCarritosRest
{

	@Autowired
	private RecursosRepository recursosRepository;

	@Autowired
	private ProfesoresRepository profesoresRepository;

	@Autowired
	private ReservasRepository reservasRepository;

	@Autowired
	private DiasSemanaRepository diasSemanaRepository;

	@Autowired
	private TramosHorariosRepository tramosHorariosRepository;

	/*
	 * Endpoint de tipo get para mostar una lista con los recursos
	 */
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
			ReservaException reservaException = new ReservaException(
					100, "Error al acceder a la bade de datos", exception
			);
			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/*
	 * Endpoint de tipo get para mostar una lista con los tramos horarios
	 */
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
			ReservaException reservaException = new ReservaException(
					100, "Error al acceder a la bade de datos", exception
			);
			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/*
	 * Endpoint de tipo get para mostar una lista con los días de la semana
	 */
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
			ReservaException reservaException = new ReservaException(
					100, "Error al acceder a la bade de datos", exception
			);
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
	@RequestMapping(method = RequestMethod.GET, value = "/bookings")
	public ResponseEntity<?> obtenerReservasDto(@RequestHeader(value = "aulaYCarritos")String recursoA)
	{
		try
		{
//			Creacion de una lista para almacenar los recursos
			List<ReservaDto> listaReservas = new ArrayList<ReservaDto>();
			List<Object[]> resultados = reservasRepository.encontrarReservaPorRecurso(recursoA);

//			Comprueba si la base de datos tiene registros de los recurso
			if (this.recursosRepository.findAll().isEmpty())
			{
				String mensajeError = "No se ha encontrado ningun recurso";
				log.error(mensajeError);
				throw new ReservaException(1, mensajeError);
			}
			
			for (Object[] row : resultados) {
				Long  diaSemana = (Long)row[0];
				Long tramoHorario = (Long) row[1];
	            Integer nAlumnos = (row[2] != null) ? (Integer) row[2] : 0;
	            String email = (String) row[3];
	            String nombreYapellidos = (String) row[4];
	            String recurso = (String) row[5];

	            // Mapeo a ReservaDto
	            listaReservas.add(new ReservaDto(diaSemana, tramoHorario, nAlumnos, email, nombreYapellidos, recurso));
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
			ReservaException reservaException = new ReservaException(
					100, "Error al acceder a la bade de datos", exception
			);
			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Endpoint de tipo post para añadir un recurso con un recurso
	 */
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

	/**
	 * Endpoint de tipo post para realizar una reserva con un correo de un profesor,
	 * un recurso, un día de la semana, un tramo horario, un profesor y un número de
	 * alumnos
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/bookings")
	public ResponseEntity<?> realizarReserva(@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String aulaYCarritos,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramosHorarios", required = true) Long tramosHorarios,
			@RequestHeader(value = "nAlumnos", required = true) int nAlumnos)
	{
		try
		{

			// Verifica si ya existe una reserva con los mismos datos
			Optional<ReservaFijas> optinalReserva = this.reservasRepository
					.encontrarReserva( aulaYCarritos, diaDeLaSemana, tramosHorarios);

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

			Optional<Profesores> profesor = this.profesoresRepository.findById(email);

			ReservasFijasId reservaId = new ReservasFijasId();

			if (!profesor.isPresent())
			{
				String mensajeError = "No existe ese email";
				log.error(mensajeError);
				throw new ReservaException(20, mensajeError);
			}
			reservaId.setProfesor(profesor.get());

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
			ReservaException reservaException = new ReservaException(
					100, "Error inesperado al realizar la reserva", exception
			);

			log.error("Error inesperado al realizar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}

	}

	/**
	 * Endpoint de tipo post para cancelar una reserva con un correo de un profesor,
	 * un recurso, un día de la semana, un tramo horario
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/bookings")
	public ResponseEntity<?> cancelarRecurso(@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "recurso", required = true) String aulaYCarritos,
			@RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
			@RequestHeader(value = "tramoHorario", required = true) Long tramoHorario)
	{
		try
		{

			// Antes de borrar la reserva verifica si existe una reserva con los mismos
			// datos
			Optional<ReservaFijas> optinalReserva = this.reservasRepository
					.encontrarReserva( aulaYCarritos, diaDeLaSemana, tramoHorario);

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

			Optional<Profesores> profesor = this.profesoresRepository.findById(email);

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
			ReservaException reservaException = new ReservaException(
					100, "Error inesperado al cancelar la reserva", exception
			);
			log.error("Error inesperado al cancelar la reserva: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}

	}

}
