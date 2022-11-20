package lab3.repository.impls;

import lab3.repository.Column;
import lab3.repository.ColumnsMapper;
import lab3.repository.Condition;
import lab3.repository.Table;
import lab3.repository.Entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractTableImpl<E extends Entity> implements Table<E> {
    private final String url;
    private final String name;
    private final Supplier<E> defaultEntityFactory;

    public AbstractTableImpl(String name, String url, Supplier<E> defaultEntityFactory) {
        this.name = name;
        this.url = url;
        this.defaultEntityFactory = defaultEntityFactory;
    }

    protected abstract String createSqlQuery();

    @Override
    public void create() {
        connectToDatabase(c -> {
            String sql = createSqlQuery();
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();

            return null;
        });
    }

    @Override
    public List<E> select() {
        return select(null);
    }

    @Override
    public List<E> select(Condition<E> condition) {
        return select(condition, null);
    }

    @Override
    public List<E> select(Condition<E> condition, Integer limit) {
        return select(condition, null, true, null);
    }

    @Override
    public List<E> select(Condition<E> condition, List<Column<E, ?>> orderBy, boolean asc, Integer limit) {
        return select((x) -> x, condition, orderBy, asc, limit);
    }

    @Override
    public <E2 extends Entity> List<E2> select(
            ColumnsMapper<E, E2> columnsMapper,
            Condition<E> condition,
            List<Column<E, ?>> orderBy,
            boolean asc,
            Integer limit) {
        return connectToDatabase(c -> {
            String sqlQuery = createSelectQuery(condition, orderBy, asc, limit);
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);

            List<E> result = new ArrayList<>();

            while (rs.next()) {
                E entity = defaultEntityFactory.get();
                for (Column<E, ?> column : allColumns()) {
                    String value = rs.getString(column.getName());
                    column.setStringValue(entity, value);
                }
                result.add(entity);
            }

            rs.close();
            stmt.close();

            return result.stream().map(columnsMapper::map).toList();
        });
    }

    private String createSelectQuery(
            Condition<E> condition,
            List<Column<E, ?>> orderBy,
            boolean asc,
            Integer limit) {

        String query = "SELECT ";

        query += allColumns()
                .stream()
                .map(Column::getName)
                .collect(Collectors.joining(", "));

        query += " FROM " + name;

        if (condition != null) {
            query += " WHERE " + condition.sqlForm();
        }

        if (orderBy != null) {
            assert orderBy.size() >= 1;

            query += " ORDER BY ";
            query += orderBy.stream()
                    .map(Column::getName)
                    .collect(Collectors.joining(", "));

            if (asc) {
                query += " ASC ";
            } else {
                query += " DESC ";
            }
        }

        if (limit != null) {
            query += " LIMIT " + limit;
        }

        return query;
    }

    @Override
    public boolean insert(List<Column<E, ?>> columns, List<E> entities) {
        return connectToDatabase(c -> {
            String sqlQuery = createInsertQuery(columns, entities);
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sqlQuery);
            stmt.close();

            return true;
        });
    }

    private String createInsertQuery(List<Column<E,?>> columns, List<E> entities) {
        assert entities.size() > 0;

        String query = "INSERT INTO " + name + " ";

        query += "(" +
                columns.stream()
                        .map(Column::getName)
                        .collect(Collectors.joining(", "))
                + ")";

        query += " VALUES ";

        query += entities.stream()
                .map(entity ->
                        "(" + columns.stream()
                                .map(column -> column.getStringValue(entity))
                                .collect(Collectors.joining(", "))
                        + ")"
                )
                .collect(Collectors.joining(", "));

        return query;
    }

    private <R> R connectToDatabase(ConnectionHandler<R> handler) {
        try {
            try (Connection c = DriverManager.getConnection(url)) {
                return handler.handle(c);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface ConnectionHandler<R> {
        R handle(Connection connection) throws SQLException;
    }
}
