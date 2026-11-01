package ua.edu.booking.adapter.in.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ua.edu.booking.adapter.in.TextFormat;
import ua.edu.booking.application.port.in.BookRoomCommand;
import ua.edu.booking.application.port.in.BookRoomUseCase;
import ua.edu.booking.application.port.in.CancelBookingUseCase;
import ua.edu.booking.application.port.in.ListBookingsUseCase;
import ua.edu.booking.application.port.in.ListRoomsUseCase;
import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.BookingException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Другий вхідний адаптер — ті самі use case через HTTP (вбудований сервер JDK).
 *
 *   GET  /rooms
 *   GET  /bookings[?room=214]
 *   POST /bookings?room=214&start=2027-03-03T10:00&minutes=90&who=Petrenko&attendees=20[&projector=true]
 *   POST /bookings/cancel?id=1&who=Petrenko
 */
public class HttpBookingController {

    private final BookRoomUseCase bookRoom;
    private final CancelBookingUseCase cancelBooking;
    private final ListBookingsUseCase listBookings;
    private final ListRoomsUseCase listRooms;

    public HttpBookingController(BookRoomUseCase bookRoom, CancelBookingUseCase cancelBooking,
                                 ListBookingsUseCase listBookings, ListRoomsUseCase listRooms) {
        this.bookRoom = bookRoom;
        this.cancelBooking = cancelBooking;
        this.listBookings = listBookings;
        this.listRooms = listRooms;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.start();
        System.out.println("HTTP: http://localhost:" + port + "/bookings");
    }

    private void handle(HttpExchange ex) throws IOException {
        String route = ex.getRequestMethod() + " " + ex.getRequestURI().getPath();
        Map<String, String> q = query(ex);
        try {
            switch (route) {
                case "GET /rooms" -> send(ex, 200, listRooms.listRooms().stream()
                        .map(TextFormat::room).collect(Collectors.joining("\n")));
                case "GET /bookings" -> send(ex, 200, listBookings.list(Optional.ofNullable(q.get("room"))).stream()
                        .map(TextFormat::booking).collect(Collectors.joining("\n")));
                case "POST /bookings" -> {
                    Booking b = bookRoom.book(new BookRoomCommand(q.get("room"), LocalDateTime.parse(q.get("start")),
                            Integer.parseInt(q.get("minutes")), q.get("who"), Integer.parseInt(q.get("attendees")),
                            "true".equals(q.get("projector"))));
                    send(ex, 201, TextFormat.booked(b));
                }
                case "POST /bookings/cancel" -> {
                    Booking b = cancelBooking.cancel(Integer.parseInt(q.get("id")), q.get("who"));
                    send(ex, 200, "OK: #" + b.id() + " скасовано");
                }
                default -> send(ex, 404, "Not Found");
            }
        } catch (BookingException e) {
            send(ex, 409, "Помилка: " + e.getMessage());
        } catch (NullPointerException | DateTimeParseException | NumberFormatException e) {
            send(ex, 400, "Помилка: неправильний формат даних");
        }
    }

    private static Map<String, String> query(HttpExchange ex) {
        Map<String, String> result = new HashMap<>();
        String raw = ex.getRequestURI().getRawQuery();
        if (raw == null) return result;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) result.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return result;
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = (body + "\n").getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
