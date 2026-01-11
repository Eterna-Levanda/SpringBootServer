package and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction;

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ChatGeminiApiAbstract extends ChatGeminiAbstract {

    protected Client client;
    protected List<Content> chatHistory;
    protected GenerateContentConfig config;

    protected ChatGeminiApiAbstract(String apiKey) {
        super(apiKey);
        client = Client.builder().apiKey(apiKey).build();
        chatHistory = new ArrayList<>();
        config = getGenerateContentConfig();
    }

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
                    geminiModel,
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

            // 6. Conteggio quanti token ho consumato
            totalTokens += response.usageMetadata().get().totalTokenCount().get();

            return botResponseText;

        } catch (ClientException e) {
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
                geminiModel,
                promptText,
                config
        );

        // Conteggio quanti token ho consumato
        totalTokens += response.usageMetadata().get().totalTokenCount().get();

        return response.text();
    }

    @Override
    public void setTemperature(float temperature) {
        super.setTemperature(temperature);
        config = getGenerateContentConfig();
    }

    private GenerateContentConfig getGenerateContentConfig() {
        //imposto la capacità di pensare!
        ThinkingConfig thinking = ThinkingConfig.builder()
                .thinkingLevel(ThinkingLevel.Known.LOW)
                .includeThoughts(false)
                .build();

        return GenerateContentConfig.builder()
                .temperature(temperature)
                .thinkingConfig(thinking)
                .build();
    }
}
