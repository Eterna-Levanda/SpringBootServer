package and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.api;

import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiApiAbstract;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static org.springframework.http.MediaType.APPLICATION_PDF;


public class ChatGeminiInLineFilesApi extends ChatGeminiApiAbstract {

    public ChatGeminiInLineFilesApi(String apiKey) {
        super(apiKey);
    }

    /**
     * Chiama Gemini senza effettuare upload dei file
     */
    public String sendMessageWithFiles(String promptText, byte[] file1, byte[] file2) throws IOException {

        GenerateContentResponse response = null;

        // Creazione delle "Parti" della richiesta
        // Gemini accetta una lista di oggetti Part (Testo, Immagini, PDF, etc.)
        List<Part> promptParts = new LinkedList<>();
        promptParts.add(Part.fromText(promptText));

        if(file1 != null) {
            promptParts.add(Part.fromBytes(file1, APPLICATION_PDF.toString()));
        }
        if(file1 != null) {
            promptParts.add(Part.fromBytes(file2, APPLICATION_PDF.toString()));
        }

        // Chiamata al modello
        // Nota: Usiamo una lista di Part invece di una singola stringa
        response = client.models.generateContent(
                geminiModel,
                promptParts.toString(),
                config
        );

        // Conteggio quanti token ho consumato
        totalTokens += response.usageMetadata().get().totalTokenCount().get();

        return response.text();
    }

    public String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) throws IOException {

        // 1. Prepariamo le parti del messaggio corrente (testo + eventuali file)
        List<Part> currentParts = new ArrayList<>();
        currentParts.add(Part.builder().text(promptText).build());

        if (file1 != null && file1.length > 0) {
            currentParts.add(Part.fromBytes(file1, APPLICATION_PDF.toString()));
        }
        if (file2 != null && file2.length > 0) {
            currentParts.add(Part.fromBytes(file2, APPLICATION_PDF.toString()));
        }

        // 2. Creiamo il nuovo contenuto dell'utente e lo aggiungiamo alla history
        Content userContent = Content.builder()
                .role("user")
                .parts(currentParts)
                .build();
        chatHistory.add(userContent);

        try {
            // 3. Chiamata al modello passando TUTTA la history
            // Nota: passiamo 'chatHistory' al posto della singola lista di Part
            GenerateContentResponse response = client.models.generateContent(
                    geminiModel,
                    chatHistory,
                    config
            );

            String modelResponseText = response.text();

            // 4. Fondamentale: aggiungiamo la risposta del modello alla history
            // Se non lo fai, Gemini "dimenticherà" quello che ti ha appena risposto
            Content modelContent = Content.builder()
                    .role("model")
                    .parts(List.of(Part.builder().text(modelResponseText).build()))
                    .build();
            chatHistory.add(modelContent);

            return modelResponseText;

        } catch (Exception e) {
            // Se la chiamata fallisce, potresti voler rimuovere l'ultimo messaggio user
            // per mantenere la history sincronizzata
            chatHistory.remove(chatHistory.size() - 1);
            throw new IOException("Errore durante la conversazione con Gemini", e);
        }
    }
}
