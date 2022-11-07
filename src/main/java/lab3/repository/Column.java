package lab3.repository;

public interface Column<E extends Entity, T> {
    T      getValue(E entity);
    String getStringValue(E entity);
    String getName();

    void   setValue(E entity, T value);
    void   setStringValue(E entity, String value);
}
