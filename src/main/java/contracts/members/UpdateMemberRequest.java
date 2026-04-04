package contracts.members;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateMemberRequest {
    private String name;
    private String email;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }
}
