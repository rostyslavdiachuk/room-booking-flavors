package ua.edu.booking.booking;

import java.time.LocalDateTime;

record Booking(int id, String room, LocalDateTime start, LocalDateTime end,
               String who, int attendees, boolean projector) {
}
