package lab3.servlet;

import lab3.service.products.ProductService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static lab3.html.HTMLGenerator.writeHTML;

/**
 * @author akirakozov
 */
public class AddProductServlet extends HttpServlet {
    private final ProductService productService;

    public AddProductServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        productService.addProduct(
                request.getParameter("name"),
                Long.parseLong(request.getParameter("price"))
        );

        writeHTML(response, "OK");
    }


}
