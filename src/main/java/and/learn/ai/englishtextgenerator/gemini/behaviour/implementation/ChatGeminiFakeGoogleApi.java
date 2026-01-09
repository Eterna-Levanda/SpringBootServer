package and.learn.ai.englishtextgenerator.gemini.behaviour.implementation;

import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiAbstract;

import java.io.IOException;

public class ChatGeminiFakeGoogleApi extends ChatGeminiAbstract {

    private final static int numRows = 8;
    private static int numRow = 1;

    public ChatGeminiFakeGoogleApi(String apiKey) {
        //apiKey nullo perchè fake
        super(null);
    }

    @Override
    public String sendMessage(String promptText) {
        return createFakeStory();
    }

    @Override
    public String sendMessageWithFiles(String promptText, byte[] file1, byte[] file2) {
        return createFakeStory();
    }

    @Override
    public String sendMessageUsingMemory(String promptText) {
        return createFakeStory();
    }

    @Override
    public String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) {
        return createFakeStory();
    }

    private String createFakeStory() {
        StringBuilder story = new StringBuilder();
        for (int i = 0; i < numRows; i++) {
            story.append("**Riga n. ").append(numRow).append("\n").append("\n");
            numRow++;
        }

        return story.toString();
    }
}
