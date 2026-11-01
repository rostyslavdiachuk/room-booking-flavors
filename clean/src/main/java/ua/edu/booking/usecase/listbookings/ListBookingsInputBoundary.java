package ua.edu.booking.usecase.listbookings;

import java.util.Optional;

public interface ListBookingsInputBoundary {

    record Request(Optional<String> room) {
    }

    void execute(Request request);
}
