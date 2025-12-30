package and.learn.ai.englishtextgenerator.gemini.behaviour;

import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.ChatGeminiInLineFilesGoogleApi;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.ChatGeminiInLineFilesRest;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.ChatGeminiUploadFilesGoogleApi;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.ChatGeminiUploadFilesRest;

public class ChatGeminiBehaviourSwitcher {

    public static ChatGeminiInterface getInstance(ChatGeminiBehaviour behaviour) {
        ChatGeminiInterface instance = null;
        if (behaviour == ChatGeminiBehaviour.UPLOAD_FILES_REST) {
            instance = new ChatGeminiUploadFilesRest();
        } else if (behaviour == ChatGeminiBehaviour.INLINE_FILES_REST) {
            instance = new ChatGeminiInLineFilesRest();
        } else if (behaviour == ChatGeminiBehaviour.INLINE_FILES_API) {
            instance = new ChatGeminiInLineFilesGoogleApi();
        } else if (behaviour == ChatGeminiBehaviour.UPLOAD_FILES_API) {
            instance = new ChatGeminiUploadFilesGoogleApi();
        }

        return instance;
    }
}
