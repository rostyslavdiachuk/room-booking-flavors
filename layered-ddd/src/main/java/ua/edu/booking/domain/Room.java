package ua.edu.booking.domain;

/** Аудиторія сама знає, кого може прийняти. */
public record Room(String number, int capacity, boolean projector) {

    public void checkCanHost(int attendees, boolean needsProjector) {
        if (attendees > capacity) {
            throw new BookingException("аудиторія " + number + " вміщує лише " + capacity + " осіб");
        }
        if (needsProjector && !projector) {
            throw new BookingException("в аудиторії " + number + " немає проєктора");
        }
    }
}
