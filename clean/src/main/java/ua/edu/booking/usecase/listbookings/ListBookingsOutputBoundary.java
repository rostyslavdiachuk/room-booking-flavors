package ua.edu.booking.usecase.listbookings;

import java.time.LocalDateTime;
import java.util.List;

/** Модель відповіді належить use case: адаптери ніколи не бачать entity. */
public interface ListBookingsOutputBoundary {

    record Item(int id, String room, LocalDateTime start, LocalDateTime end, String who, int attendees, boolean projector) {
    }

    record Response(List<Item> items) {
    }

    void present(Response response);
}
