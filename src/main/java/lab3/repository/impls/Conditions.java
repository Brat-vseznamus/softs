package lab3.repository.impls;

import lab3.repository.Column;
import lab3.repository.Condition;
import lab3.repository.Entity;

import java.util.List;
import java.util.stream.Collectors;

public final class Conditions {
    private Conditions() {

    }

    public static <E extends Entity> Condition<E> AND(List<Condition<E>> conditions) {
        return new Condition<E>() {
            @Override
            public String sqlForm() {
                return conditions.stream().map(Condition::sqlForm).collect(Collectors.joining("AND"));
            }
        };
    }

    public static <E extends Entity, T extends Comparable<T>> Condition<E> EQ(Column<E, T> column, T value) {
        return compare(column, value, "=");
    }

    public static <E extends Entity, T extends Comparable<T>> Condition<E> GT(Column<E, T> column, T value) {
        return compare(column, value, ">");
    }

    public static <E extends Entity, T extends Comparable<T>> Condition<E> LT(Column<E, T> column, T value) {
        return compare(column, value, "<");
    }

    public static <E extends Entity, T extends Comparable<T>> Condition<E> GE(Column<E, T> column, T value) {
        return compare(column, value, ">=");
    }

    public static <E extends Entity, T extends Comparable<T>> Condition<E> LE(Column<E, T> column, T value) {
        return compare(column, value, "<=");
    }

    public static <E extends Entity, T extends Comparable<T>> Condition<E> NE(Column<E, T> column, T value) {
        return compare(column, value, "!=");
    }

    public static <E extends Entity> Condition<E> NOT(Condition<E> condition) {
        return new Condition<E>() {
            @Override
            public String sqlForm() {
                return "NOT " + condition.sqlForm();
            }
        };
    }

    private static <E extends Entity, T extends Comparable<T>>
        Condition<E> compare(Column<E, T> column, T value, String sign) {
        return new Condition<E>() {
            @Override
            public String sqlForm() {
                return column.getName() + " " + sign + " " + value;
            }
        };
    }

}
