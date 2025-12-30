package and.learn.ai.englishtextgenerator.behaviour;

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
