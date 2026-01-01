package and.learn.ai.englishtextgenerator.gemini.behaviour;

//Elenca le implementazioni possibili verso Gemini per inviare un messaggio con allegati
public enum ChatGeminiBehaviour {
    FAKE_IMPLEMENTATION,
    UPLOAD_FILES_REST,
    INLINE_FILES_REST,
    INLINE_FILES_API,
    UPLOAD_FILES_API;
}
