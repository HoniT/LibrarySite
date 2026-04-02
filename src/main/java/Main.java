import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import servlet.BookServlet;
import servlet.BorrowingServlet;
import servlet.MemberServlet;

import java.io.File;

public class Main {
    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("server");
        tomcat.setPort(8080);

        // Set context path and document base
        String contextPath = "";
        String docBase = new File("./src/main/webapp").getAbsolutePath();

        // Create context
        Context context = tomcat.addContext(contextPath, new File(docBase).getAbsolutePath());

        // Add DefaultServlet to handle static files (including index.html)
        Tomcat.addServlet(context, "default", "org.apache.catalina.servlets.DefaultServlet");
        context.addServletMappingDecoded("/", "default");

        // Register servlets
        Tomcat.addServlet(context, "bookServlet", new BookServlet());
        context.addServletMappingDecoded("/books", "bookServlet");
        context.addServletMappingDecoded("/books/*", "bookServlet");

        Tomcat.addServlet(context, "memberServlet", new MemberServlet());
        context.addServletMappingDecoded("/members", "memberServlet");
        context.addServletMappingDecoded("/members/*", "memberServlet");

        Tomcat.addServlet(context, "borrowingServlet", new BorrowingServlet());
        context.addServletMappingDecoded("/borrowings", "borrowingServlet");
        context.addServletMappingDecoded("/borrowings/*", "borrowingServlet");

        tomcat.start();
        tomcat.getConnector();
        tomcat.getServer().await();
    }
}
