package ua.edu.booking.usecase.listbookings;

import ua.edu.booking.entity.Booking;
import ua.edu.booking.usecase.gateway.BookingGateway;

import java.util.List;

public class ListBookingsInteractor implements ListBookingsInputBoundary {

    private final BookingGateway bookings;
    private final ListBookingsOutputBoundary presenter;

    public ListBookingsInteractor(BookingGateway bookings, ListBookingsOutputBoundary presenter) {
        this.bookings = bookings;
        this.presenter = presenter;
    }

    @Override
    public void execute(Request r) {
        List<Booking> found = r.room().map(bookings::findByRoom).orElseGet(bookings::findAll);
        presenter.present(new ListBookingsOutputBoundary.Response(found.stream()
                .map(b -> new ListBookingsOutputBoundary.Item(b.id(), b.room(), b.start(), b.end(), b.who(),
                        b.attendees(), b.projector()))
                .toList()));
    }
}
