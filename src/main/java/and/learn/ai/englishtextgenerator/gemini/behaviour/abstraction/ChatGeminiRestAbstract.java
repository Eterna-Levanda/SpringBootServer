package and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.API_KEY;

public abstract class ChatGeminiRestAbstract extends ChatGeminiAbstract {

    protected static String urlApiGemini;

    protected ChatGeminiRestAbstract() {
        createUrlApiGemini();
    }

    @Override
    public void changeModello() {
        super.changeModello();
        createUrlApiGemini();
    }

    private void createUrlApiGemini() {
        urlApiGemini = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key="
                + API_KEY;
    }
}
