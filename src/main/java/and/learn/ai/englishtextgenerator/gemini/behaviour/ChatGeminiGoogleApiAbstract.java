package and.learn.ai.englishtextgenerator.gemini.behaviour;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.*;

public abstract class ChatGeminiGoogleApiAbstract implements ChatGeminiInterface {

    protected final Client client;
    protected List<Content> chatHistory;
    protected GenerateContentConfig config;

    protected ChatGeminiGoogleApiAbstract() {
        client = Client.builder().apiKey(API_KEY).build();
        chatHistory = new ArrayList<>();
        config = GenerateContentConfig.builder()
                .temperature(TEMPERATURE)
                .build();
    }

    @Override
    public abstract String sendMessageWithFiles(String promptText, byte[] file1, byte[] file2) throws IOException;

    @Override
    public abstract String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) throws IOException;

    @Override
    public String sendMessageUsingMemory(String promptText) throws IOException {
        try {
            // 1. Creiamo il contenuto dell'utente dal promptText
            Content userMessage = Content.builder()
                    .role("user")
                    .parts(List.of(Part.builder().text(promptText).build()))
                    .build();

            // 2. Lo aggiungiamo alla storia locale
            chatHistory.add(userMessage);

            // 3. Inviamo l'INTERA storia al modello
            // L'SDK riconosce che passando una lista di Content si tratta di una chat
            GenerateContentResponse response = client.models.generateContent(
                    GEMINI_VERSION,
                    chatHistory,
                    config
            );

            // 4. Estraiamo il testo della risposta
            String botResponseText = response.text();

            // 5. Salviamo la risposta del modello nella storia
            // Senza questo passaggio, al prossimo messaggio Gemini non saprà cosa ha detto prima
            Content modelResponse = Content.builder()
                    .role("model")
                    .parts(List.of(Part.builder().text(botResponseText).build()))
                    .build();
            chatHistory.add(modelResponse);

            return botResponseText;

        } catch (Exception e) {
            // In caso di errore, rimuoviamo l'ultimo messaggio dell'utente
            // per evitare una storia sbilanciata (User senza risposta del Model)
            if (!chatHistory.isEmpty()) {
                chatHistory.remove(chatHistory.size() - 1);
            }
            throw new IOException("Errore durante la generazione della risposta con memoria", e);
        }
    }

    @Override
    public String sendMessage(String promptText) throws IOException {
        GenerateContentResponse response = client.models.generateContent(
                GEMINI_VERSION,
                promptText,
                config
        );

        // Restituiamo il testo generato dal modello
        return response.text();
    }


}
