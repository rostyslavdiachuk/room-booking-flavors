package ua.edu.booking.room;

/** Шар представлення фічі «аудиторії». */
public class RoomCommands {

    private final RoomCatalog catalog;

    public RoomCommands(RoomCatalog catalog) {
        this.catalog = catalog;
    }

    public void rooms() {
        for (Room r : catalog.findAll()) {
            System.out.println("ауд. " + r.number() + " | " + r.capacity() + " місць | "
                    + (r.projector() ? "проєктор" : "без проєктора"));
        }
    }
}
