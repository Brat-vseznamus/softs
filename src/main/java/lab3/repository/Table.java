package lab3.repository;

import java.util.List;

public interface Table<E extends Entity> {
    List<Column<E, ?>> allColumns();
    List<E> select();
    List<E> select(Condition<E> condition);
    List<E> select(Condition<E> condition, Integer limit);
    List<E> select(Condition<E> condition, List<Column<E, ?>> orderBy, boolean asc, Integer limit);

    <E2 extends Entity>
    List<E2> select(
            ColumnsMapper<E, E2> columnsMapper,
            Condition<E> condition,
            List<Column<E, ?>> orderBy,
            boolean asc,
            Integer limit
    );

    boolean  insert(List<Column<E, ?>> columns, List<E> entities);

    void     create();
}
