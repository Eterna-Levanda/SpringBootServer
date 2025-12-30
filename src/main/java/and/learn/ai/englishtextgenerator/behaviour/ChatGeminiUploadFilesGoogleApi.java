package and.learn.ai.englishtextgenerator.behaviour;

import com.google.genai.Client;
import com.google.genai.types.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.*;

public class ChatGeminiUploadFilesGoogleApi extends ChatGeminiGoogleApiAbstract {

    /**
     * Chiama Gemini effettuando upload dei file
     */
    public String chiamaGemini(byte[] file1, byte[] file2, String promptText) throws IOException {

        GenerateContentResponse response = null;

        //chiamata a Gemini per upload file 1
        File uploadedFile1 = client.files.upload(file1,
                UploadFileConfig.builder().mimeType(APPLICATION_PDF).displayName("Documento 1").build());

        //chiamata a Gemini per upload file 2
        File uploadedFile2 = client.files.upload(file2,
                UploadFileConfig.builder().mimeType(APPLICATION_PDF).displayName("Documento 2").build());

        // Creazione del prompt usando i riferimenti (FileData)
        List<Part> promptParts = Arrays.asList(
                Part.builder().fileData(
                        FileData.builder().fileUri(uploadedFile1.uri().get()).mimeType(APPLICATION_PDF).build()
                ).build(),
                Part.builder().fileData(
                        FileData.builder().fileUri(uploadedFile2.uri().get()).mimeType(APPLICATION_PDF).build()
                ).build(),
                Part.builder().text(promptText).build()
        );

        // 3. Chiamata al modello
        // Nota: Usiamo una lista di Part invece di una singola stringa
        response = client.models.generateContent(
                GEMINI_VERSION,
                promptParts.toString(),
                null
        );

        return response.text();
    }
}
