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
	 * Obtiene el recurso más reservado combinando reservas fijas (ponderadas) y
	 * temporales (directas).
	 * 
	 * Lógica: 1. Fijas: (Semana Fin Curso - Semana Creación + 1) por cada reserva.
	 * 2. Temporales: COUNT(*) directo.
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/recurso-mas-reservado")
	public ResponseEntity<?> obtenerRecursoMasReservado()
	{
		try
		{
			Map<String, Long> contador = new HashMap<>();
			int semanaFinCurso = obtenerSemanaFinCurso();

			// 1. RESERVAS FIJAS (ponderadas por semanas restantes)
			List<Object[]> fijas = reservaFijaRepository.contarPorRecursoConFecha();

			for (Object[] row : fijas)
			{
				String recurso = (String) row[0];
				LocalDateTime fechaCreacion = (LocalDateTime) row[1];

				if (fechaCreacion == null)
				{
					contador.merge(recurso, 1L, Long::sum);
					continue;
				}

				int semanaCreacion = calcularSemanaDesdeFecha(fechaCreacion);
				long semanasRestantes = semanaFinCurso - semanaCreacion + 1;

				if (semanasRestantes > 0)
				{
					contador.merge(recurso, semanasRestantes, Long::sum);
				} else
				{
					contador.merge(recurso, 1L, Long::sum);
				}
			}

			// 2. RESERVAS TEMPORALES (1 reserva = 1 semana)
			List<Object[]> temporales = reservaTemporalRepository.contarPorRecurso();

			for (Object[] row : temporales)
			{
				String recurso = (String) row[0];
				Long count = (Long) row[1];
				//log.info("  Recurso: {}, Count: {}", recurso, count);
				contador.merge(recurso, count, Long::sum);
			}

			//log.info("=== FIN DEBUG ESTADÍSTICAS ===");
			//log.info("Resultado final: {}", contador);

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
			int semanaFinCurso = obtenerSemanaFinCurso();

			// 1. RESERVAS FIJAS (ponderadas)			
			List<Object[]> fijas = reservaFijaRepository.contarPorTramoConNombre();
			for (Object[] row : fijas)
			{
				String tramoHorario = (String) row[0];
				LocalDateTime fechaCreacion = (LocalDateTime) row[1];

				if (fechaCreacion == null)
				{
					contador.merge(tramoHorario, 1L, Long::sum);
					continue;
				}

				int semanaCreacion = calcularSemanaDesdeFecha(fechaCreacion);
				long semanasRestantes = semanaFinCurso - semanaCreacion + 1;

				if (semanasRestantes > 0)
				{
					contador.merge(tramoHorario, semanasRestantes, Long::sum);
				} else
				{
					contador.merge(tramoHorario, 1L, Long::sum);
				}
			}

			// 2. RESERVAS TEMPORALES (directas)			
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
			Map<String, Long> contador = new HashMap<>();
			int semanaFinCurso = obtenerSemanaFinCurso();

			// 1. RESERVAS FIJAS (ponderadas)			
			List<Object[]> fijas = reservaFijaRepository.contarPorDiaConNombre();
			for (Object[] row : fijas)
			{
				String diaSemana = (String) row[0];
				LocalDateTime fechaCreacion = (LocalDateTime) row[1];

				if (fechaCreacion == null)
				{
					contador.merge(diaSemana, 1L, Long::sum);
					continue;
				}

				int semanaCreacion = calcularSemanaDesdeFecha(fechaCreacion);
				long semanasRestantes = semanaFinCurso - semanaCreacion + 1;

				if (semanasRestantes > 0)
				{
					contador.merge(diaSemana, semanasRestantes, Long::sum);
				} else
				{
					contador.merge(diaSemana, 1L, Long::sum);
				}
			}

			// 2. RESERVAS TEMPORALES (directas)			
			List<Object[]> temporales = reservaTemporalRepository.contarPorDiaConNombre();
			for (Object[] row : temporales)
			{
				String diaSemana = (String) row[0];
				Long count = (Long) row[1];
				contador.merge(diaSemana, count, Long::sum);
			}

			// 3. Convertir a DTO y ordenar
			List<EstadisticaDiaTramoMasReservadoDto> resultado = contador.entrySet().stream()
					.map(e -> new EstadisticaDiaTramoMasReservadoDto(e.getKey(), "", e.getValue()))
					.sorted(Comparator.comparingLong(EstadisticaDiaTramoMasReservadoDto::getTotalReservas).reversed())
					.collect(Collectors.toList());

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
	 * Calcula dinámicamente la semana de fin de curso basada en la fecha actual.	  
	 * 
	 */
	private int obtenerSemanaFinCurso()
	{
		LocalDate hoy = LocalDate.now();
		int year = hoy.getYear();

		// Determinar el año de inicio del curso actual.
		// Si estamos entre Enero y Junio (meses 1-6), el curso empezó en Septiembre del
		// año anterior.
		// Si estamos entre Septiembre y Diciembre (meses 9-12), el curso empezó en
		// Septiembre de este año
		int yearInicioCurso;
		if (hoy.getMonthValue() <= 6)
		{
			yearInicioCurso = year - 1;
		} else
		{
			yearInicioCurso = year;
		}

		// Fecha de inicio del curso (1 de Septiembre)
		LocalDate inicioCurso = LocalDate.of(yearInicioCurso, 9, 1);

		// Fecha de fin del curso (30 de Junio)
		LocalDate finCurso = LocalDate.of(yearInicioCurso + 1, 6, 30);

		// Calcular semanas desde inicio de curso hasta fin de curso
		long semanasTotales = ChronoUnit.WEEKS.between(inicioCurso, finCurso);

		/**log.info("=== DEBUG SEMANA FIN CURSO ===");
		log.info("Inicio curso: {}", inicioCurso);
		log.info("Fin curso: {}", finCurso);
		log.info("Semanas totales del curso: {}", semanasTotales); **/

		return (int) semanasTotales; // ≈ 43 semanas
	}

	/**
	 * Calcula la semana del curso escolar desde una fecha de creación. Curso: 1
	 * Septiembre - 30 Junio.
	 * 
	 * @param fecha Fecha de creación de la reserva fija.
	 * @return Número de semana dentro del curso escolar (1 = primera semana de
	 *         septiembre).
	 */
	private int calcularSemanaDesdeFecha(LocalDateTime fecha)
	{
		LocalDate date = fecha.toLocalDate();
		LocalDate inicioCurso = LocalDate.of(date.getYear(), 9, 1);

		// Si es antes de septiembre, el curso empezó el 1 de septiembre del año
		// anterior
		if (date.getMonthValue() < 9)
		{
			inicioCurso = LocalDate.of(date.getYear() - 1, 9, 1);
		}

		long semanas = ChronoUnit.WEEKS.between(inicioCurso, date);
		int semanaNumero = (int) semanas + 1; // Semana 1 = primera semana de curso

		/**log.info("  Calculando semana desde fecha: {}", fecha);
		log.info("  Inicio curso: {}", inicioCurso);
		log.info("  Semanas desde inicio: {}", semanas);
		log.info("  Semana número: {}", semanaNumero); **/

		return semanaNumero;
	}
}