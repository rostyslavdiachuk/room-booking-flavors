package ua.edu.booking.room;

import java.util.List;
import java.util.Optional;

/** package-private: поза фічею цей клас не видно. */
class RoomRepository implements RoomCatalog {

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
