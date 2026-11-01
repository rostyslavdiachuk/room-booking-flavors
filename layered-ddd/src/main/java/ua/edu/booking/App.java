package ua.edu.booking;

import ua.edu.booking.application.BookingApplicationService;
import ua.edu.booking.infrastructure.ConsoleMailer;
import ua.edu.booking.infrastructure.CsvBookingRepository;
import ua.edu.booking.infrastructure.SeedRoomRepository;
import ua.edu.booking.ui.ConsoleUi;

import java.nio.file.Path;
import java.time.Clock;

public class App {
    public static void main(String[] args) {
        var service = new BookingApplicationService(
                new CsvBookingRepository(Path.of("bookings.csv")),
                new SeedRoomRepository(),
                new ConsoleMailer(),
                Clock.systemDefaultZone());
        new ConsoleUi(service).run();
    }
}
