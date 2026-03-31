package persistence;

import persistence.entities.Book;
import persistence.entities.Borrowing;
import persistence.entities.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbService {
    private static DbService INSTANCE;
    private Connection jdbcConnection;

    private DbService() {
        try {
            jdbcConnection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/postgres?currentSchema=LibraryDB",
                    "postgres",
                    "8374"
            );
        } catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public static DbService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DbService();
        }
        return INSTANCE;
    }

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
                rs.getString("book_code"),
                rs.getInt("member_id"),
                rs.getDate("borrow_date"),
                rs.getDate("return_date")
        );
    }
}
