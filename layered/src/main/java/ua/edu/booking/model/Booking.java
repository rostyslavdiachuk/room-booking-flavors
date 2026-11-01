package ua.edu.booking.model;

import java.time.LocalDateTime;

/** Анемічна модель: лише дані. Усі правила живуть у BookingService. */
public record Booking(int id, String room, LocalDateTime start, LocalDateTime end,
                      String who, int attendees, boolean projector) {
}
