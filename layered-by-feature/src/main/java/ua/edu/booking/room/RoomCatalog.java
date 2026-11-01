package ua.edu.booking.room;

import java.util.List;
import java.util.Optional;

/** Публічний контракт фічі «аудиторії». Інші фічі бачать лише його. */
public interface RoomCatalog {

    List<Room> findAll();

    Optional<Room> findByNumber(String number);

    static RoomCatalog create() {
        return new RoomRepository();
    }
}
