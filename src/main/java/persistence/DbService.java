package persistence;

import contracts.books.BookRequest;
import contracts.members.MemberRequest;
import persistence.entities.Book;
import persistence.entities.Borrowing;
import persistence.entities.Member;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DbService {
    private static DbService INSTANCE;
    private final Connection jdbcConnection;

    private DbService() {
        try {
            jdbcConnection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/postgres?currentSchema=\"LibraryDB\"",
                    "postgres",
                    "PASS REMOVED"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database connection ", e);
        }
    }

    public static DbService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DbService();
        }
        return INSTANCE;
    }


    // region Books

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        try (Statement stmt = jdbcConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM \"Books\"")) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    public Book getBookByCode(String code) {
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("SELECT * FROM \"Books\" WHERE code = ?")) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean addBook(Book book) {
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("INSERT INTO \"Books\" (code, title, author) VALUES (?, ?, ?)")) {
            stmt.setString(1, book.getCode());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteBook(String code) {
        if (getActiveBorrowing(code) != null) {
            return false;
        }
        try {
            try (PreparedStatement stmt = jdbcConnection.prepareStatement("DELETE FROM \"Borrowings\" WHERE book_code = ?")) {
                stmt.setString(1, code);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = jdbcConnection.prepareStatement("DELETE FROM \"Books\" WHERE code = ?")) {
                stmt.setString(1, code);
                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateBook(String code, BookRequest book) {
        String sql = "UPDATE \"Books\" SET code = ?, title = ?, author = ? WHERE code = ?";

        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql)) {
            stmt.setString(1, book.getCode());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, code);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // endregion
    // region Members

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        try (Statement stmt = jdbcConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM \"Members\"")) {
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return members;
    }

    public Member getMemberById(int id) {
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("SELECT * FROM \"Members\" WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Member getMemberByEmail(String email) {
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("SELECT * FROM \"Members\" WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Member getMemberByEmail(String email, int notIncludeId) {
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("SELECT * FROM \"Members\" WHERE email = ? AND id != ?")) {
            stmt.setString(1, email);
            stmt.setInt(2, notIncludeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean addMember(Member member) {
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("INSERT INTO \"Members\" (id, name, email, join_date) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, member.getId());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setDate(4, member.getJoin_date());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteMember(int id) {
        if(hasActiveBorrowing(id)) {
            return false;
        }
        try {
            try (PreparedStatement stmt = jdbcConnection.prepareStatement("DELETE FROM \"Borrowings\" WHERE member_id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = jdbcConnection.prepareStatement("DELETE FROM \"Members\" WHERE id = ?")) {
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateMember(int id, MemberRequest member) {
        String sql = "UPDATE \"Members\" SET name = ?, email = ? WHERE id = ?";

        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql)) {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // endregion
    // region Borrowings

    public List<Borrowing> getAllBorrowings() {
        List<Borrowing> borrowings = new ArrayList<>();
        try (Statement stmt = jdbcConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM \"Borrowings\"")) {
            while (rs.next()) {
                borrowings.add(mapResultSetToBorrowing(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return borrowings;
    }

    public Borrowing getActiveBorrowing(String code) {
        String sql = "SELECT * FROM \"Borrowings\" WHERE book_code = ? AND return_date IS NULL";

        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql)) {
            stmt.setString(1, code);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return mapResultSetToBorrowing(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean hasActiveBorrowing(int memberId) {
        String sql = "SELECT 1 FROM \"Borrowings\" WHERE member_id = ? AND return_date IS NULL";

        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean addBorrowing(Borrowing borrowing) {
        String sql = "INSERT INTO \"Borrowings\" (book_code, member_id, borrow_date, return_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql)) {
            stmt.setString(1, borrowing.getBook_code());
            stmt.setInt(2, borrowing.getMember_id());
            stmt.setDate(3, borrowing.getBorrow_date());
            stmt.setDate(4, borrowing.getReturn_date());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean returnBorrowing(Borrowing borrowing) {
        String sql = "UPDATE \"Borrowings\" SET return_date = ? WHERE book_code = ? AND member_id = ? AND borrow_date = ?";

        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setString(2, borrowing.getBook_code());
            stmt.setInt(3, borrowing.getMember_id());
            stmt.setDate(4, borrowing.getBorrow_date());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // endregion

    // === Helpers ===

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getString("code"),
                rs.getString("title"),
                rs.getString("author")
        );
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getDate("join_date")
        );
    }

    private Borrowing mapResultSetToBorrowing(ResultSet rs) throws SQLException {
        return new Borrowing(
                rs.getInt("id"),
                rs.getString("book_code"),
                rs.getInt("member_id"),
                rs.getDate("borrow_date"),
                rs.getDate("return_date")
        );
    }
}