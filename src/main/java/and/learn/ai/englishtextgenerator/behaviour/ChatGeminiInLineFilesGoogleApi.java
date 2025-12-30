package and.learn.ai.englishtextgenerator.behaviour;

import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.APPLICATION_PDF;
import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.GEMINI_VERSION;

public class ChatGeminiInLineFilesGoogleApi extends ChatGeminiGoogleApiAbstract {

    /**
     * Chiama Gemini senza effettuare upload dei file
     */
    public String chiamaGemini(byte[] file1, byte[] file2, String promptText) throws IOException {

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
}
