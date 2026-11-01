package ua.edu.booking.application.port.in;

import java.time.LocalDateTime;

public record BookRoomCommand(String room, LocalDateTime start, int minutes, String who,
                              int attendees, boolean projector) {
}
