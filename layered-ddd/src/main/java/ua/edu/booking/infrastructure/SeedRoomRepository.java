package ua.edu.booking.infrastructure;

import ua.edu.booking.domain.Room;
import ua.edu.booking.domain.RoomRepository;

import java.util.List;
import java.util.Optional;

public class SeedRoomRepository implements RoomRepository {

    private static final List<Room> ROOMS = List.of(
            new Room("101", 30, true),
            new Room("214", 25, false),
            new Room("305", 15, true));

    @Override
    public List<Room> findAll() {
        return ROOMS;
    }

    @Override
    public Optional<Room> findByNumber(String number) {
        return ROOMS.stream().filter(r -> r.number().equals(number)).findFirst();
    }
}
