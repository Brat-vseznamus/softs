package lab3.repository.products;

import lab3.repository.Column;
import lab3.repository.impls.AbstractTableImpl;

import java.util.List;

public class ProductTable extends AbstractTableImpl<Product> {

    public ProductTable(String url) {
        super("PRODUCT", url, Product::new);
    }

    @Override
    public String createSqlQuery() {
        return "CREATE TABLE IF NOT EXISTS PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)";
    }

    @Override
    public List<Column<Product, ?>> allColumns() {
        return Product.allColumns();
    }
}
