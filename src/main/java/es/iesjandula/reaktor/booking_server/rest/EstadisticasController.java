package es.iesjandula.reaktor.booking_server.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaTramoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.repository.IReservaFijaRepository;
import es.iesjandula.reaktor.booking_server.repository.IReservaTemporalRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;

/**
 * Controlador REST para proporcionar estadísticas del sistema de reservas.
 * Accesible solo para roles de administrador o dirección.
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

	// ========== ESTADÍSTICAS RESERVAS FIJAS ==========

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@GetMapping("/recurso-mas-reservado-fija")
	public ResponseEntity<?> obtenerRecursoMasReservadoFija()
	{
		try
		{
			List<EstadisticaRecursoMasReservadoDto> estadisticas = reservaFijaRepository
					.obtenerRecursoMasReservadoFija();
			return ResponseEntity.ok(estadisticas);
		} catch (Exception exception)
		{
			ReservaException e = new ReservaException(Constants.ERROR_ESTADISTICAS,
					"Error al obtener el recurso más reservado (fija)", exception);
			log.error("Error en /estadisticas/recurso-mas-reservado-fija", e);
			return ResponseEntity.status(500).body(e.getBodyMesagge());
		}
	}

	// ========== ESTADÍSTICAS COMBINADAS (FIJAS + TEMPORALES) ==========

	/**
	 * Devuelve el DÍA de la semana más reservado (SUMA de Fijas + Temporales).
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@GetMapping("/dia-mas-reservado")
	public ResponseEntity<?> obtenerDiaMasReservado()
	{
		try
		{
			// 1. Obtener estadísticas fijas
			List<EstadisticaDiaMasReservadoDto> estadisticas = this.reservaFijaRepository.obtenerDiaMasReservadoFija();

			// 2. Obtener estadísticas temporales
			List<EstadisticaDiaMasReservadoDto> estadisticasTemporales = this.reservaTemporalRepository
					.obtenerDiaMasReservadoTemporal();

			// 3. Combinar si hay temporales
			if (estadisticasTemporales != null && !estadisticasTemporales.isEmpty())
			{
				if (estadisticas == null || estadisticas.isEmpty())
				{
					estadisticas = estadisticasTemporales;
				} else
				{
					this.combinarEstadisticasDia(estadisticas, estadisticasTemporales);
				}
			}

			return ResponseEntity.ok(estadisticas);
		} catch (Exception exception)
		{
			ReservaException e = new ReservaException(Constants.ERROR_ESTADISTICAS,
					"Error al obtener el día más reservado (combinado)", exception);
			log.error("Error en /estadisticas/dia-mas-reservado", e);
			return ResponseEntity.status(500).body(e.getBodyMesagge());
		}
	}

	/**
	 * Devuelve el TRAMO HORARIO más reservado (SUMA de Fijas + Temporales).	 
	 */
	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@GetMapping("/tramo-mas-reservado")
	public ResponseEntity<?> obtenerTramoMasReservado()
	{
		try
		{
			// 1. Obtener estadísticas fijas
			List<EstadisticaTramoMasReservadoDto> estadisticas = this.reservaFijaRepository
					.obtenerTramoMasReservadoFija();

			// 2. Obtener estadísticas temporales
			List<EstadisticaTramoMasReservadoDto> estadisticasTemporales = this.reservaTemporalRepository
					.obtenerTramoMasReservadoTemporal();

			// 3. Combinar si hay temporales
			if (estadisticasTemporales != null && !estadisticasTemporales.isEmpty())
			{
				if (estadisticas == null || estadisticas.isEmpty())
				{
					estadisticas = estadisticasTemporales;
				} else
				{
					this.combinarEstadisticasTramo(estadisticas, estadisticasTemporales);
				}
			}

			return ResponseEntity.ok(estadisticas);
		} catch (Exception exception)
		{
			ReservaException e = new ReservaException(Constants.ERROR_ESTADISTICAS,
					"Error al obtener el tramo más reservado (combinado)", exception);
			log.error("Error en /estadisticas/tramo-mas-reservado", e);
			return ResponseEntity.status(500).body(e.getBodyMesagge());
		}
	}

	// ========== ESTADÍSTICAS SOLO TEMPORALES ==========

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@GetMapping("/recurso-mas-reservado-temporal")
	public ResponseEntity<?> obtenerRecursoMasReservadoTemporal()
	{
		try
		{
			List<EstadisticaRecursoMasReservadoDto> estadisticas = reservaTemporalRepository
					.obtenerRecursoMasReservadoTemporal();
			return ResponseEntity.ok(estadisticas);
		} catch (Exception exception)
		{
			ReservaException e = new ReservaException(Constants.ERROR_ESTADISTICAS,
					"Error al obtener el recurso más reservado (temporal)", exception);
			log.error("Error en /estadisticas/recurso-mas-reservado-temporal", e);
			return ResponseEntity.status(500).body(e.getBodyMesagge());
		}
	}

	// ========== MÉTODOS AUXILIARES PARA COMBINAR ==========

	private void combinarEstadisticasDia(List<EstadisticaDiaMasReservadoDto> estadisticas,
			List<EstadisticaDiaMasReservadoDto> estadisticasTemporales)
	{
		for (EstadisticaDiaMasReservadoDto temp : estadisticasTemporales)
		{
			int indice = estadisticas.indexOf(temp);
			if (indice != -1)
			{
				EstadisticaDiaMasReservadoDto encontrada = estadisticas.get(indice);
				encontrada.setTotalReservas(encontrada.getTotalReservas() + temp.getTotalReservas());
			} else
			{
				estadisticas.add(temp);
			}
		}
	}
	
	private void combinarEstadisticasTramo(List<EstadisticaTramoMasReservadoDto> estadisticas,
			List<EstadisticaTramoMasReservadoDto> estadisticasTemporales)
	{
		for (EstadisticaTramoMasReservadoDto temp : estadisticasTemporales)
		{
			int indice = estadisticas.indexOf(temp);
			if (indice != -1)
			{
				EstadisticaTramoMasReservadoDto encontrada = estadisticas.get(indice);
				encontrada.setTotalReservas(encontrada.getTotalReservas() + temp.getTotalReservas());
			} else
			{
				estadisticas.add(temp);
			}
		}
	}
}