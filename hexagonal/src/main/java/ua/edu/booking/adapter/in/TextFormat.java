package ua.edu.booking.adapter.in;

import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.Room;

/** Спільне текстове форматування для вхідних адаптерів (CLI і HTTP). */
public final class TextFormat {

    private TextFormat() {
    }

    public static String room(Room r) {
        return "ауд. " + r.number() + " | " + r.capacity() + " місць | " + (r.projector() ? "проєктор" : "без проєктора");
    }

    public static String booking(Booking b) {
        return "#" + b.id() + " | ауд. " + b.room() + " | " + b.start() + "–" + b.end().toLocalTime()
                + " | " + b.who() + " | " + b.attendees() + " осіб" + (b.projector() ? " | проєктор" : "");
    }

    public static String booked(Booking b) {
        return "OK: #" + b.id() + " ауд. " + b.room() + " " + b.start() + "–" + b.end().toLocalTime();
    }
}
