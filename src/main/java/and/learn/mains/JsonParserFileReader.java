package and.learn.mains;

import and.learn.dto.Persona;
import and.learn.util.FileReader;
import and.learn.util.json.JsonObjectConverter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Classe main che mostra le funzionalità di lettura file e
 * convesione da json a string e viceversa
 * */
@Log4j2
public class JsonParserFileReader {

    public static void main(String[] a) throws IOException {

        log.info("Stampa del file");
        log.info(FileReader.readFileAsString("txt/File.txt"));

        //cro oggetto che userò per le conversioni in json e viceversa
        Persona p = Persona.builder()
                .nome("Eterna")
                .cognome("Levanda")
                .eta(24)
                .sposato(false)
                .dataDiNascita(LocalDate.of(2000,1,1))
                .dataCensimento(LocalDateTime.now())
                .dataCreazioneOggetto(new Date())
                .build();

        log.info("Stampa di un oggetto in json");
        String json = JsonObjectConverter.fromObjectToJsonPretty(p);
        log.info(json);

        log.info("Stampa di una lista di oggetti in json");
        List<Persona> list = Arrays.asList(p, p);
        String jsonList = JsonObjectConverter.fromListToJsonPretty(list, Persona.class);
        log.info(jsonList);

        log.info("Conversione di un json in oggetto e sua stampa");
        p = JsonObjectConverter.fromJsonToObject(json, Persona.class);
        System.out.println(p);

        log.info("Conversione di un json list in un array di oggetti");
        System.out.println(JsonObjectConverter.fromJsonToObjectList(jsonList, Persona.class));
    }
}