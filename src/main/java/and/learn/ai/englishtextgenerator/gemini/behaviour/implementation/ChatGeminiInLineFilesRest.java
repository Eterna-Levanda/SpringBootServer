package and.learn.ai.englishtextgenerator.gemini.behaviour.implementation;

import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.*;

public class ChatGeminiInLineFilesRest implements ChatGeminiInterface {

    /**
     * Chiama Gemini senza effettuare upload dei file
     */
    public String sendMessageWithFiles(String promptText, byte[] pdf1, byte[] pdf2) throws IOException {

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // Costruzione del corpo JSON
        ObjectNode root = mapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ObjectNode contentObj = contents.addObject();
        ArrayNode parts = contentObj.putArray("parts");

        // 1. Aggiunta del testo del prompt
        parts.addObject().put("text", promptText);

        // 2. Aggiunta del primo PDF (Base64)
        addInlineData(parts, pdf1);

        // 3. Aggiunta del secondo PDF (Base64)
        addInlineData(parts, pdf2);

        // Configurazione opzionale (Temperature)
        ObjectNode genConfig = root.putObject("generationConfig");
        genConfig.put("temperature", TEMPERATURE);

        String jsonPayload = mapper.writeValueAsString(root);
        //  System.out.println("Json request: " + jsonPayload);


        // Creazione e invio della richiesta HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_API_GEMINI))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Chiamata verso Gemini in errore: " + response.body());
            }
            // Estrazione del testo della risposta dal JSON di Gemini
            return mapper.readTree(response.body())
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (InterruptedException e) {
            throw new IOException("Chiamata verso Gemini in fase di upload file. Errore: " + e);
        }
    }

    @Override
    public String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) throws IOException {
        throw new RuntimeException("Metodo non implementato");
    }

    @Override
    public String sendMessageUsingMemory(String promptText) throws IOException {
        throw new RuntimeException("Metodo non implementato");
    }

    @Override
    public String sendMessage(String promptText) throws IOException {
        throw new RuntimeException("Metodo non implementato");
    }

    private void addInlineData(ArrayNode parts, byte[] data) {
        ObjectNode inlineData = parts.addObject().putObject("inlineData");
        inlineData.put("mimeType", APPLICATION_PDF);
        //se il file fosse stato caricato, il nome del campo sarebbe file_uri
        inlineData.put("data", Base64.getEncoder().encodeToString(data));
    }
}
