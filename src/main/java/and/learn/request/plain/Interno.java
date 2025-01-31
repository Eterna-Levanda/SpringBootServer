package and.learn.request.plain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;



@Data
public class Interno {

    @Min(value = 0, message = "Il campo intero dev'essere almeno 0")
    @Max(value = 10, message = "Il campo intero dev'essere al massimo 10")
    private Integer intero;

    @NotNull(message = "Il campo 'string' non può essere nullo")
    private String string;

    private Boolean booleano;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date data;

    @NotNull(message = "Il campo 'date' non può essere nullo")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate localDate;
}
