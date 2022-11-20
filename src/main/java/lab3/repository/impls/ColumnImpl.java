package lab3.repository.impls;

import lab3.repository.Column;
import lab3.repository.Entity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ColumnImpl<E extends Entity, T> implements Column<E, T> {
    private String name;
    private Function<E, T> getter;
    private BiConsumer<E, T> setter;
    private Function<T, String> sqlPrinter;
    private Function<String, T> valueParser;

    public ColumnImpl(
            String name,
            Function<E, T> getter,
            BiConsumer<E, T> setter,
            Function<T, String> sqlPrinter,
            Function<String, T> valueParser
    ) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
        this.sqlPrinter = sqlPrinter;
        this.valueParser = valueParser;
    }

    @Override
    public T getValue(E entity) {
        return getter.apply(entity);
    }

    @Override
    public String getStringValue(E entity) {
        return sqlPrinter.apply(getValue(entity));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setValue(E entity, T value) {
        setter.accept(entity, value);
    }

    @Override
    public void setStringValue(E entity, String value) {
        setValue(entity, valueParser.apply(value));
    }
}
