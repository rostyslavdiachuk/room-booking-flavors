package ua.edu.booking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Чотири шари Еванса з інвертованою інфраструктурою: domain у центрі, нічого не знає про решту. */
class ArchitectureTest {

    static final String P = "ua.edu.booking.";

    @Test
    void domainDependsOnNothing() throws IOException {
        assertEquals(List.of(), violations("domain", P + "application", P + "infrastructure", P + "ui"));
    }

    @Test
    void applicationDoesNotKnowInfrastructureOrUi() throws IOException {
        assertEquals(List.of(), violations("application", P + "infrastructure", P + "ui"));
    }

    @Test
    void infrastructureDoesNotKnowUi() throws IOException {
        assertEquals(List.of(), violations("infrastructure", P + "ui"));
    }

    @Test
    void uiDoesNotKnowInfrastructure() throws IOException {
        assertEquals(List.of(), violations("ui", P + "infrastructure"));
    }

    static List<String> violations(String pkg, String... forbidden) throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/ua/edu/booking", pkg.split("/")))) {
            return files.filter(f -> f.toString().endsWith(".java"))
                    .flatMap(f -> lines(f).stream()
                            .filter(l -> l.startsWith("import "))
                            .filter(l -> Stream.of(forbidden).anyMatch(x -> l.startsWith("import " + x + ".")))
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
