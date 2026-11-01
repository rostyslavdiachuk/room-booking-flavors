package ua.edu.booking;

import ua.edu.booking.adapter.in.cli.ConsoleUi;
import ua.edu.booking.adapter.in.http.HttpBookingController;
import ua.edu.booking.adapter.out.notification.ConsoleEmailNotifier;
import ua.edu.booking.adapter.out.persistence.CsvBookingRepository;
import ua.edu.booking.adapter.out.persistence.InMemoryBookingRepository;
import ua.edu.booking.adapter.out.persistence.SeedRoomRepository;
import ua.edu.booking.application.BookingService;
import ua.edu.booking.application.RoomQueryService;
import ua.edu.booking.application.port.out.BookingRepository;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

/**
 * Composition root — єдине місце, яке знає всі адаптери.
 *
 *   java ua.edu.booking.App              консоль + CSV
 *   java ua.edu.booking.App memory       консоль + пам'ять
 *   java ua.edu.booking.App http         HTTP + CSV
 */
public class App {
    public static void main(String[] args) throws Exception {
        List<String> a = List.of(args);
        BookingRepository bookings = a.contains("memory")
                ? new InMemoryBookingRepository()
                : new CsvBookingRepository(Path.of("bookings.csv"));
        var rooms = new SeedRoomRepository();
        var service = new BookingService(bookings, rooms, new ConsoleEmailNotifier(), Clock.systemDefaultZone());
        var roomQueries = new RoomQueryService(rooms);

        if (a.contains("http")) {
            new HttpBookingController(service, service, service, roomQueries).start(8080);
        } else {
            new ConsoleUi(service, service, service, roomQueries).run();
        }
    }
}
