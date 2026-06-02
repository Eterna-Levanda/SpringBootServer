package and.learn.ai.englishtextgenerator.record;

    public record ConfigVariables(// Costanti valorizzate a runtime con valori sensibili come chiavi API o ID di documenti Drive
                              // Caricamento della chiave API e del Service Account
                              String apiKey,
                                      // ID dei tuoi Google Docs
                                      String docIdMainPrompt,
                                      String docIdErroriFrequenti,
                                      String docIdSinonimi,
                                      String docIdStoriaCreata,
                                      String docIdGerundInfinitive) {
}
