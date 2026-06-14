package hms.model;

/**
 * Abstract base class for all people in the system.
 *
 * OOP concepts demonstrated:
 *   - ABSTRACTION : declared abstract; defines an abstract method getSummary()
 *                   that every subclass must implement.
 *   - ENCAPSULATION: fields are private, exposed only through getters/setters.
 *   - INHERITANCE  : Patient and Doctor extend this class (User can too —
 *                    coordinate with the team for the shared class diagram).
 */
public abstract class Person {

    private int id;
    private String name;

    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // --- Encapsulation: controlled access to private fields ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Abstraction + Polymorphism: each subclass defines its own summary.
     */
    public abstract String getSummary();

    /**
     * toString() delegates to the overridden getSummary(), so a Patient or
     * Doctor object shows meaningful text directly inside a JComboBox.
     */
    @Override
    public String toString() {
        return getSummary();
    }
}
