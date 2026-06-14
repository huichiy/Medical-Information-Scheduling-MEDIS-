package hms.exception;

/**
 * Thrown when an appointment is booked for a doctor at a date+time that is
 * already taken. Supports the Module 2 requirement: "Prevent duplicate time
 * slots". A custom checked exception keeps the business rule explicit and lets
 * the UI react with a clear message.
 */
public class DuplicateSlotException extends Exception {

    public DuplicateSlotException(String message) {
        super(message);
    }
}
