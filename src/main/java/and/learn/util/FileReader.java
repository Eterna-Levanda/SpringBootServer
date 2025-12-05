package and.learn.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileReader {

    public FileReader() {
    }

    /**
     * Restituisce una stringa con il contenuto del file indicato nel filePath.
     * Il metodo usa il class loader per trovare il file,
     * perciò il filePath deve essere passato a partire dalla cartella resources
     *
     */
    public static String readFileAsString(String filePath) throws IOException {

        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + filePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
