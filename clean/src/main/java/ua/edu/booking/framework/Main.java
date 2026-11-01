package ua.edu.booking.framework;

import ua.edu.booking.adapter.cli.CliController;
import ua.edu.booking.adapter.cli.CliPresenter;
import ua.edu.booking.adapter.cli.CliView;
import ua.edu.booking.adapter.gateway.CsvBookingGateway;
import ua.edu.booking.adapter.gateway.InMemoryBookingGateway;
import ua.edu.booking.adapter.gateway.SeedRoomGateway;
import ua.edu.booking.adapter.http.HttpController;
import ua.edu.booking.adapter.notification.ConsoleEmailNotifier;
import ua.edu.booking.usecase.gateway.BookingGateway;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

/**
 * Composition root у найзовнішньому колі.
 *
 *   java ua.edu.booking.framework.Main              консоль + CSV
 *   java ua.edu.booking.framework.Main memory       консоль + пам'ять
 *   java ua.edu.booking.framework.Main http         HTTP (JSON) + CSV
 */
public class Main {
    public static void main(String[] args) throws Exception {
        List<String> a = List.of(args);
        BookingGateway bookings = a.contains("memory")
                ? new InMemoryBookingGateway()
                : new CsvBookingGateway(Path.of("bookings.csv"));
        var useCases = new UseCases(bookings, new SeedRoomGateway(), new ConsoleEmailNotifier(), Clock.systemDefaultZone());

        if (a.contains("http")) {
            new WebServer(new HttpController(useCases)).start(8080);
            return;
        }
        CliView view = System.out::println;
        var presenter = new CliPresenter(view);
        var controller = new CliController(useCases.bookRoom(presenter), useCases.cancelBooking(presenter),
                useCases.listBookings(presenter), useCases.listRooms(presenter), view);
        new ConsoleLoop(controller).run();
    }
}
