package and.learn.util.json;

import and.learn.util.json.adapter.LocalDateAdapter;
import and.learn.util.json.adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classe di utilità per convertire json in oggetti e viceversa
 * */
public class JsonObjectConverter {

    private JsonObjectConverter() {}

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";

    private static final Gson GSON = new GsonBuilder()
            //il metodo setDateFormat definisce il formato per i campi di tipo Date. Altrimenti usare .registerTypeAdapter(Date.class, new DateAdapter())
            .setDateFormat(DATE_TIME_FORMAT)
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter(YYYY_MM_DD))
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter(DATE_TIME_FORMAT))
            .create();

    private static final Gson GSON_PRETTY = new GsonBuilder()
            .setPrettyPrinting()
            //il metodo setDateFormat definisce il formato per i campi di tipo Date. Altrimenti usare .registerTypeAdapter(Date.class, new DateAdapter())
            .setDateFormat(DATE_TIME_FORMAT)
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter(YYYY_MM_DD))
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter(DATE_TIME_FORMAT))
            .create();

    //METODI DI CONVERSIONE DA OGGETTO A STRINGA

    /**Converte un oggetto in una stringa in formato json*/
    public static <T> String fromObjectToJson(T obj) {
        return GSON.toJson(obj);
    }

    /**Converte un oggetto in una stringa in formato json formattato*/
    public static <T> String fromObjectToJsonPretty(T obj) {
        return GSON_PRETTY.toJson(obj);
    }

    /**
     * Restitusice il json string di una lista di oggetti
     */
    public static <T> String fromListToJson(T obj, Type typeOfT) {
        return GSON.toJson(obj, typeOfT);
    }

    /**Funziona come fromListToJson ma restituisce una stringa formattata*/
    public static <T> String fromListToJsonPretty(List<T> list, Class<T> typeOfT) {
        Type listType = TypeToken.getParameterized(List.class, typeOfT).getType();
        return GSON_PRETTY.toJson(list, listType);
    }

    //METODI DI CONVERSIONE DA STRINGA A OGGETTI

    /**
     * Legge un file JSON dalle risorse del classpath in UTF-8 e lo converte in un oggetto del tipo specificato.
     */
    public static <T> T fromJsonToObject(String json, final Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Legge un file JSON dalle risorse del classpath in UTF-8 e lo converte in una lista di oggetti del tipo specificato.
     */
    public static <T> List<T> fromJsonToObjectList(final String json, final Class<T> clazz) {
        Type listType = TypeToken.getParameterized(List.class, clazz).getType();
        return GSON.fromJson(json, listType);
    }
}
