package ua.edu.booking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Закриті шари: presentation → business → data; model спільна для всіх. */
class ArchitectureTest {

    static final String P = "ua.edu.booking.";

    @Test
    void presentationDoesNotSkipBusiness() throws IOException {
        assertEquals(List.of(), violations("presentation", P + "data"));
    }

    @Test
    void businessDoesNotKnowPresentation() throws IOException {
        assertEquals(List.of(), violations("business", P + "presentation"));
    }

    @Test
    void dataKnowsOnlyModel() throws IOException {
        assertEquals(List.of(), violations("data", P + "business", P + "presentation"));
    }

    @Test
    void modelDependsOnNothing() throws IOException {
        assertEquals(List.of(), violations("model", P + "data", P + "business", P + "presentation"));
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
