package and.learn.ai.englishtextgenerator.behaviour.implementation;

import and.learn.ai.englishtextgenerator.behaviour.ChatGeminiGoogleApiAbstract;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.APPLICATION_PDF;
import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.GEMINI_VERSION;

public class ChatGeminiInLineFilesGoogleApi extends ChatGeminiGoogleApiAbstract {

    /**
     * Chiama Gemini senza effettuare upload dei file
     */
    public String sendMessageWithFiles(String promptText, byte[] file1, byte[] file2) throws IOException {

        GenerateContentResponse response = null;

        // Creazione delle "Parti" della richiesta
        // Gemini accetta una lista di oggetti Part (Testo, Immagini, PDF, etc.)
        List<Part> promptParts = Arrays.asList(
                Part.fromText(promptText),
                Part.fromBytes(file1, APPLICATION_PDF),
                Part.fromBytes(file2, APPLICATION_PDF)
        );

        // Chiamata al modello
        // Nota: Usiamo una lista di Part invece di una singola stringa
        response = client.models.generateContent(
                GEMINI_VERSION,
                promptParts.toString(),
                null
        );

        return response.text();
    }

    public String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) throws IOException {

        // 1. Prepariamo le parti del messaggio corrente (testo + eventuali file)
        List<Part> currentParts = new ArrayList<>();
        currentParts.add(Part.builder().text(promptText).build());

        if (file1 != null && file1.length > 0) {
            currentParts.add(Part.fromBytes(file1, APPLICATION_PDF));
        }
        if (file2 != null && file2.length > 0) {
            currentParts.add(Part.fromBytes(file2, APPLICATION_PDF));
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
                    GEMINI_VERSION,
                    chatHistory,
                    null
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
