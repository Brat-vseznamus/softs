package lab3.repository;

public interface Condition<E extends Entity> {
    String sqlForm();
}
