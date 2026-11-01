package ua.edu.booking.application.port.in;

import ua.edu.booking.domain.Booking;

public interface CancelBookingUseCase {
    Booking cancel(int id, String who);
}
