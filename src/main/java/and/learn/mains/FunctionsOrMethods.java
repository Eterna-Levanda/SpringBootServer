package and.learn.mains;

import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
public class FunctionsOrMethods {

    /*Serve a far notare come ad un metodo puoi passargli:
     sia l'implementazione diretta della funzione (caso 1)
     sia tramite metodo dichiarato nella classe (caso 2)*/
    public static void main(String[] args) {

        Optional<String> prova = Optional.of("prova");

        //implementazione con function
        prova.map(s -> s.toUpperCase())
                //mi serve solo per loggare
                .ifPresent(log::info);

        //implementazione con metodo
        prova.map(s -> getMaiuscolo(s))
                //mi serve solo per loggare
                .ifPresent(log::info);

        //implementazione con metodo 2
        prova.map(FunctionsOrMethods::getMaiuscolo)
                //mi serve solo per loggare
                .ifPresent(log::info);


    }

    private static String getMaiuscolo(String s) {
        return s.toUpperCase();
    }
}
