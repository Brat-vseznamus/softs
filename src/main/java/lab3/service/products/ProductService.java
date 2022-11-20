package lab3.service.products;

import lab3.repository.Table;
import lab3.repository.products.Product;
import lab3.repository.products.ProductTable;
import lab3.service.ServiceConfig;

import java.util.List;
import java.util.Optional;

public class ProductService {
    private final Table<Product> productTable;

    public ProductService(ServiceConfig serviceConfig) {
        this.productTable = new ProductTable(serviceConfig.getDatabaseUrl());

        productTable.create();
    }

    public void addProduct(String name, long price) {
        productTable.insert(
                List.of(Product.NAME, Product.PRICE),
                List.of(new Product(0, name, price))
        );
    }

    public List<Product> getAllProducts() {
        return productTable.select();
    }

    public Optional<Product> getProductWithMaxPrice() {
        List<Product> products = productTable.select(
                null,
                List.of(Product.PRICE),
                false,
                1
        );

        if (products.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(products.get(0));
        }
    }

    public Optional<Product> getProductWithMinPrice() {
        List<Product> products = productTable.select(
                null,
                List.of(Product.PRICE),
                true,
                1
        );

        if (products.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(products.get(0));
        }
    }

    public int getNumberOfProducts() {
        List<Product> products = getAllProducts();
        return products.size();
    }

    public long getSumPriceOfProducts() {
        List<Product> products = getAllProducts();
        return products.stream().map(Product::getPrice).reduce(0L, Long::sum);
    }

}
