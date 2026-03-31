package servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.DbService;

public class BorrowingServlet extends HttpServlet {
    private DbService _db;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        _db = DbService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

    }
}
