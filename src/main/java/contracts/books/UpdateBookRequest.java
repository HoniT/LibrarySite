package contracts.books;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateBookRequest {
    private String title;
    private String author;

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<String> getAuthor() {
        return Optional.ofNullable(author);
    }
}
