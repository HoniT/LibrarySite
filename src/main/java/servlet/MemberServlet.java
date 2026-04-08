package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import contracts.members.MemberRequest;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.DbService;
import persistence.entities.Member;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemberServlet extends HttpServlet {
    private DbService _db;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        _db = DbService.getInstance();
    }

    private static final Pattern ENDPOINT_ID_PATTERN = Pattern.compile("^/api/\\w+/(-?\\d+)$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<Integer> memberId = extractId(req);
        if(memberId.isPresent()) {
            Member member = _db.getMemberById(memberId.get());
            if(member == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Member with id \"" + memberId + "\" not found");
                return;
            }

            objectMapper.writeValue(resp.getWriter(), member);
        }
        else {
            List<Member> members = _db.getAllMembers();
            objectMapper.writeValue(resp.getWriter(), members);
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MemberRequest memberRequest;
        try {
            memberRequest = objectMapper.readValue(req.getReader(), MemberRequest.class);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
            return;
        }

        // Validating payload
        if(memberRequest.getName() == null || memberRequest.getName().isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Member name is required");
            return;
        }
        if(memberRequest.getEmail() == null || memberRequest.getEmail().isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Member email is required");
            return;
        }
        if(!EMAIL_PATTERN.matcher(memberRequest.getEmail()).find()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Invalid email format provided");
            return;
        }

        // Checking for member with same email
        var existingMember = _db.getMemberByEmail(memberRequest.getEmail());
        if(existingMember != null) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Member with this email already exists");
            return;
        }

        Member member = new Member(new Random().nextInt(), memberRequest.getName(), memberRequest.getEmail(), Date.valueOf(LocalDate.now()));

        boolean dbResult = _db.addMember(member);
        if(!dbResult) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't add to database");
            return;
        }

        objectMapper.writeValue(resp.getWriter(), member);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var id = extractId(req);
        if(id.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No id provided");
            return;
        }

        if(_db.hasActiveBorrowing(id.get())) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Cannot delete member. They have an active borrow.");
            return;
        }

        boolean dbResult = _db.deleteMember(id.get());
        if(!dbResult) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Member not found");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var id = extractId(req);
        if(id.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No id provided");
            return;
        }

        MemberRequest memberRequest;
        try {
            memberRequest = objectMapper.readValue(req.getReader(), MemberRequest.class);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
            return;
        }

        // Validating payload
        if(memberRequest.getName() == null || memberRequest.getName().isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Member name is required");
            return;
        }
        if(memberRequest.getEmail() == null || memberRequest.getEmail().isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Member email is required");
            return;
        }

        if(!EMAIL_PATTERN.matcher(memberRequest.getEmail()).find()) {
            resp.sendError(HttpServletResponse.SC_UNPROCESSABLE_CONTENT, "Invalid email format provided");
            return;
        }
        // Checking for member with same email
        var existingMember = _db.getMemberByEmail(memberRequest.getEmail(), id.get());
        if(existingMember != null) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Member with this email already exists");
            return;
        }

        boolean dbResult = _db.updateMember(id.get(), memberRequest);
        if(!dbResult) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Member not found");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    // === Helpers ===

    /// Extracts id from endpoint uri if any
    private Optional<Integer> extractId(HttpServletRequest req) {
        try {
            Matcher matcher = ENDPOINT_ID_PATTERN.matcher(req.getRequestURI());
            if(!matcher.find()) return Optional.empty();

            int id = Integer.parseInt(matcher.group(1));
            return Optional.of(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}