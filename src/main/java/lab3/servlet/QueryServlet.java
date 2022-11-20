package lab3.servlet;

import lab3.repository.products.Product;
import lab3.service.products.ProductService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static lab3.html.HTMLGenerator.body;
import static lab3.html.HTMLGenerator.br;
import static lab3.html.HTMLGenerator.cover;
import static lab3.html.HTMLGenerator.html;
import static lab3.html.HTMLGenerator.lines;
import static lab3.html.HTMLGenerator.writeHTML;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private final ProductService productService;

    public QueryServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        try {
            if ("max".equals(command)) {
                Optional<Product> result = productService.getProductWithMaxPrice();

                writeHTML(
                    response,
                    html(
                        body(
                            lines(
                                cover("h1", "Product with max price: "),
                                result
                                    .map(product -> br(product.getName() + "\t" + product.getPrice()))
                                    .orElse("")
                            )
                        )
                    )
                );
            } else if ("min".equals(command)) {
                Optional<Product> result = productService.getProductWithMinPrice();

                writeHTML(
                    response,
                    html(
                        body(
                            lines(
                                cover("h1", "Product with min price: "),
                                result
                                    .map(product -> br(product.getName() + "\t" + product.getPrice()))
                                    .orElse("")
                            )
                        )
                    )
                );
            } else if ("sum".equals(command)) {
                writeHTML(
                    response,
                    html(
                        body(
                            lines(
                                "Summary price: ",
                                Long.toString(productService.getSumPriceOfProducts())
                            )
                        )
                    )
                );
            } else if ("count".equals(command)) {
                writeHTML(
                    response,
                    html(
                        body(
                            lines(
                                "Number of products: ",
                                Integer.toString(productService.getNumberOfProducts())
                            )
                        )
                    )
                );
            } else {
                writeHTML(response, "Unknown command: " + command);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
