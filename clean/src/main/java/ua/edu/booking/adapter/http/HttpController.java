package ua.edu.booking.adapter.http;

import ua.edu.booking.usecase.bookroom.BookRoomInputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingInputBoundary;
import ua.edu.booking.usecase.listbookings.ListBookingsInputBoundary;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

/**
 *   GET  /rooms
 *   GET  /bookings[?room=214]
 *   POST /bookings?room=214&start=2027-03-03T10:00&minutes=90&who=Petrenko&attendees=20[&projector=true]
 *   POST /bookings/cancel?id=1&who=Petrenko
 */
public class HttpController {

    private final UseCaseFactory useCases;

    public HttpController(UseCaseFactory useCases) {
        this.useCases = useCases;
    }

    public HttpResult handle(String method, String path, Map<String, String> q) {
        JsonPresenter presenter = new JsonPresenter();
        try {
            switch (method + " " + path) {
                case "GET /rooms" -> useCases.listRooms(presenter).execute();
                case "GET /bookings" -> useCases.listBookings(presenter)
                        .execute(new ListBookingsInputBoundary.Request(Optional.ofNullable(q.get("room"))));
                case "POST /bookings" -> useCases.bookRoom(presenter).execute(new BookRoomInputBoundary.Request(
                        q.get("room"), LocalDateTime.parse(q.get("start")), Integer.parseInt(q.get("minutes")),
                        q.get("who"), Integer.parseInt(q.get("attendees")), "true".equals(q.get("projector"))));
                case "POST /bookings/cancel" -> useCases.cancelBooking(presenter)
                        .execute(new CancelBookingInputBoundary.Request(Integer.parseInt(q.get("id")), q.get("who")));
                default -> {
                    return new HttpResult(404, "{\"error\":\"not found\"}");
                }
            }
        } catch (NullPointerException | DateTimeParseException | NumberFormatException e) {
            return new HttpResult(400, "{\"error\":\"неправильний формат даних\"}");
        }
        return presenter.result();
    }
}
