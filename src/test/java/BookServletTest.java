import com.fasterxml.jackson.databind.ObjectMapper;
import contracts.books.BookRequest;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DbService;
import persistence.entities.Book;
import servlet.BookServlet;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BookServletTest {

    private BookServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private DbService dbService;
    private MockedStatic<DbService> mockedDbService;
    private StringWriter stringWriter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BookServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dbService = mock(DbService.class);

        mockedDbService = mockStatic(DbService.class);
        mockedDbService.when(DbService::getInstance).thenReturn(dbService);

        ServletConfig config = mock(ServletConfig.class);
        servlet.init(config);

        stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        mockedDbService.close();
    }

    @Test
    void doGet_AllBooks() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/books");
        when(dbService.getAllBooks()).thenReturn(Collections.singletonList(new Book("B1", "Title", "Author")));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(dbService).getAllBooks();
    }

    @Test
    void doGet_SingleBook_Found() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/books/B1");
        when(dbService.getBookByCode("B1")).thenReturn(new Book("B1", "Title", "Author"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doGet_SingleBook_NotFound() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/books/B1");
        when(dbService.getBookByCode("B1")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    }

    @Test
    void doPost_Success() throws Exception {
        BookRequest reqBody = new BookRequest();
        reqBody.setCode("B1");
        reqBody.setTitle("New Book");
        reqBody.setAuthor("Author");
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(dbService.getBookByCode("B1")).thenReturn(null);
        when(dbService.addBook(any(Book.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    void doDelete_Success() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/books/B1");
        when(dbService.deleteBook("B1")).thenReturn(true);

        servlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doPut_Success() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/books/B1");
        BookRequest reqBody = new BookRequest();
        reqBody.setCode("B1");
        reqBody.setTitle("Updated");
        reqBody.setAuthor("Author");
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(dbService.getBookByCode("B1")).thenReturn(null);
        when(dbService.updateBook(eq("B1"), any(BookRequest.class))).thenReturn(true);

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}