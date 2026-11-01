package ua.edu.booking.adapter.gateway;

import ua.edu.booking.entity.Booking;
import ua.edu.booking.usecase.gateway.BookingGateway;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** CSV: id;room;start;end;who;attendees;projector */
public class CsvBookingGateway implements BookingGateway {

    private final Path file;

    public CsvBookingGateway(Path file) {
        this.file = file;
    }

    @Override
    public List<Booking> findAll() {
        try {
            if (!Files.exists(file)) return List.of();
            return Files.readAllLines(file).stream().map(CsvBookingGateway::parse).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Booking> findByRoom(String room) {
        return findAll().stream().filter(b -> b.room().equals(room)).toList();
    }

    @Override
    public Optional<Booking> findById(int id) {
        return findAll().stream().filter(b -> b.id() == id).findFirst();
    }

    @Override
    public int nextId() {
        return findAll().stream().mapToInt(Booking::id).max().orElse(0) + 1;
    }

    @Override
    public void save(Booking b) {
        try {
            Files.writeString(file, format(b) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            Files.write(file, findAll().stream().filter(b -> b.id() != id).map(CsvBookingGateway::format).toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String format(Booking b) {
        return b.id() + ";" + b.room() + ";" + b.start() + ";" + b.end() + ";"
                + b.who() + ";" + b.attendees() + ";" + b.projector();
    }

    private static Booking parse(String line) {
        String[] f = line.split(";");
        return new Booking(Integer.parseInt(f[0]), f[1], LocalDateTime.parse(f[2]), LocalDateTime.parse(f[3]),
                f[4], Integer.parseInt(f[5]), Boolean.parseBoolean(f[6]));
    }
}
