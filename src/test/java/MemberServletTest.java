import com.fasterxml.jackson.databind.ObjectMapper;
import contracts.members.MemberRequest;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DbService;
import persistence.entities.Member;
import servlet.MemberServlet;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MemberServletTest {

    private MemberServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private DbService dbService;
    private MockedStatic<DbService> mockedDbService;
    private StringWriter stringWriter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new MemberServlet();
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
    void doGet_AllMembers() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/members");
        when(dbService.getAllMembers()).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(dbService).getAllMembers();
    }

    @Test
    void doGet_SingleMember_Found() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/members/1");
        when(dbService.getMemberById(1)).thenReturn(new Member(1, "John", "john@test.com", Date.valueOf(LocalDate.now())));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPost_Success() throws Exception {
        MemberRequest reqBody = new MemberRequest();
        reqBody.setName("John");
        reqBody.setEmail("john@test.com");
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(dbService.getMemberByEmail("john@test.com")).thenReturn(null);
        when(dbService.addMember(any(Member.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    void doPost_InvalidEmail() throws Exception {
        MemberRequest reqBody = new MemberRequest();
        reqBody.setName("John");
        reqBody.setEmail("invalid-email");
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_UNPROCESSABLE_CONTENT), anyString());
    }

    @Test
    void doDelete_Success() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/members/1");
        when(dbService.deleteMember(1)).thenReturn(true);

        servlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doPut_Success() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/members/1");
        MemberRequest reqBody = new MemberRequest();
        reqBody.setName("John Updated");
        reqBody.setEmail("john@test.com");
        String json = objectMapper.writeValueAsString(reqBody);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(dbService.getMemberByEmail("john@test.com", 1)).thenReturn(null);
        when(dbService.updateMember(eq(1), any(MemberRequest.class))).thenReturn(true);

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}