package ua.edu.booking.application;

import ua.edu.booking.application.port.in.ListRoomsUseCase;
import ua.edu.booking.application.port.out.RoomRepository;
import ua.edu.booking.domain.Room;

import java.util.List;

public class RoomQueryService implements ListRoomsUseCase {

    private final RoomRepository rooms;

    public RoomQueryService(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @Override
    public List<Room> listRooms() {
        return rooms.findAll();
    }
}
