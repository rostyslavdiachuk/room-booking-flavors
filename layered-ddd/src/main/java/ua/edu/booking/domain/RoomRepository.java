package ua.edu.booking.domain;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    List<Room> findAll();

    Optional<Room> findByNumber(String number);
}
