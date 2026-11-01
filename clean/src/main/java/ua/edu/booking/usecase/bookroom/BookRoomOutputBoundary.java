package ua.edu.booking.usecase.bookroom;

import java.time.LocalDateTime;

/** Вихідна межа: use case не повертає результат, а віддає його презентеру. */
public interface BookRoomOutputBoundary {

    record Response(int id, String room, LocalDateTime start, LocalDateTime end) {
    }

    void present(Response response);

    void presentError(String message);
}
