package and.learn.ai.englishtextgenerator;

import and.learn.ai.englishtextgenerator.drive.DriveManager;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviour;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviourSwitcher;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiInterface;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class EnglishTextGeneratorMain {
    // Parametri applicativi
    public static final float TEMPERATURE = 0.5f;
    private static final int numRigheInSingoloParagrafo = 5;
    public static final String GEMINI_VERSION = "gemini-2.5-flash-lite";// "gemini-flash-latest";
    public static final String PROMPT_ALLUNGAMENTO_PARAGRAFO = "Continue to apply the grammar rules early requested in order to prolong the following paragraph. ";
    public static final String APPLICATION_PDF = "application/pdf";
    // Caricamento della chiave API e del Service Account
    public static final String API_KEY;
    public static final String URL_API_GEMINI;
    // ID dei tuoi Google Docs
    private static final String DOC_ID_PROMPT;
    private static final String DOC_ID_ERRORI_FREQUENTI;
    private static final String DOC_ID_SINONIMI;

    static {
        //inizializzazione delle costanti tramite file di properties
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(Path.of("src/main/resources/ai/config/englishtextgenerator/secret.properties"))) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }

        API_KEY = map.get("apiKey");
        DOC_ID_PROMPT = map.get("docIdPrompt");
        DOC_ID_ERRORI_FREQUENTI = map.get("docIdErroriFrequenti");
        DOC_ID_SINONIMI = map.get("docIdSinonimi");

        URL_API_GEMINI = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_VERSION + ":generateContent?key="
                + API_KEY;
    }

    private static ChatGeminiInterface chatGemini;

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        DriveManager driveManager = new DriveManager();

        // Esportazione dei documenti Drive (Operazione specifica per file nativi)
        String prompt = driveManager.extractNativeDocText(DOC_ID_PROMPT);
        byte[] contentSinonimi = driveManager.exportNativeDoc(DOC_ID_SINONIMI);
        byte[] contentErroriFrequenti = driveManager.exportNativeDoc(DOC_ID_ERRORI_FREQUENTI);

        System.out.println("Documenti Drive scaricati con successo. Ora verrà generato il testo con Gemini.");

        long start = System.currentTimeMillis();
        // Chiamate a Gemini
        chatGemini = ChatGeminiBehaviourSwitcher.getInstance(ChatGeminiBehaviour.UPLOAD_FILES_API);

        String risposta = chatGemini
                .sendMessageWithFilesUsingMemory(prompt, contentSinonimi, contentErroriFrequenti);


        List<String> righe = suddividiRighe(risposta);
        List<String> paragrafi = raggruppaRigheInParagrafiLunghiN(righe);

        System.out.println("Storia generata. Ora chiedo di generare i singoli paragrafi. In totale saranno "+ paragrafi.size());


        //allunghiamo i paragrafi
        List<String> parafrafiAllungati = allungaParagrafi(paragrafi);


        //long end = System.currentTimeMillis(); System.out.println("Tempo impegato in millisecondi: " + (end - start));
        parafrafiAllungati.forEach(System.out::println);
    }

    private static List<String> allungaParagrafi(List<String> paragrafi) {
        List<String> paragrafiAllungati = new ArrayList<>();
        int numPar = 1;
        for(String paragrafo: paragrafi) {
            try {
                paragrafiAllungati.add(chatWithGemini(paragrafo));
                System.out.println("Paragrafo allungato n."+numPar);
                numPar++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return paragrafiAllungati;
    }

    private static List<String> allungaParagrafiAsync(List<String> paragrafi) {
        ExecutorService executorService = Executors.newFixedThreadPool(paragrafi.size());

        List<CompletableFuture<String>> futures = paragrafi.stream()
                .map(paragrafo -> CompletableFuture.supplyAsync(() -> {
                            try {
                                return chatWithGemini(paragrafo);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }, executorService)

                        //  .completeOnTimeout("Timeout per " + source, 2, TimeUnit.SECONDS)
                )
                .collect(Collectors.toList());

        List<String> paragrafiAllungati = raccoltaParagrafiAllungati(futures);

        // Shutdown del thread pool
        executorService.shutdown();

        return paragrafiAllungati;
    }

    private static List<String> raccoltaParagrafiAllungati(List<CompletableFuture<String>> futures) {
        CompletableFuture<String>[] futuresArray = futures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(futuresArray).join();
        List<String> results = new ArrayList<>();
        for (CompletableFuture<String> cf : futures) {
            results.add(cf.join());
        }
        return results;
    }


    private static String chatWithGemini(String input) throws IOException {
        return chatGemini.sendMessageUsingMemory(PROMPT_ALLUNGAMENTO_PARAGRAFO + input);
    }

    private static List<String> raggruppaRigheInParagrafiLunghiN(List<String> righe) {
        List<String> paragrafi = new ArrayList<>();
        //va da 0 a numRigheInSingoloParagrafo
        int numRigaParagrafo = 1;
        StringBuilder paragrafo = new StringBuilder();
        for (String riga : righe) {
            if (numRigaParagrafo % numRigheInSingoloParagrafo > 0) {
                paragrafo.append(riga).append(" ");
                numRigaParagrafo++;
            } else {
                paragrafi.add(paragrafo.append(riga).toString());
                paragrafo = new StringBuilder();
                numRigaParagrafo = 1;
            }
        }
        if (!paragrafo.isEmpty()) {
            paragrafi.add(paragrafo.toString());
        }
        return paragrafi;
    }


    private static List<String> suddividiRighe(String risposta) {
        risposta = risposta.replace("*", "");
        return Stream.of(risposta.split("\n")).filter(r -> !r.isBlank()).collect(Collectors.toList());
    }
}
