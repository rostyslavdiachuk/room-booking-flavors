package ua.edu.booking.business;

import ua.edu.booking.data.RoomDao;
import ua.edu.booking.model.Room;

import java.util.List;

public class RoomService {

    private final RoomDao roomDao = new RoomDao();

    // Architecture sinkhole: шар просто передає виклик далі
    public List<Room> listRooms() {
        return roomDao.findAll();
    }
}
