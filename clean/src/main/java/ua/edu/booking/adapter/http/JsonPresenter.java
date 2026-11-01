package ua.edu.booking.adapter.http;

import ua.edu.booking.usecase.bookroom.BookRoomOutputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingOutputBoundary;
import ua.edu.booking.usecase.listbookings.ListBookingsOutputBoundary;
import ua.edu.booking.usecase.listrooms.ListRoomsOutputBoundary;

import java.util.stream.Collectors;

/** Той самий Response, інший вигляд: JSON замість тексту. Use case про це не знає. */
public class JsonPresenter implements BookRoomOutputBoundary, CancelBookingOutputBoundary,
        ListBookingsOutputBoundary, ListRoomsOutputBoundary {

    private HttpResult result = new HttpResult(500, "{}");

    public HttpResult result() {
        return result;
    }

    @Override
    public void present(BookRoomOutputBoundary.Response r) {
        result = new HttpResult(201, "{\"id\":" + r.id() + ",\"room\":" + str(r.room())
                + ",\"start\":" + str(r.start().toString()) + ",\"end\":" + str(r.end().toString()) + "}");
    }

    @Override
    public void present(CancelBookingOutputBoundary.Response r) {
        result = new HttpResult(200, "{\"cancelled\":" + r.id() + "}");
    }

    @Override
    public void present(ListBookingsOutputBoundary.Response r) {
        result = new HttpResult(200, r.items().stream()
                .map(b -> "{\"id\":" + b.id() + ",\"room\":" + str(b.room()) + ",\"start\":" + str(b.start().toString())
                        + ",\"end\":" + str(b.end().toString()) + ",\"who\":" + str(b.who())
                        + ",\"attendees\":" + b.attendees() + ",\"projector\":" + b.projector() + "}")
                .collect(Collectors.joining(",", "[", "]")));
    }

    @Override
    public void present(ListRoomsOutputBoundary.Response r) {
        result = new HttpResult(200, r.rooms().stream()
                .map(x -> "{\"number\":" + str(x.number()) + ",\"capacity\":" + x.capacity()
                        + ",\"projector\":" + x.projector() + "}")
                .collect(Collectors.joining(",", "[", "]")));
    }

    @Override
    public void presentError(String message) {
        result = new HttpResult(409, "{\"error\":" + str(message) + "}");
    }

    private static String str(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
