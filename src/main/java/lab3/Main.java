package lab3;

import lab3.service.ServiceConfig;
import lab3.service.products.ProductService;
import lab3.servlet.AddProductServlet;
import lab3.servlet.GetProductsServlet;
import lab3.servlet.QueryServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * @author akirakozov
 */
public class Main {
    public static void main(String[] args) throws Exception {
        ProductService productService = new ProductService(new ServiceConfig("jdbc:sqlite:test.db"));

        Server server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet(productService)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(productService)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(productService)),"/query");

        server.start();
        server.join();
    }
}
