package es.iesjandula.reaktor.bookings_server.repository.reservas_puntuales;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.bookings_server.models.reservas_puntuales.Classroom2;
import es.iesjandula.reaktor.bookings_server.models.reservas_puntuales.ClassroomId;

public interface IClassroom2Repository extends JpaRepository<Classroom2, ClassroomId>
{

}
