package and.learn.ai.englishtextgenerator.gemini.behaviour;

import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiAbstract;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.*;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.api.ChatGeminiInLineFilesApi;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.api.ChatGeminiUploadFilesApi;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.rest.ChatGeminiInLineFilesRest;
import and.learn.ai.englishtextgenerator.gemini.behaviour.implementation.rest.ChatGeminiUploadFilesRest;

public class ChatGeminiBehaviourCreator {

    public static ChatGeminiAbstract getInstance(ChatGeminiBehaviour behaviour, String apiKey) {
        ChatGeminiAbstract instance = null;
        if (behaviour == ChatGeminiBehaviour.UPLOAD_FILES_REST) {
            instance = new ChatGeminiUploadFilesRest(apiKey);
        } else if (behaviour == ChatGeminiBehaviour.INLINE_FILES_REST) {
            instance = new ChatGeminiInLineFilesRest(apiKey);
        } else if (behaviour == ChatGeminiBehaviour.INLINE_FILES_API) {
            instance = new ChatGeminiInLineFilesApi(apiKey);
        } else if (behaviour == ChatGeminiBehaviour.UPLOAD_FILES_API) {
            instance = new ChatGeminiUploadFilesApi(apiKey);
        } else if (behaviour == ChatGeminiBehaviour.FAKE_IMPLEMENTATION) {
            instance = new ChatGeminiFakeGoogleApi(apiKey);
        }

        return instance;
    }
}
