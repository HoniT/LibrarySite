package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import contracts.books.AddBookRequest;
import contracts.books.UpdateBookRequest;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.DbService;
import persistence.entities.Book;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServlet extends HttpServlet {
    private DbService _db;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        _db = DbService.getInstance();
    }

    private static final Pattern ENDPOINT_ID_PATTERN = Pattern.compile("^/\\w+/(\\w+)$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<String> bookCode = extractId(req);
        if(bookCode.isPresent()) {
            Book book = _db.getBookByCode(bookCode.get());
            if(book == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Book with code \"" + bookCode + "\" not found");
                return;
            }

            objectMapper.writeValue(resp.getWriter(), book);
        }
        else {
            List<Book> books = _db.getAllBooks();
            objectMapper.writeValue(resp.getWriter(), books);
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AddBookRequest bookRequest;
        try {
            bookRequest = objectMapper.readValue(req.getReader(), AddBookRequest.class);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
            return;
        }

        // Validating payload
        if(bookRequest.getTitle() == null || bookRequest.getTitle().isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Book title is required");
            return;
        }
        if(bookRequest.getAuthor() == null || bookRequest.getAuthor().isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Book author is required");
            return;
        }

        Book book = new Book(generateBookCode(10), bookRequest.getTitle(), bookRequest.getAuthor());

        boolean dbResult = _db.addBook(book);
        if(!dbResult) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't add to database");
            return;
        }

        objectMapper.writeValue(resp.getWriter(), book);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var code = extractId(req);
        if(code.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No code provided");
            return;
        }

        boolean dbResult = _db.deleteBook(code.get());
        if(!dbResult) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var code = extractId(req);
        if(code.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No code provided");
            return;
        }

        UpdateBookRequest bookRequest;
        try {
            bookRequest = objectMapper.readValue(req.getReader(), UpdateBookRequest.class);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
            return;
        }

        boolean dbResult = _db.updateBook(code.get(), bookRequest);
        if(!dbResult) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    // === Helpers ===

    /// Extracts id from endpoint uri if any
    private Optional<String> extractId(HttpServletRequest req) {
        try {
            Matcher matcher = ENDPOINT_ID_PATTERN.matcher(req.getRequestURI());
            if(!matcher.find()) return Optional.empty();

            String id = matcher.group(1);
            return Optional.of(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    public static String generateBookCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}
