package ua.edu.booking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Порти й адаптери: залежності в коді йдуть від адаптерів до ядра, ніколи навпаки. */
class ArchitectureTest {

    static final String P = "ua.edu.booking.";

    @Test
    void domainDependsOnNothing() throws IOException {
        assertEquals(List.of(), violations("domain", P + "application", P + "adapter"));
    }

    @Test
    void applicationDoesNotKnowAdapters() throws IOException {
        assertEquals(List.of(), violations("application", P + "adapter"));
    }

    @Test
    void drivingAdaptersUseOnlyInputPorts() throws IOException {
        assertEquals(List.of(), violations("adapter/in", P + "application.port.out", P + "adapter.out",
                P + "application.BookingService", P + "application.RoomQueryService"));
    }

    @Test
    void drivenAdaptersDoNotKnowDrivingOnes() throws IOException {
        assertEquals(List.of(), violations("adapter/out", P + "adapter.in", P + "application.port.in"));
    }

    static List<String> violations(String pkg, String... forbidden) throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/ua/edu/booking", pkg.split("/")))) {
            return files.filter(f -> f.toString().endsWith(".java"))
                    .flatMap(f -> lines(f).stream()
                            .filter(l -> l.startsWith("import "))
                            .filter(l -> Stream.of(forbidden).anyMatch(x -> l.startsWith("import " + x)))
                            .map(l -> f.getFileName() + ": " + l))
                    .toList();
        }
    }

    static List<String> lines(Path p) {
        try {
            return Files.readAllLines(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
