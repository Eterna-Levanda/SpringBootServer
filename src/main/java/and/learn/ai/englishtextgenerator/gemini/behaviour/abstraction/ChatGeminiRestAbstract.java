package and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction;

public abstract class ChatGeminiRestAbstract extends ChatGeminiAbstract {

    protected static String urlApiGemini;

    protected ChatGeminiRestAbstract(String apiKey) {
        super(apiKey);
        createUrlApiGemini();
    }

    @Override
    public void changeModello() {
        super.changeModello();
        createUrlApiGemini();
    }

    @Override
    public String sendMessageUsingMemory(String promptText) {
        throw new RuntimeException("Metodo non implementato");
    }

    @Override
    public String sendMessage(String promptText) {
        throw new RuntimeException("Metodo non implementato");
    }

    @Override
    public String sendMessageWithFilesUsingMemory(String promptText, byte[] file1, byte[] file2) {
        throw new RuntimeException("Metodo non implementato");
    }

    private void createUrlApiGemini() {
        urlApiGemini = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key="
                + apiKey;
    }
}
