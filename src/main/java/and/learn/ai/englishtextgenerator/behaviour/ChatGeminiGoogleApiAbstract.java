package and.learn.ai.englishtextgenerator.behaviour;

import com.google.genai.Client;

import java.io.IOException;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.API_KEY;

public class ChatGeminiGoogleApiAbstract implements ChatGeminiInterface {

    protected final Client client;

    protected ChatGeminiGoogleApiAbstract(){
        client = Client.builder().apiKey(API_KEY).build();
    }

    @Override
    public String chiamaGemini(byte[] file1, byte[] file2, String promptText) throws IOException {
        throw new RuntimeException("Metodo non implementato");
    }
}
