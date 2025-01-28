package and.learn.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class Esterno {
    @Valid
    private Interno interno;
}
