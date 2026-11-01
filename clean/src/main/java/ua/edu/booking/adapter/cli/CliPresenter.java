package ua.edu.booking.adapter.cli;

import ua.edu.booking.usecase.bookroom.BookRoomOutputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingOutputBoundary;
import ua.edu.booking.usecase.listbookings.ListBookingsOutputBoundary;
import ua.edu.booking.usecase.listrooms.ListRoomsOutputBoundary;

/** Presenter: перетворює Response use case на текст для консолі. */
public class CliPresenter implements BookRoomOutputBoundary, CancelBookingOutputBoundary,
        ListBookingsOutputBoundary, ListRoomsOutputBoundary {

    private final CliView view;

    public CliPresenter(CliView view) {
        this.view = view;
    }

    @Override
    public void present(BookRoomOutputBoundary.Response r) {
        view.show("OK: #" + r.id() + " ауд. " + r.room() + " " + r.start() + "–" + r.end().toLocalTime());
    }

    @Override
    public void present(CancelBookingOutputBoundary.Response r) {
        view.show("OK: #" + r.id() + " скасовано");
    }

    @Override
    public void present(ListBookingsOutputBoundary.Response r) {
        if (r.items().isEmpty()) view.show("(порожньо)");
        for (ListBookingsOutputBoundary.Item b : r.items()) {
            view.show("#" + b.id() + " | ауд. " + b.room() + " | " + b.start() + "–" + b.end().toLocalTime()
                    + " | " + b.who() + " | " + b.attendees() + " осіб" + (b.projector() ? " | проєктор" : ""));
        }
    }

    @Override
    public void present(ListRoomsOutputBoundary.Response r) {
        for (ListRoomsOutputBoundary.Item room : r.rooms()) {
            view.show("ауд. " + room.number() + " | " + room.capacity() + " місць | "
                    + (room.projector() ? "проєктор" : "без проєктора"));
        }
    }

    @Override
    public void presentError(String message) {
        view.show("Помилка: " + message);
    }
}
