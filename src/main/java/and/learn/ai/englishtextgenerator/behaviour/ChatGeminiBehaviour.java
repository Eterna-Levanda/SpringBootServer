package and.learn.ai.englishtextgenerator.behaviour;

//Elenca le implementazioni possibili verso Gemini per inviare un messaggio con allegati
public enum ChatGeminiBehaviour {
    UPLOAD_FILES_REST,
    INLINE_FILES_REST,
    INLINE_FILES_API,
    UPLOAD_FILES_API;
}
