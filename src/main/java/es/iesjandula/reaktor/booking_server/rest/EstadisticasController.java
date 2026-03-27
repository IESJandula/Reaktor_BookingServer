package es.iesjandula.reaktor.booking_server.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@RequestMapping("/bookings/estadisticas")
@RestController
public class EstadisticasController
{
	private static final Logger log = LoggerFactory.getLogger(EstadisticasController.class);

	@Autowired
	private IReservaFijaRepository reservaFijaRepository;

	@Autowired
	private IReservaTemporalRepository reservaTemporalRepository;

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/recurso-mas-reservado")
	public ResponseEntity<?> obtenerRecursoMasReservado()
	{
		try
		{
			Map<String, Long> mapaTotales = new HashMap<>();
			int semanaActual = this.obtenerSemanaActualCurso();

			// Reservas fijas
			List<Object[]> reservasFijas = this.reservaFijaRepository.contarPorRecursoConFecha();
			for (Object[] fila : reservasFijas)
			{
				String recurso = (String) fila[0];
				LocalDateTime fechaCreacion = (LocalDateTime) fila[1];

				long semanas = 1;
				if (fechaCreacion != null)
				{
					int semanaCreacion = this.calcularSemanaDesdeFecha(fechaCreacion);
					if (semanaActual >= semanaCreacion)
					{
						semanas = semanaActual - semanaCreacion + 1;
					}
				}

				Long totalActual = mapaTotales.get(recurso);
				if (totalActual == null)
				{
					mapaTotales.put(recurso, semanas);
				}
				else
				{
					mapaTotales.put(recurso, totalActual + semanas);
				}
			}

			// Reservas temporales
			List<Object[]> reservasTemporales = this.reservaTemporalRepository.contarPorRecurso();
			for (Object[] fila : reservasTemporales)
			{
				String recurso = (String) fila[0];
				Long conteo = (Long) fila[1];

				Long totalActual = mapaTotales.get(recurso);
				if (totalActual == null)
				{
					mapaTotales.put(recurso, conteo);
				}
				else
				{
					mapaTotales.put(recurso, totalActual + conteo);
				}
			}

			// Convertir a lista de DTOs
			List<EstadisticaRecursoMasReservadoDto> listaResultados = new ArrayList<>();
			for (String recurso : mapaTotales.keySet())
			{
				Long total = mapaTotales.get(recurso);
				listaResultados.add(new EstadisticaRecursoMasReservadoDto(recurso, total));
			}

			// Ordenar
			this.ordenarResultadosRecurso(listaResultados);

			return ResponseEntity.ok(listaResultados);
		}
		catch (Exception exception)
		{
			String mensajeError = "Error inesperado al obtener el recurso más reservado";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_ESTADISTICAS, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/tramo-horario-mas-reservado")
	public ResponseEntity<?> obtenerTramoHorarioMasReservado()
	{
		try
		{
			Map<String, Long> mapaTotales = new HashMap<>();
			int semanaActual = this.obtenerSemanaActualCurso();

			// Reservas fijas
			List<Object[]> reservasFijas = this.reservaFijaRepository.contarPorTramoConNombre();
			for (Object[] fila : reservasFijas)
			{
				String tramo = (String) fila[0];
				LocalDateTime fechaCreacion = (LocalDateTime) fila[1];

				long semanas = 1;
				if (fechaCreacion != null)
				{
					int semanaCreacion = this.calcularSemanaDesdeFecha(fechaCreacion);
					if (semanaActual >= semanaCreacion)
					{
						semanas = semanaActual - semanaCreacion + 1;
					}
				}

				Long totalActual = mapaTotales.get(tramo);
				if (totalActual == null)
					mapaTotales.put(tramo, semanas);
				else
					mapaTotales.put(tramo, totalActual + semanas);
			}

			// Reservas temporales
			List<Object[]> reservasTemporales = this.reservaTemporalRepository.contarPorTramoConNombre();
			for (Object[] fila : reservasTemporales)
			{
				String tramo = (String) fila[0];
				Long conteo = (Long) fila[1];

				Long totalActual = mapaTotales.get(tramo);
				if (totalActual == null)
					mapaTotales.put(tramo, conteo);
				else
					mapaTotales.put(tramo, totalActual + conteo);
			}

			// Convertir a DTOs
			List<EstadisticaDiaTramoMasReservadoDto> listaResultados = new ArrayList<>();
			for (String tramo : mapaTotales.keySet())
			{
				Long total = mapaTotales.get(tramo);
				listaResultados.add(new EstadisticaDiaTramoMasReservadoDto("", tramo, total));
			}

			this.ordenarResultadosTramo(listaResultados);

			return ResponseEntity.ok(listaResultados);
		}
		catch (Exception exception)
		{
			String mensajeError = "Error inesperado al obtener el tramo horario más reservado";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_ESTADISTICAS, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/dia-semana-mas-reservado")
	public ResponseEntity<?> obtenerDiaSemanaMasReservado()
	{
		try
		{
			Map<String, Long> mapaTotales = new HashMap<>();
			int semanaActual = this.obtenerSemanaActualCurso();

			// Reservas fijas
			List<Object[]> reservasFijas = this.reservaFijaRepository.contarPorDiaConNombre();
			for (Object[] fila : reservasFijas)
			{
				String dia = (String) fila[0];
				LocalDateTime fechaCreacion = (LocalDateTime) fila[1];

				long semanas = 1;
				if (fechaCreacion != null)
				{
					int semanaCreacion = this.calcularSemanaDesdeFecha(fechaCreacion);
					if (semanaActual >= semanaCreacion)
					{
						semanas = semanaActual - semanaCreacion + 1;
					}
				}

				Long totalActual = mapaTotales.get(dia);
				if (totalActual == null)
					mapaTotales.put(dia, semanas);
				else
					mapaTotales.put(dia, totalActual + semanas);
			}

			// Reservas temporales
			List<Object[]> reservasTemporales = this.reservaTemporalRepository.contarPorDiaConNombre();
			for (Object[] fila : reservasTemporales)
			{
				String dia = (String) fila[0];
				Long conteo = (Long) fila[1];

				Long totalActual = mapaTotales.get(dia);
				if (totalActual == null)
					mapaTotales.put(dia, conteo);
				else
					mapaTotales.put(dia, totalActual + conteo);
			}

			// Convertir a DTOs
			List<EstadisticaDiaTramoMasReservadoDto> listaResultados = new ArrayList<>();
			for (String dia : mapaTotales.keySet())
			{
				Long total = mapaTotales.get(dia);
				listaResultados.add(new EstadisticaDiaTramoMasReservadoDto(dia, "", total));
			}

			this.ordenarResultadosDia(listaResultados);

			return ResponseEntity.ok(listaResultados);
		}
		catch (Exception exception)
		{
			String mensajeError = "Error inesperado al obtener el día de la semana más reservado";
			log.error(mensajeError, exception);
			ReservaException reservaException = new ReservaException(Constants.ERROR_ESTADISTICAS, mensajeError, exception);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	// MÉTODOS AUXILIARES

	private void ordenarResultadosRecurso(List<EstadisticaRecursoMasReservadoDto> lista)
	{
		int tamano = lista.size();
		for (int i = 0; i < tamano - 1; i++)
		{
			for (int j = 0; j < tamano - i - 1; j++)
			{
				EstadisticaRecursoMasReservadoDto actual = lista.get(j);
				EstadisticaRecursoMasReservadoDto siguiente = lista.get(j + 1);
				if (actual.getTotalReservas() < siguiente.getTotalReservas())
				{
					lista.set(j, siguiente);
					lista.set(j + 1, actual);
				}
			}
		}
	}

	private void ordenarResultadosTramo(List<EstadisticaDiaTramoMasReservadoDto> lista)
	{
		int tamano = lista.size();
		for (int i = 0; i < tamano - 1; i++)
		{
			for (int j = 0; j < tamano - i - 1; j++)
			{
				EstadisticaDiaTramoMasReservadoDto actual = lista.get(j);
				EstadisticaDiaTramoMasReservadoDto siguiente = lista.get(j + 1);
				if (actual.getTotalReservas() < siguiente.getTotalReservas())
				{
					lista.set(j, siguiente);
					lista.set(j + 1, actual);
				}
			}
		}
	}

	private void ordenarResultadosDia(List<EstadisticaDiaTramoMasReservadoDto> lista)
	{
		int tamano = lista.size();
		for (int i = 0; i < tamano - 1; i++)
		{
			for (int j = 0; j < tamano - i - 1; j++)
			{
				EstadisticaDiaTramoMasReservadoDto actual = lista.get(j);
				EstadisticaDiaTramoMasReservadoDto siguiente = lista.get(j + 1);
				if (actual.getTotalReservas() < siguiente.getTotalReservas())
				{
					lista.set(j, siguiente);
					lista.set(j + 1, actual);
				}
			}
		}
	}

	private int obtenerSemanaActualCurso()
	{
		return this.calcularSemanaDesdeFecha(LocalDateTime.now());
	}

	private int calcularSemanaDesdeFecha(LocalDateTime fecha)
	{
		LocalDate fechaLocal = fecha.toLocalDate();
		int anio = fechaLocal.getYear();
		LocalDate inicioCurso = LocalDate.of(anio, 9, 1);
		if (fechaLocal.getMonthValue() < 9)
		{
			inicioCurso = LocalDate.of(anio - 1, 9, 1);
		}
		long semanas = ChronoUnit.WEEKS.between(inicioCurso, fechaLocal);
		return (int) semanas + 1;
	}
}