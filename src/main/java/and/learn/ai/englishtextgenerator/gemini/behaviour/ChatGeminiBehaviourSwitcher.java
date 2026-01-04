package and.learn.ai.englishtextgenerator.gemini.behaviour;

import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiAbstract;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.*;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.api.ChatGeminiInLineFilesApi;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.api.ChatGeminiUploadFilesApi;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.rest.ChatGeminiInLineFilesRest;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.rest.ChatGeminiUploadFilesRest;

public class ChatGeminiBehaviourSwitcher {

    public static ChatGeminiAbstract getInstance(ChatGeminiBehaviour behaviour) {
        ChatGeminiAbstract instance = null;
        if (behaviour == ChatGeminiBehaviour.UPLOAD_FILES_REST) {
            instance = new ChatGeminiUploadFilesRest();
        } else if (behaviour == ChatGeminiBehaviour.INLINE_FILES_REST) {
            instance = new ChatGeminiInLineFilesRest();
        } else if (behaviour == ChatGeminiBehaviour.INLINE_FILES_API) {
            instance = new ChatGeminiInLineFilesApi();
        } else if (behaviour == ChatGeminiBehaviour.UPLOAD_FILES_API) {
            instance = new ChatGeminiUploadFilesApi();
        } else if (behaviour == ChatGeminiBehaviour.FAKE_IMPLEMENTATION) {
            instance = new ChatGeminiFakeGoogleApi();
        }

        return instance;
    }
}
