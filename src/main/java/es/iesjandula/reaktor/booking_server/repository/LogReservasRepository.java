package es.iesjandula.reaktor.booking_server.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.booking_server.models.LogReservas;

@Repository
public interface LogReservasRepository extends JpaRepository<LogReservas, Date>
{


}
