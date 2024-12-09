package ies.jandula.reservaCarritos.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ies.jandula.reservaCarritos.dto.ReservaDto;
import ies.jandula.reservaCarritos.exception.ReservaException;
import ies.jandula.reservaCarritos.models.DiasSemana;
import ies.jandula.reservaCarritos.models.Recursos;
import ies.jandula.reservaCarritos.models.Reserva;
import ies.jandula.reservaCarritos.models.ReservaId;
import ies.jandula.reservaCarritos.models.TramosHorarios;
import ies.jandula.reservaCarritos.repository.DiasSemanaRepository;
import ies.jandula.reservaCarritos.repository.RecursosRepository;
import ies.jandula.reservaCarritos.repository.ReservaRepository;
import ies.jandula.reservaCarritos.repository.TramoHorarioRepository;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/incidenciasTic", produces = { "application/json" }) 
@RestController
@Log4j2
public class ReservaCarritosRestWeb 
{
	
	@Autowired
	private RecursosRepository recursosRepository;
	
	@Autowired
	private ReservaRepository reservaRepository;
	
	@Autowired
	private DiasSemanaRepository diasSemanaRepository;
	
	@Autowired
	private TramoHorarioRepository tramoHorarioRepository;
	
	/*
	 * Endpoint de tipo get para mostar una lista con los recursos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/obtener_recurso")
	public ResponseEntity<?> obtenerRecurso()
	{
		
		try
		{
//			Creacion de una lista para almacenar los recursos
			List<Recursos> listaRecursos;
			
//			Comprueba si la base de datos tiene registros de los recurso
			if(this.recursosRepository.findAll().isEmpty()) 
			{
				
				String mensajeError="No se ha encontrado ningun recurso";
				log.error(mensajeError);
				throw new ReservaException(1, mensajeError);
				
			}
			
//			Encontramos todos los recursos y los introducimos en una lista para mostrarlos más adelante
			listaRecursos = this.recursosRepository.findAll();
			
			return ResponseEntity.ok(listaRecursos);
		}
		catch (ReservaException reservaException) 
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}	
		catch (Exception exception) 
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100,"Error al acceder a la bade de datos", exception);
			
		    log.error("Error al acceder a la bade de datos: ", exception);
		    return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
	
	/*
	 * Endpoint de tipo get para mostar una lista con los tramos horarios
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/obtener_tramos_horarios")
	public ResponseEntity<?> obtenerTramosHorarios()
	{
		
		try
		{
//			Creacion de una lista para almacenar los tramos horarios
			List<TramosHorarios> listaTramos;
			
//			Comprueba si la base de datos tiene registros de los tramos horarios
			if(this.tramoHorarioRepository.findAll().isEmpty()) 
			{
				
				String mensajeError="No se ha encontrado ningun tramo horario";
				log.error(mensajeError);
				throw new ReservaException(2, mensajeError);
				
			}
//			Encontramos todos los tramos y los introducimos en una lista para mostrarlos más adelante
			listaTramos = this.tramoHorarioRepository.findAll();
			
			return ResponseEntity.ok(listaTramos);
		}
		catch (ReservaException reservaException) 
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception) 
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100,"Error al acceder a la bade de datos", exception);
			
		    log.error("Error al acceder a la bade de datos: ", exception);
		    return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
	
	/*
	 * Endpoint de tipo get para mostar una lista con los días de la semana
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/obtener_dias_semanas")
	public ResponseEntity<?> obtenerDiasSemana()
	{
		try
		{
//			Creacion de una lista para almacenar los dias de la semana
			List<DiasSemana> listaDias;
			
//			Comprueba si la base de datos tiene registros de los días de la semana
			if(this.diasSemanaRepository.findAll().isEmpty()) 
			{
				
				String mensajeError="Error al obtener los días de la semana";
				log.error(mensajeError);
				throw new ReservaException(3, mensajeError);
			}
			
//			Encontramos todos los dias y los introducimos en una lista para mostrarlos más adelante
			listaDias = this.diasSemanaRepository.findAll();
			
			return ResponseEntity.ok(listaDias);
		}
		catch (ReservaException reservaException) 
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception) 
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100,"Error al acceder a la bade de datos", exception);
			
		    log.error("Error al acceder a la bade de datos: ", exception);
		    return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
    }
	
	/*
	 * Endpoint de tipo get para mostar una lista con los días de la semana
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/obtener_reservas")
	public ResponseEntity<?> obtenerReservas()
	{
		try
		{
//			Creacion de una lista para almacenar las reservas
			List<Reserva> listaRecursos;
			
//			Comprueba si la base de datos tiene registros de las reservas
			if(this.reservaRepository.findAll().isEmpty()) 
			{
				
				String mensajeError="Error al obtener los recursos";
				log.error(mensajeError);
				throw new ReservaException(3, mensajeError);
			}
			
//			Encontramos todas las reservas y los introducimos en una lista para mostrarlos más adelante
			listaRecursos = this.reservaRepository.findAll();
			
			return ResponseEntity.ok(listaRecursos);
		}
		catch (ReservaException reservaException) 
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception) 
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100,"Error al acceder a la bade de datos", exception);
			
			log.error("Error al acceder a la bade de datos: ", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
	
	/**
	 * Recibe un recurso y devuelve una lista de recursos organizados por días y tramos horarios, para mostrarlos 
	 * @param recursos
	 * @return
	 * @throws ReservaException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/obtenerReservaPorRecurso")
	public ResponseEntity<?> obtenerReservaPorRecurso(@RequestHeader(value = "recursos", required = true) Recursos recursos)
	{
		try 
		{
//			Verifica si existe una reserva asociada a ese recurso en base de datos
			if(this.reservaRepository.encontrarReservaPorRecurso(recursos).isEmpty())
			{
				
				String mensajeError="No se ha encontrado ninguna reseva con ese recurso";
				log.error(mensajeError);
				throw new ReservaException(4, mensajeError);
			}
			
//			Si existe, la añadimos a una lista para mostrarla más adelante
			List<ReservaDto> listaRecursos = this.reservaRepository.encontrarReservaPorRecurso(recursos);
		
			return ResponseEntity.ok(listaRecursos);
		}
		catch (ReservaException reservaException) 
		{
//			Captura la excepcion personalizada, devolvera un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}
		catch (Exception exception) 
		{
//			Captura los errores relacionados con la base de datos, devolverá un 500
			ReservaException reservaException = new ReservaException(100,"Error al acceder a la bade de datos", exception);
			
		    log.error("Error al acceder a la bade de datos: ", exception);
		    return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
		
	}
	
	/**
	 * Endpoint de tipo post para añadir un recurso con un recurso
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/añadir_recurso")
	public ResponseEntity<?> añadirRecurso(@RequestHeader(value = "recursos", required = true) String recursos)
	{
		
		try 
		{
//			Comprueba si existe un recurso con esos datos
			if(this.recursosRepository.existsById(recursos)) {
				
				String mensajeError="El recurso que quieres añadir ya existe";
				log.error(mensajeError);
				throw new ReservaException(5, mensajeError);
			}
			
			Recursos nuevoRecurso = new Recursos();
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
			ReservaException reservaException = new ReservaException(100,"Error inesperado al añadir un recurso", exception);
			
		    log.error("Error inesperado al añadir un recurso: ", exception);
		    return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
	
	
	/**
	 * Endpoint de tipo post para realizar una reserva con un correo de un profesor, un
	 * recurso, un día de la semana, un tramo horario, un profesor y un número
	 * de alumnos
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/realizar_reserva")
	public ResponseEntity<?> realizarReserva(@RequestHeader(value = "email", required = true) String email,
											 @RequestHeader(value = "profesor", required = true ) String nombreProfesor,
											 @RequestHeader(value = "recurso", required = true) String aulaYCarritos,
											 @RequestHeader(value = "diaDeLaSemana", required = true) String diaDeLaSemana,
											 @RequestHeader(value = "tramosHorarios", required = true) String tramosHorarios,
											 @RequestHeader(value = "nAlumnos", required = true) int nAlumnos)
	{
		try {
			
			// Verifica si ya existe una reserva con los mismos datos
			Optional<Reserva> optinalReserva = this.reservaRepository.encontrarReserva(email, aulaYCarritos, diaDeLaSemana, tramosHorarios);
			
			if (optinalReserva.isPresent()) 
			{
				String mensajeError="Ya existe una la reserva con esos datos";
				log.error(mensajeError);
				throw new ReservaException(6, mensajeError);
	        }
			
			Recursos recurso = new Recursos();
			recurso.setAulaYCarritos(aulaYCarritos);
			
			
			DiasSemana diasSemana = new DiasSemana();
			diasSemana.setDiasDeLaSemana(diaDeLaSemana);
			
			
			TramosHorarios tramos = new TramosHorarios();
			tramos.setTramosHorarios(tramosHorarios);

			
			ReservaId reservaId = new ReservaId();
			reservaId.setEmail(email);
			reservaId.setAulaYCarritos(recurso);
			reservaId.setDiasDeLaSemana(diasSemana);
			reservaId.setTramosHorarios(tramos);
			
			Reserva reserva = new Reserva();
			reserva.setReservaId(reservaId);
			reserva.setNombreProfesor(nombreProfesor);
			reserva.setNAlumnos(nAlumnos);
			
			log.info("Se ha reservado correctamente");
			
			reserva.setReservaId(reservaId);
			
//			Si no existe una reserva previa, se guarda la nueva reserva en la base de datos
			this.reservaRepository.saveAndFlush(reserva);		
			
			return ResponseEntity.ok().body("Reserva realizada correctamente");
			
		}
		catch (ReservaException reservaException) 
		{
			
//			Captura la excepcion personalizada y retorna un 409 ya que existe un conflicto,
//			que existe una reserva con los mismos datos
			return ResponseEntity.status(409).body(reservaException.getBodyMesagge());
		}		
		catch (Exception exception) 
		{
//			Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(100,"Error inesperado al realizar la reserva", exception);
			
		    log.error("Error inesperado al realizar la reserva: ", exception);
		    return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
		
	}
	
	/**
	 * Endpoint de tipo post para cancelar una reserva con un correo de un profesor, un
	 * recurso, un día de la semana, un tramo horario
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/cancelar_recurso")
	public ResponseEntity<?> cancelarRecurso(@RequestHeader(value = "email", required = true) String email,
											 @RequestHeader(value = "recurso", required = true) String aulaYCarritos,
											 @RequestHeader(value = "diaDeLaSemana", required = true) String diaDeLaSemana,
											 @RequestHeader(value = "tramoHorario", required = true) String tramoHorario)
	{
		try 
		{
			
			// Antes de borrar la reserva verifica si existe una reserva con los mismos datos
			Optional<Reserva> optinalReserva = this.reservaRepository.encontrarReserva(email, aulaYCarritos, diaDeLaSemana, tramoHorario);
			
			if (!optinalReserva.isPresent()) 
			{
				String mensajeError="La reserva que quiere borrar no existe";
				log.error(mensajeError);
				throw new ReservaException(7, mensajeError);
	        }
			
			Recursos recurso = new Recursos();
			recurso.setAulaYCarritos(aulaYCarritos);
			
			
			DiasSemana diasSemana = new DiasSemana();
			diasSemana.setDiasDeLaSemana(diaDeLaSemana);
			
			
			TramosHorarios tramosHorarios = new TramosHorarios();
			tramosHorarios.setTramosHorarios(tramoHorario);

			
			ReservaId reservaId = new ReservaId();
			reservaId.setEmail(email);
			reservaId.setAulaYCarritos(recurso);
			reservaId.setDiasDeLaSemana(diasSemana);
			reservaId.setTramosHorarios(tramosHorarios);
			
			log.info("La reserva se ha borrado correctamente");
			
			// Si la reserva existe en la base de datos, se borrará
			this.reservaRepository.deleteById(reservaId);		
			
			return ResponseEntity.ok().build();
			
		}
		catch (ReservaException reservaException) 
		{
//			Si la reserva no existe, devolverá un 404
			return ResponseEntity.status(404).body(reservaException.getBodyMesagge());
		}			
		catch (Exception exception) 
		{
//			Para cualquier error inesperado, devolverá un 500
			ReservaException reservaException = new ReservaException(100,"Error inesperado al cancelar la reserva", exception);
			
		    log.error("Error inesperado al cancelar la reserva: ", exception);
		    return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}

	}
	
}
