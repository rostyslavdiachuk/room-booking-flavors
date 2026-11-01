package ua.edu.booking.adapter.gateway;

import ua.edu.booking.entity.Room;
import ua.edu.booking.usecase.gateway.RoomGateway;

import java.util.List;
import java.util.Optional;

public class SeedRoomGateway implements RoomGateway {

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
