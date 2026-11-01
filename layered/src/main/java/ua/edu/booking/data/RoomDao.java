package ua.edu.booking.data;

import ua.edu.booking.model.Room;

import java.util.List;
import java.util.Optional;

public class RoomDao {

    private static final List<Room> ROOMS = List.of(
            new Room("101", 30, true),
            new Room("214", 25, false),
            new Room("305", 15, true));

    public List<Room> findAll() {
        return ROOMS;
    }

    public Optional<Room> findByNumber(String number) {
        return ROOMS.stream().filter(r -> r.number().equals(number)).findFirst();
    }
}
