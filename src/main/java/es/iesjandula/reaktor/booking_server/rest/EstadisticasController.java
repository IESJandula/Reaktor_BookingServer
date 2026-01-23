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
import es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaTramoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.repository.LogReservasRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;

/**
 * Controlador REST para proporcionar estadísticas del sistema de reservas.
 * Accesible solo para roles de administrador o dirección.
 */
@RequestMapping("/bookings/estadisticas")
@RestController
public class EstadisticasController {

    private static final Logger log = LoggerFactory.getLogger(EstadisticasController.class);

    @Autowired
    private LogReservasRepository logReservasRepository;

    /**
     * Devuelve el recurso más reservado (suma de fijas y temporales).
     * 
     * @return Lista de {@link EstadisticaRecursoMasReservadoDto} con el recurso y total de reservas.
     */
    @PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
    @GetMapping("/recurso-mas-reservado")
    public ResponseEntity<?> obtenerRecursoMasReservado() {
        try {
            List<EstadisticaRecursoMasReservadoDto> estadisticas = logReservasRepository.obtenerRecursoMasReservado();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception ex) {
            ReservaException e = new ReservaException(
                Constants.ERROR_ESTADISTICAS,
                "Error al obtener el recurso más reservado",
                ex
            );
            log.error("Error en /estadisticas/recurso-mas-reservado", e);
            return ResponseEntity.status(500).body(e.getBodyMesagge());
        }
    }

    /**
     * Devuelve el día de la semana y tramo horario más reservado.
     * 
     * @return Lista de {@link EstadisticaDiaTramoMasReservadoDto} con día, tramo y total de reservas.
     */
    @PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
    @GetMapping("/dia-tramo-mas-reservado")
    public ResponseEntity<?> obtenerDiaTramoMasReservado() {
        try {
            List<EstadisticaDiaTramoMasReservadoDto> estadisticas = logReservasRepository.obtenerDiaTramoMasReservado();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception ex) {
            ReservaException e = new ReservaException(
                Constants.ERROR_ESTADISTICAS,
                "Error al obtener el día y tramo más reservado",
                ex
            );
            log.error("Error en /estadisticas/dia-tramo-mas-reservado", e);
            return ResponseEntity.status(500).body(e.getBodyMesagge());
        }
    }
}