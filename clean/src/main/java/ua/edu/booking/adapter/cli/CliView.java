package ua.edu.booking.adapter.cli;

/** Куди виводити рядки. Реалізацію дає шар framework (System.out). */
public interface CliView {
    void show(String line);
}
