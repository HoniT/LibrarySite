package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import contracts.borrowings.BorrowRequest;
import contracts.borrowings.ReturnRequest;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.DbService;
import persistence.entities.Borrowing;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class BorrowingServlet extends HttpServlet {
    private DbService _db;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        _db = DbService.getInstance();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Borrowing> borrowings = _db.getAllBorrowings();
        objectMapper.writeValue(resp.getWriter(), borrowings);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if(path == null || path.isBlank() || path.equals("/")) {
            BorrowRequest request;
            try {
                request = objectMapper.readValue(req.getReader(), BorrowRequest.class);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
                return;
            }

            // Validating payload
            if(request.getBookCode() == null || request.getBookCode().isBlank()) {
                resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Book code is required");
                return;
            }

            // Checking if the book is already borrowed
            if(_db.getActiveBorrowing(request.getBookCode()) != null) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Book is already borrowed");
                return;
            }

            Borrowing borrowing = new Borrowing(request.getBookCode(), request.getMemberId(), Date.valueOf(LocalDate.now()), null);
            boolean dbResult = _db.addBorrowing(borrowing);
            if(!dbResult) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't add to database");
                return;
            }
        }
        else if(path.equals("/return")) {
            ReturnRequest request;
            try {
                request = objectMapper.readValue(req.getReader(), ReturnRequest.class);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
                return;
            }

            // Validating payload
            if(request.getBookCode() == null || request.getBookCode().isBlank()) {
                resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Book code is required");
                return;
            }

            // Checking if the book is not borrowed
            Borrowing borrowing = _db.getActiveBorrowing(request.getBookCode());
            if(borrowing == null) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Book is not borrowed. Can't return");
                return;
            }

            boolean dbResult = _db.returnBorrowing(borrowing);
            if(!dbResult) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't update data");
                return;
            }
        }
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
