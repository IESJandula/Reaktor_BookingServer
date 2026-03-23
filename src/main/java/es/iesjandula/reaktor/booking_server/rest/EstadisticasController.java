package es.iesjandula.reaktor.booking_server.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaTramoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.repository.IReservaFijaRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaTemporalRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;

/**
 * Controlador REST para proporcionar estadísticas del sistema de reservas.
 * Accesible solo para roles de administrador o dirección.
 * 
 * ESTADÍSTICAS PONDERADAS: - Reservas FIJAS: Se ponderan por semanas restantes
 * hasta fin de curso - Reservas TEMPORALES: Cuentan como 1 semana cada una
 */
@RequestMapping("/bookings/estadisticas")
@RestController
public class EstadisticasController
{
	private static final Logger log = LoggerFactory.getLogger(EstadisticasController.class);

	@Autowired
	private IReservaFijaRepository reservaFijaRepository;

	@Autowired
	private IReservaTemporalRepository reservaTemporalRepository;

	/**
	 * SEMANA FIN DE CURSO (aproximada - 40 semanas desde septiembre)
	 */
	private static final int SEMANA_FIN_CURSO = 40;

	/**
	 * Obtiene el recurso más reservado combinando reservas fijas (ponderadas) y
	 * temporales (directas).
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/recurso-mas-reservado")
	public ResponseEntity<?> obtenerRecursoMasReservado()
	{
		try
		{
			Map<String, Long> contador = new HashMap<>();

			// 1. RESERVAS FIJAS (ponderadas por semanas restantes)
			List<Object[]> fijas = reservaFijaRepository.contarPorRecursoConFecha();
			for (Object[] row : fijas)
			{
				String recurso = (String) row[0];
				LocalDateTime fechaCreacion = (LocalDateTime) row[1];
				int semanaCreacion = calcularSemanaDesdeFecha(fechaCreacion);

				// Ponderar: semanas restantes desde creación hasta fin de curso
				long semanasRestantes = SEMANA_FIN_CURSO - semanaCreacion + 1;
				contador.merge(recurso, semanasRestantes, Long::sum);
			}

			// 2. RESERVAS TEMPORALES (1 reserva = 1 semana)
			List<Object[]> temporales = reservaTemporalRepository.contarPorRecurso();
			for (Object[] row : temporales)
			{
				String recurso = (String) row[0];
				contador.merge(recurso, 1L, Long::sum);
			}

			// 3. Convertir a DTO y ordenar
			List<EstadisticaRecursoMasReservadoDto> resultado = contador.entrySet().stream()
					.map(e -> new EstadisticaRecursoMasReservadoDto(e.getKey(), e.getValue()))
					.sorted(Comparator.comparingLong(EstadisticaRecursoMasReservadoDto::getTotalReservas).reversed())
					.collect(Collectors.toList());

			return ResponseEntity.ok(resultado);
		} catch (Exception exception)
		{
			log.error("Error al obtener recurso más reservado", exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_ESTADISTICAS,
					"Error al obtener el recurso más reservado", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Obtiene el tramo horario más reservado combinando reservas fijas y
	 * temporales. (ejemplo: "8:00-9:00") 
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/tramo-horario-mas-reservado")
	public ResponseEntity<?> obtenerTramoHorarioMasReservado()
	{
		try
		{
			Map<String, Long> contador = new HashMap<>();

			// 1. RESERVAS FIJAS (ponderadas por semanas restantes)			
			List<Object[]> fijas = reservaFijaRepository.contarPorTramoConNombre();
			for (Object[] row : fijas)
			{
				String tramoHorario = (String) row[0]; // ← AHORA es el nombre "8:00-9:00"
				LocalDateTime fechaCreacion = (LocalDateTime) row[1];
				int semanaCreacion = calcularSemanaDesdeFecha(fechaCreacion);
				long semanasRestantes = SEMANA_FIN_CURSO - semanaCreacion + 1;
				contador.merge(tramoHorario, semanasRestantes, Long::sum);
			}

			// 2. RESERVAS TEMPORALES (1 reserva = 1 semana)			
			List<Object[]> temporales = reservaTemporalRepository.contarPorTramoConNombre();
			for (Object[] row : temporales)
			{
				String tramoHorario = (String) row[0];
				Long count = (Long) row[1];
				contador.merge(tramoHorario, count, Long::sum);
			}

			// 3. Convertir a DTO y ordenar
			List<EstadisticaDiaTramoMasReservadoDto> resultado = contador.entrySet().stream()
					.map(e -> new EstadisticaDiaTramoMasReservadoDto("", e.getKey(), e.getValue()))
					.sorted(Comparator.comparingLong(EstadisticaDiaTramoMasReservadoDto::getTotalReservas).reversed())
					.collect(Collectors.toList());

			return ResponseEntity.ok(resultado);
		} catch (Exception exception)
		{
			log.error("Error al obtener tramo horario más reservado", exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_ESTADISTICAS,
					"Error al obtener el tramo horario más reservado", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Obtiene el día de la semana más reservado combinando reservas fijas y
	 * temporales.
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/dia-semana-mas-reservado")
	public ResponseEntity<?> obtenerDiaSemanaMasReservado()
	{
		try
		{
			Map<Long, Long> contador = new HashMap<>();

			// 1. RESERVAS FIJAS (ponderadas)
			List<Object[]> fijas = reservaFijaRepository.contarPorDiaConFecha();
			for (Object[] row : fijas)
			{
				Long diaId = (Long) row[0];
				LocalDateTime fechaCreacion = (LocalDateTime) row[1];
				int semanaCreacion = calcularSemanaDesdeFecha(fechaCreacion);
				long semanasRestantes = SEMANA_FIN_CURSO - semanaCreacion + 1;
				contador.merge(diaId, semanasRestantes, Long::sum);
			}

			// 2. RESERVAS TEMPORALES (directas)
			List<Object[]> temporales = reservaTemporalRepository.contarPorDia();
			for (Object[] row : temporales)
			{
				Long diaId = (Long) row[0];
				contador.merge(diaId, 1L, Long::sum);
			}

			// 3. Convertir a DTO
			List<EstadisticaDiaTramoMasReservadoDto> resultado = new ArrayList<>();
			contador.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEach(e ->
			{
				// Mapeo de ID de día a nombre (1=Lunes, 2=Martes, etc.)
				String nombreDia = obtenerNombreDia(e.getKey());
				resultado.add(new EstadisticaDiaTramoMasReservadoDto(nombreDia, "", e.getValue()));
			});

			return ResponseEntity.ok(resultado);
		} catch (Exception exception)
		{
			log.error("Error al obtener día de la semana más reservado", exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_ESTADISTICAS,
					"Error al obtener el día de la semana más reservado", exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	/**
	 * Calcula la semana del curso escolar desde una fecha. Curso: 1 Septiembre - 30
	 * Junio
	 */
	private int calcularSemanaDesdeFecha(LocalDateTime fecha)
	{
		LocalDate date = fecha.toLocalDate();
		LocalDate inicioCurso = LocalDate.of(date.getYear(), 9, 1);

		// Si es antes de septiembre, el curso empezó el año anterior
		if (date.getMonthValue() < 9)
		{
			inicioCurso = LocalDate.of(date.getYear() - 1, 9, 1);
		}

		long semanas = ChronoUnit.WEEKS.between(inicioCurso, date);
		return (int) semanas + 1;
	}

	/**
	 * Obtiene el nombre del día de la semana desde su ID.
	 */
	private String obtenerNombreDia(Long diaId)
	{
		if (diaId == null)
			return "Desconocido";

		switch (diaId.intValue())
		{
		case 1:
			return "Lunes";
		case 2:
			return "Martes";
		case 3:
			return "Miércoles";
		case 4:
			return "Jueves";
		case 5:
			return "Viernes";
		case 6:
			return "Sábado";
		case 7:
			return "Domingo";
		default:
			return "Día " + diaId;
		}
	}
}