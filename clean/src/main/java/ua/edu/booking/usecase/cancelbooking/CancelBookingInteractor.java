package ua.edu.booking.usecase.cancelbooking;

import ua.edu.booking.entity.Booking;
import ua.edu.booking.entity.BookingException;
import ua.edu.booking.usecase.gateway.BookingGateway;
import ua.edu.booking.usecase.gateway.Notifier;

public class CancelBookingInteractor implements CancelBookingInputBoundary {

    private final BookingGateway bookings;
    private final Notifier notifier;
    private final CancelBookingOutputBoundary presenter;

    public CancelBookingInteractor(BookingGateway bookings, Notifier notifier, CancelBookingOutputBoundary presenter) {
        this.bookings = bookings;
        this.notifier = notifier;
        this.presenter = presenter;
    }

    @Override
    public void execute(Request r) {
        try {
            Booking booking = bookings.findById(r.id())
                    .orElseThrow(() -> new BookingException("бронювання #" + r.id() + " не знайдено"));
            booking.checkCanBeCancelledBy(r.who());
            bookings.delete(r.id());
            notifier.notify(r.who(), "Бронювання #" + r.id() + " скасовано");
            presenter.present(new CancelBookingOutputBoundary.Response(r.id()));
        } catch (BookingException e) {
            presenter.presentError(e.getMessage());
        }
    }
}
