package persistence;

import contracts.books.UpdateBookRequest;
import contracts.members.UpdateMemberRequest;
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
                    "jdbc:postgresql://localhost:5432/postgres?currentSchema=\"LibraryDB\"",
                    "postgres",
                    "PASS REMOVED"
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
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("DELETE FROM \"Books\" WHERE code = ?")) {
            stmt.setString(1, code);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateBook(String code, UpdateBookRequest book) {
        if (book.getTitle().isEmpty() && book.getAuthor().isEmpty()) {
            return true;
        }

        StringBuilder sql = new StringBuilder("UPDATE \"Books\" SET ");
        List<Object> parameters = new ArrayList<>();

        if (book.getTitle().isPresent()) {
            sql.append("title = ?, ");
            parameters.add(book.getTitle().get());
        }
        if (book.getAuthor().isPresent()) {
            sql.append("author = ?, ");
            parameters.add(book.getAuthor().get());
        }

        sql.setLength(sql.length() - 2);

        sql.append(" WHERE code = ?");
        parameters.add(code);

        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++)
                stmt.setObject(i + 1, parameters.get(i));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

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
        try (PreparedStatement stmt = jdbcConnection.prepareStatement("DELETE FROM \"Members\" WHERE id = ?")) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateMember(int id, UpdateMemberRequest member) {
        if (member.getName().isEmpty() && member.getEmail().isEmpty()) {
            return true;
        }

        StringBuilder sql = new StringBuilder("UPDATE \"Members\" SET ");
        List<Object> parameters = new ArrayList<>();

        if (member.getName().isPresent()) {
            sql.append("name = ?, ");
            parameters.add(member.getName().get());
        }
        if (member.getEmail().isPresent()) {
            sql.append("email = ?, ");
            parameters.add(member.getEmail().get());
        }

        sql.setLength(sql.length() - 2);

        sql.append(" WHERE id = ?");
        parameters.add(id);

        try (PreparedStatement stmt = jdbcConnection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++)
                stmt.setObject(i + 1, parameters.get(i));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

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
                rs.getString("book_code"),
                rs.getInt("member_id"),
                rs.getDate("borrow_date"),
                rs.getDate("return_date")
        );
    }
}
