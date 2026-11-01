package ua.edu.booking.domain;

import java.time.LocalDateTime;

/** Багата модель: правила живуть разом із даними. Без I/O і без «зараз». */
public record Booking(int id, String room, LocalDateTime start, LocalDateTime end,
                      String who, int attendees, boolean projector) {

    public static final int MAX_MINUTES = 180;

    public static Booking create(int id, Room room, LocalDateTime start, int minutes, String who,
                                 int attendees, boolean needsProjector, LocalDateTime now) {
        if (start.isBefore(now)) {
            throw new BookingException("не можна бронювати в минулому");
        }
        if (minutes <= 0 || minutes > MAX_MINUTES) {
            throw new BookingException("тривалість має бути від 1 до " + MAX_MINUTES + " хв");
        }
        if (attendees <= 0) {
            throw new BookingException("кількість осіб має бути додатною");
        }
        room.checkCanHost(attendees, needsProjector);
        return new Booking(id, room.number(), start, start.plusMinutes(minutes), who, attendees, needsProjector);
    }

    public boolean overlaps(Booking other) {
        return room.equals(other.room) && start.isBefore(other.end) && other.start.isBefore(end);
    }

    public void checkCanBeCancelledBy(String person) {
        if (!who.equals(person)) {
            throw new BookingException("скасувати може лише автор бронювання");
        }
    }
}
