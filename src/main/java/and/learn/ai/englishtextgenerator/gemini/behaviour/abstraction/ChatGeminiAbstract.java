package and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public abstract class ChatGeminiAbstract {

    protected static String geminiModel;

    protected float temperature;
    protected String apiKey;
    protected int totalTokens;
    /*
       gemini-pro-latest to gemini-3-pro-preview
        gemini-flash-latest to gemini-3-flash-preview
 */

    private static final List<String> modelli = new ArrayList<>(Arrays.asList(

            "gemini-3-flash-preview"// USABILE con THINKING di livello MEDIUM e HIGH
            //"gemini-3.1-pro-preview",// NON HA IL THINKING di livello MEDIUM

            /*
            "gemini-flash-latest",//NON HA IL THINKING!
            "gemini-2.5-flash-lite",//NON HA IL THINKING!
            "gemini-2.5-flash",//NON HA IL THINKING!
            "gemini-3-flash", //gemini-3-flash is not found for API version v1beta
            "gemini-2.5-pro",// NON HA IL THINKING!
            */
    ));

    protected ChatGeminiAbstract(String apiKey) {
        this.apiKey = apiKey;
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
