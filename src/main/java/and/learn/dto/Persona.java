package and.learn.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Builder
@Data
public class Persona {
    private String nome;
    private String cognome;
    private Integer eta;
    private Boolean sposato;
    private LocalDate dataDiNascita;
    private LocalDateTime dataCensimento;
    private Date dataCreazioneOggetto;
}
