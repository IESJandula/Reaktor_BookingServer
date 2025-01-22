package es.iesjandula.reaktor.booking_server.rest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import es.iesjandula.reaktor.base.security.models.DtoUsuario;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.booking_server.dto.ReservasFijasDto;
import es.iesjandula.reaktor.booking_server.dto.ReservasPuntualesDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiasSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesores;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.RecursosPrevios;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservaFijas;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservasFijasId;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramosHorarios;
import es.iesjandula.reaktor.booking_server.repository.IDiasSemanaRepository;
import es.iesjandula.reaktor.booking_server.repository.IProfesoresRepository;
import es.iesjandula.reaktor.booking_server.repository.IRecursosRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservasRepository;
import es.iesjandula.reaktor.booking_server.repository.ITramosHorariosRepository;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/puntuals_bookings/previous_resources", produces =
{ "application/json" })
@RestController
@Log4j2
public class ReservasPuntualesRest
{
	@Autowired
	private IRecursosRepository recursosRepository;

	@Autowired
	private IProfesoresRepository profesoresRepository;

	@Autowired
	private IReservasRepository reservasRepository;

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
	public ResponseEntity<?> obtenerReservasDto(@RequestHeader(value = "aulaYCarritos")String recursoA,
												@RequestHeader(value = "numeroSemana")Integer nSemana
			)
	{
		try
		{
//			Creacion de una lista para almacenar los recursos
			List<ReservasPuntualesDto> listaReservas = new ArrayList<ReservasPuntualesDto>();
			List<Object[]> resultados = reservasRepository.encontrarReservaPorRecursoYnSemana(recursoA,nSemana);

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
	            Integer numeroSemana = (row[6] != null) ? (Integer) row[6] : 0;
	            // Mapeo a ReservaDto
	            listaReservas.add(new ReservasPuntualesDto(diaSemana, tramoHorario, nAlumnos, email, nombreYapellidos, recurso, numeroSemana));
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
	 * Endpoint de tipo post para realizar una reserva con un correo de un profesor,
	 * un recurso, un día de la semana, un tramo horario, un profesor y un número de
	 * alumnos
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/bookings")
	public ResponseEntity<?> realizarReservaFija(@AuthenticationPrincipal DtoUsuario usuario,
											 @RequestHeader(value = "email", required = true) String email,
											 @RequestHeader(value = "recurso", required = true) String aulaYCarritos,
											 @RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
											 @RequestHeader(value = "tramosHorarios", required = true) Long tramosHorarios,
											 @RequestHeader(value = "nAlumnos", required = true) int nAlumnos)
	{
		try
		{
			// Si el role del usuario es Administrador, creará la reserva con el email recibido en la cabecera
			// Si el role del usuario no es Administrador, se verificará primero que el email coincide con el que viene en DtoUsuario. Enviando excepción si no es correcto
			
			
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
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/bookings")
	public ResponseEntity<?> cancelarRecurso(@AuthenticationPrincipal DtoUsuario usuario,
											 @RequestHeader(value = "email", required = true) String email,
											 @RequestHeader(value = "recurso", required = true) String aulaYCarritos,
											 @RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
											 @RequestHeader(value = "tramoHorario", required = true) Long tramoHorario,
											 @RequestHeader(value = "numeroSemna", required = true) Integer numeroSemana
												)
	{
		try
		{
			// Si el role del usuario es Administrador, borrará la reserva con el email recibido en la cabecera
			// Si el role del usuario no es Administrador, se verificará primero que el email coincide con el que viene en DtoUsuario. Enviando excepción si no es correcto
			
			
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

//			if (usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR))
			
			Optional<Profesores> profesor = this.profesoresRepository.findById(usuario.getEmail());

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
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/bookings")
	public ResponseEntity<?> realizarReservaPuntual(@AuthenticationPrincipal DtoUsuario usuario,
											 @RequestHeader(value = "email", required = true) String email,
											 @RequestHeader(value = "recurso", required = true) String aulaYCarritos,
											 @RequestHeader(value = "diaDeLaSemana", required = true) Long diaDeLaSemana,
											 @RequestHeader(value = "tramosHorarios", required = true) Long tramosHorarios,
											 @RequestHeader(value = "nAlumnos", required = true) int nAlumnos)
	{
		try
		{
			// Si el role del usuario es Administrador, creará la reserva con el email recibido en la cabecera
			// Si el role del usuario no es Administrador, se verificará primero que el email coincide con el que viene en DtoUsuario. Enviando excepción si no es correcto
			
			
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
}
