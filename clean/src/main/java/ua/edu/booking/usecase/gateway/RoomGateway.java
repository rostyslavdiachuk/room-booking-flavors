package ua.edu.booking.usecase.gateway;

import ua.edu.booking.entity.Room;

import java.util.List;
import java.util.Optional;

public interface RoomGateway {

    List<Room> findAll();

    Optional<Room> findByNumber(String number);
}
