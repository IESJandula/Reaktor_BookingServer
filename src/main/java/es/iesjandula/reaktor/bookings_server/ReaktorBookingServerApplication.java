package es.iesjandula.reaktor.bookings_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "es.iesjandula" })
public class ReaktorBookingServerApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(ReaktorBookingServerApplication.class, args);
	}
}
