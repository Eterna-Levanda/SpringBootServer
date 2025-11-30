package and.learn.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Classe di utilità per convertire json in oggetti e viceversa
 * */
public class JsonObjectConverter {
    private static final Gson GSON = new Gson();
    private static final Gson GSON_PRETTY = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private JsonObjectConverter() {}

    /**Converte un oggetto in una stringa in formato json*/
    public static <T> String toJson(T obj) {
        return GSON.toJson(obj);
    }

    /**Converte un oggetto in una stringa in formato json formattato*/
    public static <T> String toJsonPretty(T obj) {
        return GSON_PRETTY.toJson(obj);
    }

    /**
     * Restitusice il json string di una lista di oggetti.
     * Esempio:
     * List<MyClass> list = List.of(new MyClass("A",1), new MyClass("B",2));
     * TypeToken typeToken = new TypeToken<List<MyClass>>();
     * Type listType = typeToken.getType();
     * String jsonList = JsonUtil.toJson(list, listType);
     */
    public static <T> String fromListToJson(T obj, Type typeOfT) {
        return GSON.toJson(obj, typeOfT);
    }

    /**Funziona come fromListToJson ma restituisce una stringa formattata*/
    public static <T> String fromListToJsonPretty(T obj, Type typeOfT) {
        return GSON_PRETTY.toJson(obj, typeOfT);
    }
}
