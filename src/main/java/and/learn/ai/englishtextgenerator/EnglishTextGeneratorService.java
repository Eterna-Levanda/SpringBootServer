package and.learn.ai.englishtextgenerator;

import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviour;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviourCreator;
import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiAbstract;
import and.learn.ai.englishtextgenerator.googleservice.DocsManager;
import and.learn.ai.englishtextgenerator.googleservice.DriveManager;
import and.learn.ai.englishtextgenerator.googleservice.GoogleServicesFactory;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
public class EnglishTextGeneratorService {

    //vale true se l'app viene eseguita su Google Cloud
    public static final boolean GOOGLE_CLOUD_ENABLED = StringUtils.isNotEmpty(System.getenv("google_cloud"));

    // Parametri applicativi
    private static final String SEPARATORE_PROMPT = "PROMPT_2";
    private static final String PATH_PROPERTIES = "src/main/resources/ai/englishtextgenerator/config/secret.properties";
    private static final String PATH_LOG = "src/main/resources/ai/englishtextgenerator/log";

    //parametri che cambiano il comportamento dell'applicativo
    private static final int NUMERO_REGOLE_SINGOLA_RICHIESTA = 2;
    private static final int NUM_CARATTERI_PER_SINGOLO_PARAGRAFO = 600;

    // Prompt
    private static final String INITIAL_RECOMMENDATION = "Now, revise the previous story and apply the following instructions. Give me back the renewed story. ";
    private static final String FINAL_RECOMMENDATION = "Doing this, do not modify sentences already changed, but add new short text. Be sure the story continues to sound natural and the plot is coherent and sensible. Even more important: the text must be utterly understandable for a B2 student, do not use more complicated words. ";
    private static final String APPLY_RULES_PROMPT = INITIAL_RECOMMENDATION + "Where it sounds naturally, apply to the story the following grammar rules a couple of times. " + FINAL_RECOMMENDATION;
    private static final String GERUND_INFINITIVE_PROMPT = INITIAL_RECOMMENDATION + "Read the document attached containing many examples of usage of verb in gerund or infinitive form. Choose 4 or 5 of them and apply them to the story. " + FINAL_RECOMMENDATION;
    private static final String SYNONYMS_PROMPT = INITIAL_RECOMMENDATION + "Read the document attached containing many words and their synonyms. Try to apply 7 or 8 of them to the story. " + FINAL_RECOMMENDATION;
    private static final String TRANSLATE_TEXT_PROMPT = "Translate the following text from English into Italian. Use a natural, conversational tone. Don't add any your comment. This is the text. ";

    // Stringhe maggiormente usate
    private static final String LINE_BREAK = "\n";
    private static final String SERIE_DI_LINE_BREAK = "\n\n\n\n\n\n\n\n\n";

    //variabili utilizzate solo per configurazione tramite variabili d'ambiente come in Google Cloud Run
    private static final String ENGLISH_TEXT_GENERATOR = "EnglishTextGenerator.";
    private static final String API_KEY = "apiKey";
    private static final String DOC_ID_PROMPT = "docIdPrompt";
    private static final String DOC_ID_ERRORI_FREQUENTI = "docIdErroriFrequenti";
    private static final String DOC_ID_SINONIMI = "docIdSinonimi";
    private static final String DOC_ID_STORIA_CREATA = "docIdStoriaCreata";
    private static final String DOC_ID_GERUND_INFINITIVE = "docIdGerundInfinitive";
    //contenitore di variabili di configurazione
    private final ConfigVariables configVariables;

    public EnglishTextGeneratorService() {
        log.info("Avvio EnglishTextGeneratorService");
        //inizializzazione delle costanti tramite file di properties
        Properties props = new Properties();

        //se non sono su Google Cloud, leggo le properties da file, altrimenti mi aspetto che siano tutte valorizzate da variabili d'ambiente
        if (!GOOGLE_CLOUD_ENABLED) {
            try (InputStream is = Files.newInputStream(Path.of(PATH_PROPERTIES))) {
                props.load(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, String> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }

        // Costanti valorizzate a runtime con valori sensibili come chiavi API o ID di documenti Drive
        // Caricamento della chiave API e del Service Account
        String API_KEY_VALUE = Optional.ofNullable(System.getenv(ENGLISH_TEXT_GENERATOR + API_KEY)).orElse(map.get(API_KEY));

        //lettura delle costanti da variabili d'ambiente (se presenti in Cloud) o da file di properties
        String DOC_ID_MAIN_PROMPT_VALUE = Optional.ofNullable(System.getenv(ENGLISH_TEXT_GENERATOR + DOC_ID_PROMPT)).orElse(map.get(DOC_ID_PROMPT));
        String DOC_ID_ERRORI_FREQUENTI_VALUE = Optional.ofNullable(System.getenv(ENGLISH_TEXT_GENERATOR + DOC_ID_ERRORI_FREQUENTI)).orElse(map.get(DOC_ID_ERRORI_FREQUENTI));
        String DOC_ID_SINONIMI_VALUE = Optional.ofNullable(System.getenv(ENGLISH_TEXT_GENERATOR + DOC_ID_SINONIMI)).orElse(map.get(DOC_ID_SINONIMI));
        String DOC_ID_STORIA_CREATA_VALUE = Optional.ofNullable(System.getenv(ENGLISH_TEXT_GENERATOR + DOC_ID_STORIA_CREATA)).orElse(map.get(DOC_ID_STORIA_CREATA));
        String DOC_ID_GERUND_INFINITIVE_VALUE = Optional.ofNullable(System.getenv(ENGLISH_TEXT_GENERATOR + DOC_ID_GERUND_INFINITIVE)).orElse(map.get(DOC_ID_GERUND_INFINITIVE));

        configVariables = new ConfigVariables(API_KEY_VALUE, DOC_ID_MAIN_PROMPT_VALUE,
                DOC_ID_ERRORI_FREQUENTI_VALUE, DOC_ID_SINONIMI_VALUE,
                DOC_ID_STORIA_CREATA_VALUE, DOC_ID_GERUND_INFINITIVE_VALUE);
    }

    public void generaStoria() throws Exception {
        // Usato per creare file di log univoci
        Integer numFile = 0;
        String sessionID = UUID.randomUUID().toString();

        // Creo il client Gemini, usando un'implementazione specifica predefinita
        final ChatGeminiAbstract clientChatGemini = ChatGeminiBehaviourCreator.getInstance(ChatGeminiBehaviour.UPLOAD_FILES_API, configVariables.apiKey());

        //istanzio le classi per interfacciarmi con Google Drive e Docs
        GoogleServicesFactory googleServicesFactory = new GoogleServicesFactory();
        DriveManager driveManager = googleServicesFactory.getDriveManager();

        // Lettura dei documenti da Google Drive
        // File n.1 i prompt per la creazione della storia in formato Stringa
        String filePrompts = driveManager.extractNativeDocText(configVariables.docIdMainPrompt());
        PromptForGenerateText coppiaDiPrompt = splitByString(filePrompts);
        String promptIniziale = coppiaDiPrompt.firstPrompt();
        String elencoRequisitiGrammaticali = coppiaDiPrompt.secondPrompt();

        //Serie di file da Google Drive scaricati come array di byte, per usarli poi come allegati ai prompt
        byte[] contentSinonimi = driveManager.exportNativeDoc(configVariables.docIdSinonimi());
        byte[] contentErroriFrequenti = driveManager.exportNativeDoc(configVariables.docIdErroriFrequenti());
        byte[] contentGerundInfinitive = driveManager.exportNativeDoc(configVariables.docIdGerundInfinitive());

        log.info("Documenti Drive scaricati con successo. Ora verrà generato il testo con Gemini.");

        String storia;
        //creo la storia
        clientChatGemini.setTemperature(0.8f);
        storia = sendPromptWithFiles(clientChatGemini, promptIniziale, contentSinonimi, contentErroriFrequenti, sessionID, numFile);
        logSuFile(storia, sessionID, numFile);

        log.info("Storia creata");

        // Modifica la storia, aggiungendo frasi aventi i requisiti grammaticali richiesti, raggruppandoli in gruppi di N (NUMERO_REGOLE_SINGOLA_RICHIESTA) alla volta
        List<List<String>> requirements = groupGrammarRules(elencoRequisitiGrammaticali);
        //ordine casuale nei requisiti
        Collections.shuffle(requirements);
        clientChatGemini.setTemperature(0.5f);


        for (int i = 0; i < requirements.size(); i++) {
            log.info("Applico la serie di requisiti n. " + (i + 1) + " ovvero " + requirements.get(i));
            String promptRequisitiGrammaticali = "";
            numFile++;
            //costruisco un prompt in cui ogni singola regola è separata dalle altre, per meglio esplicitarle
            for (int j = 0; j < requirements.get(i).size(); j++) {
                promptRequisitiGrammaticali += " Requirement n. " + (j + 1) + ": " + requirements.get(i).get(j) + ", ";
            }

            //modifico la storia applicando i requisiti grammaticali richiesti
            storia = sendPromptWithFiles(clientChatGemini, APPLY_RULES_PROMPT + promptRequisitiGrammaticali, null, null, sessionID, numFile);
            logSuFile(storia, sessionID, numFile);
        }

        //modifico nuovamente la storia per applicare regole relative al gerundio/infinito
        clientChatGemini.setTemperature(0.4f);
        for (int i = 0; i < 1; i++) {
            log.info("Applico la regola di gerundio/infinito per la " + (i + 1) + " volta");
            numFile++;
            storia = sendPromptWithFiles(clientChatGemini, GERUND_INFINITIVE_PROMPT, contentGerundInfinitive, null, sessionID, numFile);
            logSuFile(storia, sessionID, numFile);
        }

        //modifico nuovamente la storia per lavorare di nuovo sui sinonimi
        log.info("Aggiungo i sinonimi");
        numFile++;
        clientChatGemini.setTemperature(0.4f);
        storia = sendPromptWithFiles(clientChatGemini, SYNONYMS_PROMPT, contentSinonimi, null, sessionID, numFile);
        logSuFile(storia, sessionID, numFile);

        //estraggo il titolo e lo rimuovo dalla storia
        List<String> righeDellaStoria = suddividiRighe(storia);
        String titolo = righeDellaStoria.remove(0);

        //miglioro il raggruppamento dei paragrafi
        List<String> paragrafi = raggruppaRigheInParagrafiLunghiAlmenoNCaratteri(righeDellaStoria);

        log.info("Ora chiedo la traduzione dei paragrafi, in totale sono " + paragrafi.size());
        clientChatGemini.setTemperature(0.3f);
        List<String> paragrafiTradotti = traduciParagrafi(clientChatGemini, paragrafi, sessionID, numFile);
        log.info("Numero di token richiesti per questa storia: " + clientChatGemini.getTotalTokens());

        String storiaTradottaDaStampare = String.join(LINE_BREAK + LINE_BREAK, paragrafiTradotti);
        String storiaOriginaleDaStampare = String.join(LINE_BREAK + LINE_BREAK, paragrafi);

        log.info("Creazione del file su Drive contenente la storia creata");
        DocsManager docsManager = googleServicesFactory.getDocsManager();
        docsManager.appendToDocument(configVariables.docIdStoriaCreata(), titolo + LINE_BREAK + LINE_BREAK + storiaTradottaDaStampare + SERIE_DI_LINE_BREAK + titolo + LINE_BREAK + LINE_BREAK + storiaOriginaleDaStampare);

        log.info("File creato correttamente sul Drive!");
    }

    private List<String> raggruppaRigheInParagrafiLunghiAlmenoNCaratteri(List<String> righeDellaStoria) {
        //NUM_CARATTERI_PER_SINGOLO_PARAGRAFO
        List<String> paragrafi = new ArrayList<>();
        String rigaTemp = "";
        for (String riga : righeDellaStoria) {
            if (rigaTemp.length() >= NUM_CARATTERI_PER_SINGOLO_PARAGRAFO) {
                paragrafi.add(rigaTemp);
                rigaTemp = riga;
            } else {
                rigaTemp = rigaTemp + LINE_BREAK + riga;
            }
        }
        paragrafi.add(rigaTemp);
        return paragrafi;
    }

    /**
     * Divide una stringa in due parti basandosi su un separatore.
     *
     * @return Un oggetto Pair con la parte a sinistra e la parte a destra del separatore
     */
    private PromptForGenerateText splitByString(String input) {

        int index = input.indexOf(SEPARATORE_PROMPT);
        String left = input.substring(0, index);
        String right = input.substring(index + SEPARATORE_PROMPT.length());
        return new PromptForGenerateText(left, right);
    }

    /**
     * Estrae le regole che iniziano con '*' e le raggruppa in liste di dimensione N.
     */
    private List<List<String>> groupGrammarRules(String input) {
        // 1. Pulizia ed estrazione delle righe che iniziano con '*'
        List<String> allRules = input.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("*"))
                .map(line -> line.substring(1).trim()) // Rimuoviamo l'asterisco
                .collect(Collectors.toList());

        // 2. Raggruppamento
        List<List<String>> groups = new ArrayList<>();
        for (int i = 0; i < allRules.size(); i += NUMERO_REGOLE_SINGOLA_RICHIESTA) {
            int end = Math.min(i + NUMERO_REGOLE_SINGOLA_RICHIESTA, allRules.size());
            groups.add(new ArrayList<>(allRules.subList(i, end)));
        }

        return groups;
    }

    private List<String> traduciParagrafi(ChatGeminiAbstract clientChatGemini, final List<String> paragrafi, String sessionID, int numFile) throws IOException {

        List<String> paragrafiTradotti = new ArrayList<>();
        int numParagrafo = 1;
        for (String paragrafo : paragrafi) {

            log.info("Chiedo traduzione del paragrafo n. " + numParagrafo);
            String paragrafoTradotto = sendPromptWithFiles(clientChatGemini, TRANSLATE_TEXT_PROMPT + paragrafo, null, null, sessionID, numFile);
            String paragrafoPulito = eliminaRigheVuoteEAsterischi(paragrafoTradotto);
            paragrafiTradotti.add(paragrafoPulito);
            numParagrafo++;
        }

        return paragrafiTradotti;
    }

    private String sendPromptWithFiles(ChatGeminiAbstract clientChatGemini, String prompt, byte[] file1, byte[] file2, String sessionID, int numFile) {

        logSuFile(prompt, sessionID, numFile);

        String response;
        try {
            if (file1 != null || file2 != null) {
                response = clientChatGemini.sendMessageWithFilesUsingMemory(prompt, file1, file2);
            } else {
                response = clientChatGemini.sendMessageUsingMemory(prompt);
            }

        } catch (IOException e) {
            //ho esaurito i token di un modello gratuito, ora passo al successivo
            clientChatGemini.changeModello();
            response = sendPromptWithFiles(clientChatGemini, prompt, file1, file2,  sessionID, numFile);
        }
        return response;

    }

    private void logSuFile(String textToLog, String sessionID, int numFile) {

        if (GOOGLE_CLOUD_ENABLED) {
            return;
        }
        // Definiamo il percorso relativo
        Path dirPath = Path.of(PATH_LOG);

        try (Stream<Path> files = Files.walk(dirPath)) {

            numFile++;
            String fileName = "log_" + sessionID + "_NUM_" + numFile + ".txt";
            Path filePath = dirPath.resolve(fileName);

            // 2. Scrive il file (sovrascrive se esiste, lo crea se manca)
            Files.writeString(filePath, textToLog, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Log salvato con successo: " + filePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Errore durante il salvataggio del log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private @NonNull String eliminaRigheVuoteEAsterischi(String paragrafoAllungato) {
        return paragrafoAllungato.lines()
                .filter(line -> !line.isBlank())
                .map(p -> p.replace("*", ""))
                .collect(Collectors.joining(LINE_BREAK));
    }

    private List<String> suddividiRighe(String risposta) {
        risposta = risposta.replace("*", "");
        return Stream.of(risposta.split(LINE_BREAK)).filter(r -> !r.isBlank()).collect(Collectors.toList());
    }
}
