import com.fasterxml.jackson.databind.ObjectMapper;
import contracts.borrowings.BorrowRequest;
import contracts.borrowings.ReturnRequest;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DbService;
import persistence.entities.Borrowing;
import servlet.BorrowingServlet;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class BorrowingServletTest {

    private BorrowingServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private DbService dbService;
    private MockedStatic<DbService> mockedDbService;
    private StringWriter stringWriter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BorrowingServlet();
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
    void doGet_AllBorrowings() throws Exception {
        when(dbService.getAllBorrowings()).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(dbService).getAllBorrowings();
    }

    @Test
    void doPost_Borrow_Success() throws Exception {
        when(request.getPathInfo()).thenReturn("/");
        BorrowRequest reqBody = new BorrowRequest();
        reqBody.setBookCode("B1");
        reqBody.setMemberId(1);
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(dbService.getActiveBorrowing("B1")).thenReturn(null);
        when(dbService.addBorrowing(any(Borrowing.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doPost_Borrow_Conflict() throws Exception {
        when(request.getPathInfo()).thenReturn("/");
        BorrowRequest reqBody = new BorrowRequest();
        reqBody.setBookCode("B1");
        reqBody.setMemberId(1);
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(dbService.getActiveBorrowing("B1")).thenReturn(new Borrowing("B1", 1, Date.valueOf(LocalDate.now()), null));

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_CONFLICT), anyString());
    }

    @Test
    void doPost_Return_Success() throws Exception {
        when(request.getPathInfo()).thenReturn("/return");
        ReturnRequest reqBody = new ReturnRequest();
        reqBody.setBookCode("B1");
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        Borrowing activeBorrowing = new Borrowing("B1", 1, Date.valueOf(LocalDate.now()), null);
        when(dbService.getActiveBorrowing("B1")).thenReturn(activeBorrowing);
        when(dbService.returnBorrowing(activeBorrowing)).thenReturn(true);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}