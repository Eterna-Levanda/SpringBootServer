package and.learn.ai.englishtextgenerator.behaviour;

import java.io.IOException;

public interface ChatGeminiInterface {
    String sendMessage(String promptText) throws IOException;

    String sendMessageWithFiles(String promptText, byte[] file1, byte[] file2) throws IOException;

    String sendMessageUsingMemory(String promptText) throws IOException;

    String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) throws IOException;

}
