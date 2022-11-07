package lab3.servlet;

import lab3.repository.products.Product;
import lab3.service.products.ProductService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

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

        if ("max".equals(command)) {
            try {
                response.getWriter().println("<html><body>");
                response.getWriter().println("<h1>Product with max price: </h1>");

                Optional<Product> result = productService.getProductWithMaxPrice();

                result.ifPresent(p -> {
                    String name = p.getName();
                    long price = p.getPrice();
                    try {
                        response.getWriter().println(name + "\t" + price + "</br>");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                response.getWriter().println("</body></html>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if ("min".equals(command)) {
            try {
                response.getWriter().println("<html><body>");
                response.getWriter().println("<h1>Product with min price: </h1>");

                Optional<Product> result = productService.getProductWithMinPrice();

                result.ifPresent(p -> {
                    String name = p.getName();
                    long price = p.getPrice();
                    try {
                        response.getWriter().println(name + "\t" + price + "</br>");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                response.getWriter().println("</body></html>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if ("sum".equals(command)) {
            try {
                response.getWriter().println("<html><body>");
                response.getWriter().println("Summary price: ");
                response.getWriter().println(productService.getSumPriceOfProducts());
                response.getWriter().println("</body></html>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if ("count".equals(command)) {
            try {
                response.getWriter().println("<html><body>");
                response.getWriter().println("Number of products: ");
                response.getWriter().println(productService.getNumberOfProducts());
                response.getWriter().println("</body></html>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            response.getWriter().println("Unknown command: " + command);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
