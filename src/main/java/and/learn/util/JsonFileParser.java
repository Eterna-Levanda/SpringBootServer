package and.learn.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe di utilità che offre funzioni di:
 * <ul>
 * <li>lettura di file come stringa</li>
 * <li>creazione oggetto da file name contenente un json</li>
 * <li>creazione lista di oggetti da file name contenente un json</li>
 * </ul>
 */
public class JsonFileParser {

    private JsonFileParser() {}

    public static void main(String[] a) throws IOException {
        System.out.println(JsonFileParser.readFileAsString("txt/File.txt"));
    }

    /**
     * Restituisce una stringa con il contenuto del file indicato nel filePath.
     * Il metodo usa il class loader per trovare il file,
     * perciò il filePath deve essere passato a partire dalla cartella resources
     * */
    public static String readFileAsString(String filePath) throws IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        FileInputStream fis = new FileInputStream(url.getFile());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            //elimino l'ultimo line sepatator aggiunto all'ultimo ciclo
            if (!sb.isEmpty()) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }

    }

    /**
     * Legge un file JSON dalle risorse del classpath in UTF-8 e lo converte in un oggetto del tipo specificato.
     */
    public static <T> T readJsonResourceUTF8(final String resourceName, final Class<T> clazz) throws IOException {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + resourceName);
            }
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(content, clazz);
        }
    }

    /**
     * Legge un file JSON dalle risorse del classpath in UTF-8 e lo converte in una lista di oggetti del tipo specificato.
     */
    public static <T> List<T> readJsonResourceUTF8ForList(final String resourceName, final Class<T> clazz) throws IOException {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + resourceName);
            }
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Type listType = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(content, listType);
        }
    }

    //Classi interne per gestione date
    protected static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter jsonWriter, LocalDate date) throws IOException {
            if (date == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }

        @Override
        public LocalDate read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            } else {
                return LocalDate.parse(jsonReader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        }
    }

    protected static class LocalDateTimeAdapter implements JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(dateTime.format(FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        }
    }

}
