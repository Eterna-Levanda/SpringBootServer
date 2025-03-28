package and.learn.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Classe che serve a leggere e mappare nelle sue variabili le propery figlie di serie-prop
 * */
@ConfigurationProperties(prefix = "serie-prop")
@Component
@Data
public class PropertyReader {
    private String prop11;
    /*Notare che SpringBoot fa il mapping guardando il nome della variabile, non della classe,
     anche se sarebbe bene mantenere la stessa nomenclatura tra classe, variabile e property.
     In realtà non c'è da stupirsi se il nome della classe non ha importanza,
     succede lo stesso con le variabili di tipo primitivo, mica la properties deve chiamarsi String!
     */
    private ClasseNomeACaso prop12;
    private Double prop2;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate data;

    @Data
    public static class ClasseNomeACaso {
        private Integer prop121;
    }
}
