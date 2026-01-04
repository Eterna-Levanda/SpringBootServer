package and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.api;

import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiApiAbstract;
import com.google.genai.types.*;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.*;

public class ChatGeminiUploadFilesApi extends ChatGeminiApiAbstract {

    /**
     * Chiama Gemini effettuando upload dei file
     */
    public String sendMessageWithFiles(String promptText, byte[] file1, byte[] file2) throws IOException {

        GenerateContentResponse response = null;

        // Creazione del prompt usando i riferimenti (FileData)
        List<Part> promptParts = uploadFiles(file1, file2, promptText);

        response = client.models.generateContent(
                geminiModel,
                promptParts.toString(),
                null
        );

        return response.text();
    }

    public String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) throws IOException {

        List<Part> promptParts = uploadFiles(file1, file2, promptText);

        // Creazione del nuovo Content dell'utente e aggiunta alla storia
        Content userContent = Content.builder()
                .role("user")
                .parts(promptParts)
                .build();

        // Aggiornamento della history della chat con il prompt del programma
        chatHistory.add(userContent);

        try {
            // Chiamata al modello passando TUTTA la history
            // Importante: passiamo la lista chatHistory direttamente
            GenerateContentResponse response = client.models.generateContent(
                    geminiModel,
                    chatHistory,
                    null
            );

            String modelResponseText = response.text();

            // Aggiornamento della history della chat con la rispsota di Gemini
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

    private @NonNull List<Part> uploadFiles(byte[] file1, byte[] file2, String promptText) {
        // 1. Preparazione delle Parti del messaggio corrente
        List<Part> currentParts = new ArrayList<>();

        if (file1 != null && file1.length > 0) {

            //chiamata a Gemini per upload file 1
            File uploadedFile1 = client.files.upload(file1,
                    UploadFileConfig.builder().mimeType(APPLICATION_PDF).displayName("Documento 1").build());

            // Aggiungiamo i riferimenti ai file nelle Parti del primo messaggio
            currentParts.add(Part.builder().fileData(
                    FileData.builder().fileUri(uploadedFile1.uri().get()).mimeType(APPLICATION_PDF).build()
            ).build());
        }

        if (file2 != null && file2.length > 0) {
            //chiamata a Gemini per upload file 2
            File uploadedFile2 = client.files.upload(file2,
                    UploadFileConfig.builder().mimeType(APPLICATION_PDF).displayName("Documento 2").build());

            currentParts.add(Part.builder().fileData(
                    FileData.builder().fileUri(uploadedFile2.uri().get()).mimeType(APPLICATION_PDF).build()
            ).build());
        }

        // Aggiungiamo sempre il testo del prompt
        currentParts.add(Part.builder().text(promptText).build());
        return currentParts;
    }
}
