package and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction;

import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
public abstract class ChatGeminiAbstract {

    protected static String geminiModel = "gemini-3-flash-preview";

    protected float temperature;
    protected String apiKey;
    protected String modello;

    private static final List<String> modelli = new ArrayList<>(Arrays.asList(
            "gemini-flash-latest",
            "gemini-2.5-flash-lite",// - usato alla grande
            "gemini-2.5-flash",// - usato alla grande
            "gemini-3-flash-preview",// - usato alla grande
            "gemini-3-flash", //mai provato
            "gemini-2.0-flash-lite",// - alla prima richiesta ha fallito
            "gemini-2.0-flash",// - alla prima richiesta ha fallito
            "gemini-2.5-pro",// - alla prima richiesta ha fallito
            "gemini-3-pro-preview",// - alla prima richiesta ha fallito
            "gemini-2.0-flash-thinking-exp-1219"// - in grado di pensare ma consuma token
    ));

    protected ChatGeminiAbstract() {
        temperature = 1;
        geminiModel = modelli.remove(0);
        System.out.println("Primo modello di Gemini: " + geminiModel);
    }

    public abstract String sendMessage(String promptText) throws IOException;

    public abstract String sendMessageWithFiles(String promptText, byte[] file1, byte[] file2) throws IOException;

    public abstract String sendMessageUsingMemory(String promptText) throws IOException;

    public abstract String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) throws IOException;

    public void changeModello() {
        if(!modelli.isEmpty()){
            geminiModel = modelli.remove(0);
            System.out.println("Cambio modello di Gemini. Ora si usa " + geminiModel);
        } else {
            throw new IllegalStateException("Modelli di Gemini terminati");
        }
    }


}
