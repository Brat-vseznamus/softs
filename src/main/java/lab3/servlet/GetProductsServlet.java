package lab3.servlet;

import lab3.html.HTMLGenerator;
import lab3.repository.products.Product;
import lab3.service.products.ProductService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static lab3.html.HTMLGenerator.body;
import static lab3.html.HTMLGenerator.br;
import static lab3.html.HTMLGenerator.html;
import static lab3.html.HTMLGenerator.lines;
import static lab3.html.HTMLGenerator.writeHTML;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {
    private final ProductService productService;

    public GetProductsServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        writeHTML(
            response,
            html(
                body(
                    lines(
                        productService.getAllProducts()
                            .stream()
                            .map(product -> br(product.getName() + "\t" + product.getPrice()))
                            .toList()
                            .toArray(new String[0])
                    )
                )
            )
        );
    }
}
