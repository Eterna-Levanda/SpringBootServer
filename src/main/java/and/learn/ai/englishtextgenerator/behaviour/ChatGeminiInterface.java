package and.learn.ai.englishtextgenerator.behaviour;

import java.io.IOException;

public interface ChatGeminiInterface {
    String chiamaGemini(byte[] file1, byte[] file2, String promptText) throws IOException;
}
