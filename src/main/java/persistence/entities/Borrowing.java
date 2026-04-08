package persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Borrowing {
    public Borrowing(String book_code, int member_id, Date borrow_date, Date return_date) {
        this.book_code = book_code;
        this.member_id = member_id;
        this.borrow_date = borrow_date;
        this.return_date = return_date;
    }

    private int id;
    private String book_code;
    private int member_id;
    private Date borrow_date;
    private Date return_date;
}
