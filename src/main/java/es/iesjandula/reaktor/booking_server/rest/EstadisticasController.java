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
			// Mapa para acumular totales (clave = nombre del recurso y valor = total de semanas reservadas) 
			Map<String, Long> mapaTotales = new HashMap<>();			
			int semanaActual = this.calcularSemanaDesdeFecha(LocalDateTime.now());

			// Reservas fijas (calculamos cuántas semanas han pasado desde su creación hasta hoy)
			List<Object[]> reservasFijas = this.reservaFijaRepository.contarPorRecursoConFecha();
			for (Object[] fila : reservasFijas)
			{
				String recurso = (String) fila[0];
				LocalDateTime fechaCreacion = (LocalDateTime) fila[1];

				// Por defecto al menos hay una semana.
				long semanas = 1;
				if (fechaCreacion != null)
				{
					// Semana del curso en que se creó la reserva.
					int semanaCreacion = this.calcularSemanaDesdeFecha(fechaCreacion);
					// Si la reserva se creó antes o en la semana actual, la diferencia más uno nos da las semanas transcurridas.
					if (semanaActual >= semanaCreacion)
					{
						semanas = semanaActual - semanaCreacion + 1;
					}
				}
				
				// Acumulamos los datos en el mapa.
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

			// Reservas temporales (Cada reserva cuenta como una semana)
			List<Object[]> reservasTemporales = this.reservaTemporalRepository.contarPorRecurso();
			for (Object[] fila : reservasTemporales)
			{
				String recurso = (String) fila[0];
				Long conteo = (Long) fila[1];

				// Acumulamos los datos en el mapa.
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

			// Convertimos el mapa a una lista de DTOs
			List<EstadisticaRecursoMasReservadoDto> listaResultados = new ArrayList<>();
			for (String recurso : mapaTotales.keySet())
			{
				Long total = mapaTotales.get(recurso);
				listaResultados.add(new EstadisticaRecursoMasReservadoDto(recurso, total));
			}

			// Ordenamos la lista de mayor a menor usando el algoritmo burbuja
			this.ordenarResultadosRecurso(listaResultados);

			return ResponseEntity.ok(listaResultados);
		}
		// Si ocurre un error lo capturamos, lo registramos en el log y devolvemos un error con un mensaje JSON.
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
	// Método para saber qué tramo horario tiene más reservas combinando reservas fijas y temporales. 
	public ResponseEntity<?> obtenerTramoHorarioMasReservado()
	{
		try
		{
			Map<String, Long> mapaTotales = new HashMap<>();
			int semanaActual = this.calcularSemanaDesdeFecha(LocalDateTime.now());

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

			// Convertimos el mapa a una lista de DTOs
			List<EstadisticaDiaTramoMasReservadoDto> listaResultados = new ArrayList<>();
			for (String tramo : mapaTotales.keySet())
			{
				Long total = mapaTotales.get(tramo);
				// Como este DTO necesita día y tramo pasamos el día vacío ("") y el nombre del tramo.
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
			int semanaActual = this.calcularSemanaDesdeFecha(LocalDateTime.now());

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

	// Métodos para ordenar la cantidad de recursos, de tramos y de días.

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

	// Método para calcular el número de semanas desde el inicio del curso escolar hasta la fecha de la reserva.
	private int calcularSemanaDesdeFecha(LocalDateTime fecha)
	{
		LocalDate fechaLocal = fecha.toLocalDate();
		int anio = fechaLocal.getYear();
		// El curso escolar empieza el 1 de septiembre.
		LocalDate inicioCurso = LocalDate.of(anio, 9, 1);
		if (fechaLocal.getMonthValue() < 9)
		{
			inicioCurso = LocalDate.of(anio - 1, 9, 1);
		}
		// Contamos las semanas desde el inicio del curso hasta la fecha de la reserva.
		long semanas = ChronoUnit.WEEKS.between(inicioCurso, fechaLocal);
		return (int) semanas + 1;
	}
}