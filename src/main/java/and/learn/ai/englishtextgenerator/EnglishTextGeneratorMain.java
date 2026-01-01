package and.learn.ai.englishtextgenerator;

import and.learn.ai.englishtextgenerator.googleservice.DocsManager;
import and.learn.ai.englishtextgenerator.googleservice.DriveManager;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviour;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviourSwitcher;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiInterface;
import and.learn.ai.englishtextgenerator.googleservice.GoogleServicesFactory;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class EnglishTextGeneratorMain {
    // Parametri applicativi
    public static final float TEMPERATURE = 0.5f;
    private static final int NUM_RIGHE_PER_BLOCCO_IN_STORIA_ORIGINALE = 5;
    private static final int NUM_RIGHE_PER_PARAGRAFO = 10;
    public static final String GEMINI_VERSION = "gemini-flash-latest";//"gemini-2.0-flash-lite";//"gemini-2.5-flash-lite";// "gemini-flash-latest";

    // Prompt
    public static final String PROMPT_ALLUNGAMENTO_PARAGRAFO = "Continue to apply the grammar rules early requested in order to prolong the following paragraph. ";
    public static final String TRANSLATE_TEXT_PROMPT = "Translate this text in Italian keeping as much as possibile the fidelity with the original text in English. This is the text. ";

    // Stringhe maggiormente usate
    public static final String APPLICATION_PDF = "application/pdf";
    public static final String LINE_BREAK = "\n";
    public static final String SERIE_DI_LINE_BREAK = "\n\n\n\n\n\n\n\n\n";

    // Costanti valorizzate a runtime
    // Caricamento della chiave API e del Service Account
    public static final String API_KEY;
    public static final String URL_API_GEMINI;
    // ID dei tuoi Google Docs
    private static final String DOC_ID_PROMPT;
    private static final String DOC_ID_ERRORI_FREQUENTI;
    private static final String DOC_ID_SINONIMI;
    private static final String DOC_ID_STORIA_CREATA;


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
        DOC_ID_STORIA_CREATA = map.get("docIdStoriaCreata");

        URL_API_GEMINI = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_VERSION + ":generateContent?key="
                + API_KEY;
    }

    private static ChatGeminiInterface chatGemini;

    public static void main(String[] args) throws Exception {

        GoogleServicesFactory googleServicesFactory = new GoogleServicesFactory();
        DriveManager driveManager = googleServicesFactory.getDriveManager();

        // Esportazione dei documenti Drive (Operazione specifica per file nativi)
        String prompt = driveManager.extractNativeDocText(DOC_ID_PROMPT);
        byte[] contentSinonimi = driveManager.exportNativeDoc(DOC_ID_SINONIMI);
        byte[] contentErroriFrequenti = driveManager.exportNativeDoc(DOC_ID_ERRORI_FREQUENTI);

        System.out.println("Documenti Drive scaricati con successo. Ora verrà generato il testo con Gemini.");

        long start = System.currentTimeMillis();
        // Chiamate a Gemini
        chatGemini = ChatGeminiBehaviourSwitcher.getInstance(ChatGeminiBehaviour.UPLOAD_FILES_API);

        String storia = chatGemini
                .sendMessageWithFilesUsingMemory(prompt, contentSinonimi, contentErroriFrequenti);

        List<String> righeDellaStoria = suddividiRighe(storia);

        //estraggo il titolo e lo rimuovo dalla storia
        String titolo = righeDellaStoria.remove(0);

        List<String> blocchi = raggruppaRigheInBlocchiLunghiN(righeDellaStoria, NUM_RIGHE_PER_BLOCCO_IN_STORIA_ORIGINALE);

        System.out.println("Storia generata. Ora chiedo di allungare i singoli blocchi che in totale saranno " + blocchi.size());
        List<String> blocchiAllungati = allungaBlocchi(blocchi);
        System.out.println("Accorpo i blocchi in un'unica storia lunga");
        String storiaCompletaAllungata = accorpa(blocchiAllungati);

        System.out.println("Sudddivido la storia in una lista di paragrafi lunghi "+ NUM_RIGHE_PER_PARAGRAFO + " righe ciascuno");
        List<String> paragrafi = suddividiInParagrafi(storiaCompletaAllungata);

        System.out.println("Ora chiedo la traduzione dei paragrafi");
        List<String> paragrafiTradotti = traduciParagrafi(paragrafi);

        System.out.println("Testo in italiano!\n");
        String storiaTradottaDaStampare = stampa(paragrafiTradotti);
        System.out.println(storiaTradottaDaStampare);

        System.out.println("Testo in inglese!\n");
        String storiaOriginaleDaStampare = stampa(paragrafi);
        System.out.println(storiaOriginaleDaStampare);

        System.out.println("Creazione del file su Drive contenente la storia creata");
        DocsManager docsManager = googleServicesFactory.getDocsManager();
        docsManager.appendToDocument(DOC_ID_STORIA_CREATA, titolo + LINE_BREAK + LINE_BREAK + storiaTradottaDaStampare + SERIE_DI_LINE_BREAK + storiaOriginaleDaStampare);

        System.out.println("File creato correttamente sul Drive!");

    }

    private static String stampa(List<String> paragrafi) {
        StringBuilder storia = new StringBuilder();
        paragrafi.forEach(p -> {
            storia.append(p).append(LINE_BREAK).append(LINE_BREAK);
        });
        return storia.toString();
    }

    private static List<String> suddividiInParagrafi(String storiaCompletaAllungata) {
        List<String> paragrafi;
        List<String> storiaCompletaInRighe = suddividiRighe(storiaCompletaAllungata);
        paragrafi = raggruppaRigheInBlocchiLunghiN(storiaCompletaInRighe, NUM_RIGHE_PER_PARAGRAFO);
        return paragrafi;
    }

    /**
     * Restituisce una stringa composta dalla concatenazione di tutte le strighe contenute nella lista
     */
    private static String accorpa(List<String> paragrafiAllungati) {
        StringBuilder storiaCompleta = new StringBuilder();
        paragrafiAllungati.stream().forEach(storiaCompleta::append);

        return storiaCompleta.toString();
    }

    private static List<String> traduciParagrafi(List<String> paragrafiAllungati) {

        List<String> paragrafiTradotti = new ArrayList<>();
        int numParagrafo = 1;
        for (String paragrafo : paragrafiAllungati) {
            try {
                System.out.println("Chiedo traduzione del paragrafo n. " + numParagrafo);
                String paragrafoTradotto = chatGemini.sendMessage(TRANSLATE_TEXT_PROMPT + paragrafo);
                String paragrafoPulito = eliminaRigheVuoteEAsterischi(paragrafoTradotto);
                paragrafiTradotti.add(paragrafoPulito);
                numParagrafo++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return paragrafiTradotti;
    }

    private static List<String> allungaBlocchi(List<String> paragrafi) {
        List<String> paragrafiAllungati = new ArrayList<>();
        int numPar = 1;
        for (String paragrafo : paragrafi) {
            try {
                String paragrafoAllungato = chatWithGemini(paragrafo);
                String paragrafoSenzaRigheVuote = eliminaRigheVuoteEAsterischi(paragrafoAllungato);
                //TODO vedere se anche con Gemini da qui si esce con un a capo, non con una riga vuota
                paragrafiAllungati.add(paragrafoSenzaRigheVuote + LINE_BREAK);
                System.out.println("Generato il blocco di testo allungato n." + numPar);
                numPar++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return paragrafiAllungati;
    }

    private static @NonNull String eliminaRigheVuoteEAsterischi(String paragrafoAllungato) {
        return paragrafoAllungato.lines()
                .filter(line -> !line.isBlank())
                .map(p -> p.replace("*", ""))
                .collect(Collectors.joining(LINE_BREAK));
    }


    private static String chatWithGemini(String input) throws IOException {
        return chatGemini.sendMessageUsingMemory(PROMPT_ALLUNGAMENTO_PARAGRAFO + input);
    }

    private static List<String> raggruppaRigheInBlocchiLunghiN(List<String> righe, int numerosita) {
        List<String> paragrafi = new ArrayList<>();
        //va da 0 a numRigheInSingoloParagrafo
        int numRigaParagrafo = 1;
        StringBuilder paragrafo = new StringBuilder();
        for (String riga : righe) {
            if (numRigaParagrafo % numerosita > 0) {
                paragrafo.append(riga).append(LINE_BREAK);
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
        return Stream.of(risposta.split(LINE_BREAK)).filter(r -> !r.isBlank()).collect(Collectors.toList());
    }
}
