package ua.edu.booking.application.port.in;

import ua.edu.booking.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface ListBookingsUseCase {
    List<Booking> list(Optional<String> room);
}
