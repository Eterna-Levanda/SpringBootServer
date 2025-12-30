package and.learn.ai.englishtextgenerator.behaviour;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.*;

public class ChatGeminiUploadFilesRest implements ChatGeminiInterface {

    /**
     * Questo metodo effetua upload
     */
    @Override
    public String chiamaGemini(byte[] file1, byte[] file2, String promptText) throws IOException {

        HttpClient client = HttpClient.newHttpClient();
        String fileUri1;
        String fileUri2;

        try {
            // 1) Upload PDF 1
            fileUri1 = uploadPdf(client, file1);

            // 2) Upload PDF 2
            fileUri2 = uploadPdf(client, file2);


            // 3) Prompt Gemini con riferimento ai file caricato. Il json è lo stesso dell'inline, cambia solo "file_uri" con "data"
            String requestBody = """
                    {
                      "contents": [
                        {
                          "role": "user",
                          "parts": [
                            { "text": "%s" },
                            {
                              "file_data": {
                                "mime_type": "application/pdf",
                                "file_uri": "%s"
                              }
                            },
                            {
                              "file_data": {
                                "mime_type": "application/pdf",
                                "file_uri": "%s"
                              }
                            }
                          ]
                        }
                      ],
                      "generationConfig": {
                          "temperature": 0.5
                        }
                    }
                    """.formatted(
                    promptText.replace("\"", "\\\""),
                    fileUri1,
                    fileUri2
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API_GEMINI))
                    .header("Content-Type", APPLICATION_PDF)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Chiamata verso Gemini in errore: " + response.body());
            }

            // Parsing minimale della risposta (estrazione testo)
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            return json
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (InterruptedException e) {
            throw new IOException("Chiamata verso Gemini in fase di upload file. Errore: " + e);
        }
    }

    private static String uploadPdf(HttpClient client, byte[] pdfBytes)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + API_KEY))
                .header("Content-Type", APPLICATION_PDF)
                .header("X-Goog-Upload-Protocol", "raw")
                .POST(HttpRequest.BodyPublishers.ofByteArray(pdfBytes))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Errore upload file: " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.get("file").getAsJsonObject().get("uri").getAsString();
    }
}
