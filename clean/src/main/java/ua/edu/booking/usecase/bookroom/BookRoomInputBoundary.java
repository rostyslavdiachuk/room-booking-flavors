package ua.edu.booking.usecase.bookroom;

import java.time.LocalDateTime;

/** Вхідна межа use case: що приходить ззовні — лише прості дані. */
public interface BookRoomInputBoundary {

    record Request(String room, LocalDateTime start, int minutes, String who, int attendees, boolean projector) {
    }

    void execute(Request request);
}
