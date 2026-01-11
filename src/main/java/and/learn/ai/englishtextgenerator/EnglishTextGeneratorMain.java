package and.learn.ai.englishtextgenerator;

import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviour;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviourCreator;
import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiAbstract;
import and.learn.ai.englishtextgenerator.googleservice.DocsManager;
import and.learn.ai.englishtextgenerator.googleservice.DriveManager;
import and.learn.ai.englishtextgenerator.googleservice.GoogleServicesFactory;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class EnglishTextGeneratorMain {

    public static final String APPLICATION_PDF = "application/pdf";

    // Parametri applicativi
    private static final String SEPARATORE_PROMPT = "PROMPT_2";
    private static final String PATH_PROPERTIES = "src/main/resources/ai/englishtextgenerator/config/secret.properties";
    private static final String PATH_LOG = "src/main/resources/ai/englishtextgenerator/log";

    //parametri che cambiano il comportamento dell'applicativo
    private static final int NUMERO_REGOLE_SINGOLA_RICHIESTA = 3;
    private static final int NUM_CARATTERI_PER_SINGOLO_PARAGRAFO = 600;

    // Costanti valorizzate a runtime
    // Caricamento della chiave API e del Service Account
    private static final String API_KEY;
    // ID dei tuoi Google Docs
    private static final String DOC_ID_MAIN_PROMPT;
    private static final String DOC_ID_ERRORI_FREQUENTI;
    private static final String DOC_ID_SINONIMI;
    private static final String DOC_ID_STORIA_CREATA;
    private static final String DOC_ID_GERUND_INFINITIVE;

    // Prompt
    private static final String INITIAL_RECOMMENDATION = "Now, revise the previous story and apply the following instructions. Give me back the renewed story. ";
    private static final String FINAL_RECOMMENDATION = "Be sure the story you are working with continues to sound natural and the plot coherent and sensible. Even more important: the text must be understandable for a B2 student, do not use more complicated words. ";
    private static final String APPLY_RULES_PROMPT = INITIAL_RECOMMENDATION + "Where it sounds naturally in the middle of the text, apply to the story the following grammar rules a couple of times. Doing this, do not modify sentences already changed, but add new short text. " + FINAL_RECOMMENDATION;
    private static final String GERUND_INFINITIVE_PROMPT = INITIAL_RECOMMENDATION + "Read the document attached containing many examples of usage of verb in gerund or infinitive form. Choose 4 or 5 of them and apply them to the story in the middle of the text, changing slightly the needed sentences in a very natural way in order to have the story with a few of those verbs. " + FINAL_RECOMMENDATION;
    private static final String SYNONYMS_PROMPT = INITIAL_RECOMMENDATION + "Read the document attached containing many words and their synonyms. Try to apply 7 or 8 of them to the story in the middle of the text, changing slightly the needed sentences in a very natural way, in order to have the story with a few of those synonyms. " + FINAL_RECOMMENDATION;
    private static final String IMPROVE_STORY_PROMPT = "Read the following story in order to improve the readability, avoiding repetition and correcting very innatural sentences. Delete every sentence or comment that is not part of the story. ";
    private static final String TRANSLATE_TEXT_PROMPT = "Translate the following text from English into Italian. Use a natural, conversational tone. Don't add any your comment. This is the text. ";

    // Stringhe maggiormente usate
    private static final String LINE_BREAK = "\n";
    private static final String SERIE_DI_LINE_BREAK = "\n\n\n\n\n\n\n\n\n";

    // Usato per creare file di log univoci
    private static int numFile = 0;
    private static ChatGeminiAbstract clientChatGemini;

    static {
        //inizializzazione delle costanti tramite file di properties
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(Path.of(PATH_PROPERTIES))) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }

        API_KEY = map.get("apiKey");
        DOC_ID_MAIN_PROMPT = map.get("docIdPrompt");
        DOC_ID_ERRORI_FREQUENTI = map.get("docIdErroriFrequenti");
        DOC_ID_SINONIMI = map.get("docIdSinonimi");
        DOC_ID_STORIA_CREATA = map.get("docIdStoriaCreata");
        DOC_ID_GERUND_INFINITIVE = map.get("docIdGerundInfinitive");
    }

    public static void main(String[] args) throws Exception {

        GoogleServicesFactory googleServicesFactory = new GoogleServicesFactory();
        DriveManager driveManager = googleServicesFactory.getDriveManager();

        // Lettura dei documenti Drive
        String filePrompts = driveManager.extractNativeDocText(DOC_ID_MAIN_PROMPT);
        PromptForGenerateText coppiaDiPrompt = splitByString(filePrompts);
        String promptIniziale = coppiaDiPrompt.firstPrompt();
        String elencoRequisitiGrammaticali = coppiaDiPrompt.secondPrompt();

        byte[] contentSinonimi = driveManager.exportNativeDoc(DOC_ID_SINONIMI);
        byte[] contentErroriFrequenti = driveManager.exportNativeDoc(DOC_ID_ERRORI_FREQUENTI);
        byte[] contentGerundInfinitive = driveManager.exportNativeDoc(DOC_ID_GERUND_INFINITIVE);

        log("Documenti Drive scaricati con successo. Ora verrà generato il testo con Gemini.");

        // Creo il client Gemini
        clientChatGemini = ChatGeminiBehaviourCreator.getInstance(ChatGeminiBehaviour.UPLOAD_FILES_API, API_KEY);

        String storia;
        //creo la storia
        clientChatGemini.setTemperature(0.8f);
        storia = sendPromptWithFiles(promptIniziale, contentSinonimi, contentErroriFrequenti);
        logSuFile(storia);

        log("Storia creata");

        // Aggiunge nella storia requisiti grammaticali a gruppi di N (NUMERO_REGOLE_SINGOLA_RICHIESTA) alla volta
        List<List<String>> requirements = groupGrammarRules(elencoRequisitiGrammaticali);
        clientChatGemini.setTemperature(0.5f);
        for (int i = 0; i < requirements.size(); i++) {
            log("Applico la serie di requisiti n. " + (i + 1) + " ovvero " + requirements.get(i));
            String promptRequisitiGrammaticali = "";

            for (int j = 0; j < requirements.get(i).size(); j++) {
                promptRequisitiGrammaticali += " Requirement n. " + (j+1) + ": " + requirements.get(i).get(j) + ", ";
            }

            storia = sendPromptWithFiles(APPLY_RULES_PROMPT + promptRequisitiGrammaticali, null, null);
            logSuFile(storia);
        }

        //applico gerundio/infinito
        clientChatGemini.setTemperature(0.4f);
        for (int i = 0; i < 2; i++) {
            log("Applico gerundio/infinito per la " + (i + 1) + " volta");
            storia = sendPromptWithFiles(GERUND_INFINITIVE_PROMPT, contentGerundInfinitive, null);
            logSuFile(storia);
        }

        //di nuovo i sinonimi
        log("Aggiungo i sinonimi");
        clientChatGemini.setTemperature(0.4f);
        storia = sendPromptWithFiles(SYNONYMS_PROMPT, contentSinonimi, null);
        logSuFile(storia);

        //miglioro la storia
        /*log("Miglioro la storia");
        clientChatGemini.setTemperature(0.3f);
        storia = sendPromptWithFiles(IMPROVE_STORY_PROMPT + storia, null, null);*/

        // storia = setStoria();

        //estraggo il titolo e lo rimuovo dalla storia
        List<String> righeDellaStoria = suddividiRighe(storia);
        String titolo = righeDellaStoria.remove(0);

        //miglioro il raggruppamento dei paragrafi
        List<String> paragrafi = raggruppaRigheInParagrafiLunghiAlmenoNCaratteri(righeDellaStoria);

        log("Ora chiedo la traduzione dei paragrafi, in totale sono " + paragrafi.size());
        clientChatGemini.setTemperature(0.3f);
        List<String> paragrafiTradotti = traduciParagrafi(paragrafi);
        log("Numero di token richiesti per questa storia: " + clientChatGemini.getTotalTokens());

        String storiaTradottaDaStampare = String.join(LINE_BREAK + LINE_BREAK, paragrafiTradotti);
        String storiaOriginaleDaStampare = String.join(LINE_BREAK + LINE_BREAK, paragrafi);

        log("Creazione del file su Drive contenente la storia creata");
        DocsManager docsManager = googleServicesFactory.getDocsManager();
        docsManager.appendToDocument(DOC_ID_STORIA_CREATA, titolo + LINE_BREAK + LINE_BREAK + storiaTradottaDaStampare + SERIE_DI_LINE_BREAK + titolo + LINE_BREAK + LINE_BREAK + storiaOriginaleDaStampare);

        log("File creato correttamente sul Drive!");
    }

    private static List<String> raggruppaRigheInParagrafiLunghiAlmenoNCaratteri(List<String> righeDellaStoria) {
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
    public static PromptForGenerateText splitByString(String input) {

        int index = input.indexOf(SEPARATORE_PROMPT);
        String left = input.substring(0, index);
        String right = input.substring(index + SEPARATORE_PROMPT.length());
        return new PromptForGenerateText(left, right);
    }

    /**
     * Estrae le regole che iniziano con '*' e le raggruppa in liste di dimensione N.
     */
    public static List<List<String>> groupGrammarRules(String input) {
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

    private static void log(String s) {
        System.out.println(s);
    }

    private static void log(List l) {
        for (int i = 0; i < l.size(); i++) {
            log("Elemento n." + (i + 1) + "\n" + l.get(i));
        }
    }

    private static String stampa(List<String> paragrafi) {
        StringBuilder storia = new StringBuilder();
        paragrafi.forEach(paragrafo -> {
            storia.append(paragrafo).append(LINE_BREAK).append(LINE_BREAK);
        });
        return storia.toString();
    }

    private static List<String> traduciParagrafi(final List<String> paragrafi) throws IOException {

        List<String> paragrafiTradotti = new ArrayList<>();
        int numParagrafo = 1;
        for (String paragrafo : paragrafi) {

            log("Chiedo traduzione del paragrafo n. " + numParagrafo);
            String paragrafoTradotto = sendPromptWithFiles(TRANSLATE_TEXT_PROMPT + paragrafo, null, null);
            String paragrafoPulito = eliminaRigheVuoteEAsterischi(paragrafoTradotto);
            paragrafiTradotti.add(paragrafoPulito);
            numParagrafo++;
        }

        return paragrafiTradotti;
    }

    private static String sendPromptWithFiles(String prompt, byte[] file1, byte[] file2) {

        logSuFile(prompt);

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
            response = sendPromptWithFiles(prompt, file1, file2);
        }
        return response;

    }

    private static void logSuFile(String textToLog) {

        // Definiamo il percorso relativo
        Path dirPath = Path.of(PATH_LOG);

        try (Stream<Path> files = Files.walk(dirPath)) {
            // Se numFile è 0, svuota la cartella prima di iniziare
            if (numFile == 0 && Files.exists(dirPath)) {
                // Seleziona solo i file, evita di eliminare la cartella stessa
                files.filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                System.err.println("Errore eliminazione file: " + file);
                            }
                        });
                System.out.println("Cartella log pulita con successo.");
            }

            numFile++;
            String fileName = "log_" + numFile + ".txt";
            Path filePath = dirPath.resolve(fileName);

            // 2. Scrive il file (sovrascrive se esiste, lo crea se manca)
            Files.writeString(filePath, textToLog, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Log salvato con successo: " + filePath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio del log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static @NonNull String eliminaRigheVuoteEAsterischi(String paragrafoAllungato) {
        return paragrafoAllungato.lines()
                .filter(line -> !line.isBlank())
                .map(p -> p.replace("*", ""))
                .collect(Collectors.joining(LINE_BREAK));
    }

    private static List<String> suddividiRighe(String risposta) {
        risposta = risposta.replace("*", "");
        return Stream.of(risposta.split(LINE_BREAK)).filter(r -> !r.isBlank()).collect(Collectors.toList());
    }

    private static String setStoria() {
        return "**The Unveiling of Aethel's Truth**\n" +
                "\n" +
                "The old map, brittle and yellowed, **lay** spread across Dr. Aris Thorne's desk, its faded lines hinting at forgotten paths and hidden wonders. Aris, a man **whose** entire professional life revolved around the pursuit of the past, felt a peculiar sense of anticipation, a low, persistent hum of excitement that **had been definitely increasing** in the quiet solitude of his study. **In times long past**, he **used to** spend countless hours in this very room, poring over ancient texts. He loved ancient history, **especially** forgotten myths, and often **would wish** he **could have lived** in those ancient times to witness them firsthand. Many of his colleagues felt the same, and he **too** often expressed this wish. He **was about to** embark on something monumental, a discovery that **was certainly going to overcome** the dry theories of his peers and perhaps rewrite a chapter of history. His research, a tireless **effort of many years**, **had been leading** him to believe in the existence of the Relic of Aethel, an artifact shrouded in myth, **which** was said to **possess** the power to reveal truths, **of which** **many** **had long been certainly hidden**. He felt that such a discovery, if real, would be **worth** more than any worldly treasure.\n" +
                "\n" +
                "Day after day **kept on passing by** as Aris meticulously cross-referenced ancient texts; he **would often find** his eyes drawn to the cryptic symbols on the map. He could **just barely** decipher the faint, ancient script in some sections, and often **wished** he **had had** a clearer version of the map. **However**, the promise of what **lay** beyond kept him striving. One stormy evening, a flash of lightning briefly illuminated a crucial passage that **had been overlooked** countless times by him. \"The Ascent of the Silent Peaks,\" it read, a name that sent a chill down his spine. **Never before had such a chill been felt** by Aris, a premonition that this arduous journey **would be** fraught with **much** peril. He knew this expedition **certainly could have taken** a lot longer than it **should have** if he **hadn't had** his gear meticulously **checked** before setting out. Aris **couldn't help but** begin to **probably feel like** he **was being watched**, a strange intuition urging him forward, though he **certainly wished** he **wouldn't feel** so exposed. He **still** had so much intricate preparation to complete, knowing every detail was **worth** his attention.\n" +
                "\n" +
                "He soon found himself at the foot of the Silent Peaks, a majestic, formidable mountain range **whose** jagged, dark summits **were piercing** the hazy sky. A narrow, winding river, **neither** too wide **nor** too narrow, **was flowing** **nearby**, its gentle rush the only sound. **Furthermore**, the air grew **increasingly** thinner with every step, and he **was getting used to** the crisp, biting cold atmosphere as the landscape **was becoming** more **picturesque**, a rich tapestry of ancient, gnarled pines and moss-covered, grey boulders. His **anxiety** was **no longer** a crippling force, but his **will** to uncover the relic was stronger. As he ascended, old tales **probably seemed to be whispered** by the wind, and the sheer scale of the mountains **would always make** him feel **rather** insignificantly small. He **couldn't help but gaze** at the sprawling valleys below, and **would have liked** to pause longer, to truly **get** his memories of the beauty **for which** he deeply craved to have captured. He **certainly didn't feel like** rushing this tranquil part of the journey **just yet**, believing the experience itself was **worth** savoring.\n" +
                "\n" +
                "After several strenuous days, Aris stumbled upon a narrow, almost invisible crevice, **in which** the entrance was hidden, **which** **had been even almost completely concealed** by hanging vines. He had to **bend** low to enter, his backpack **was scraping** against the rough rock. **Nevertheless**, he **could have chosen** to turn back, but his resolve was too strong. Inside, the **dimness** was **rather** profound, **only broken** by the **faintly** glowing, bioluminescent lichen on the damp walls. His lantern **was ignited**, its **beam** cutting through the oppressive gloom. He **couldn't help but wish** he **had remembered** to **get** his lantern **serviced** before the trip; it flickered ominously. It was **no longer** reliable. **As well as that**, he could **just** hardly even believe his luck. This **must certainly have been** the entrance. He felt a strong **craving** to push forward, but caution made him **pause**. He wondered if that was truly the right way, **to which** he was committed, and what **would happen** if that path **led** nowhere. He asked himself whether he **should have double-checked** his notes before entering, and **for what reason** he might have neglected such a detail. He **wished** he **were** more certain, but he **always used to** rely on his instincts in such situations, and they **were still** urging him forward. He **no longer** felt completely certain, but he pressed on, convinced the potential discovery was **worth** the risk.\n" +
                "\n" +
                "He pressed on, the passage descending steeply. The air **would certainly grow** heavy, almost making it hard to **breathe**. Suddenly, **even** as he stepped, the ground beneath his feet **had given way** with a sickening, ominous **crunch**. **Therefore**, a protruding, jagged rock **was instinctively grasped** by him, his heart **racing**, and a cold knot of fear **rose** in his throat. He **couldn't help but** exclaim that it had been close, his **breath** hitched in his throat. He **could have fallen** if he **hadn't instinctively grasped** that rock, and he **was supposed to avoid** such dangerous situations. So close was he to falling apart, he was **definitely nearly about to fall apart**. **However**, he steadied himself, deciding that the expedition **couldn't just be left** by him because of a mere scare. He **would have liked** to leave, but he knew he **couldn't**. It **was known** to him that he **must** be **patient**, and he **couldn't help but** remind himself of his goal, **determined to prevent** *either* further mishaps *or* a premature end **to which** he was opposed. He didn't want to fail, and **neither did** his research, which he believed was **worth** protecting.\n" +
                "\n" +
                "Further in, the tunnel opened into a vast, echoing cavern, **in which** a natural spring **was feeding** a crystal-clear, serene pool in the center. Its surface **was shimmering** with an ethereal **moonlight** that **was filtered** through a distant, high opening, **the exact location of which** he couldn't discern. But what truly captured his attention was a structure on the far side: a small, intricately carved ancient shrine, its stone **being covered** by intricate carvings. He **couldn't help but stare** at the symbols, realizing their meaning **had already been made familiar** to him from **a lot of** his texts. **Given that** he had translated them, he felt a deep connection. He exclaimed that it was he who had translated those very glyphs, a triumphant grin spreading across his face. He **certainly couldn't help but wish** he **could have shared** this profound moment of triumph with his colleagues, explaining its immense **worth**.\n" +
                "\n" +
                "As he approached the shrine, a tall, gaunt figure **was rather slowly emerging** from the deep shadows, **who was clad** in dark, flowing, ceremonial robes. Aris **froze**, his every muscle tensing. He **couldn't help but** demand who the figure was and **to whom** he was speaking. He also asked what the figure was doing there, and **for what purpose**. He **wished** the figure **would answer** him directly. He **could no longer** control his rising fear, and **couldn't just make** himself move.\n" +
                "\n" +
                "The figure stepped into the soft **glow**, revealing a face that **had been deeply etched** with age, yet with eyes that **glittered** with an unsettling, piercing intensity. The figure said he was Kael, keeper of that place, his voice surprisingly soft. Kael stated that he **had certainly been expecting** Aris and **had wanted** to meet him for a very long time. He added that Aris **was probably supposed to** arrive **much** earlier, **for which** they had been waiting, but **nevertheless**, he was there.\n" +
                "\n" +
                "Aris was taken aback. He asked how **on earth** Kael could possibly have known, inquiring if there was **much** magic involved, or **to what** he owed such foresight. He **definitely couldn't help but wish** he **understood** the extent of Kael's knowledge. He admitted that he didn't understand it, **either**.\n" +
                "\n" +
                "Kael replied that Aris's arrival **had been foretold** by the prophecy. He then asked if Aris sought the Relic of Aethel, and if he understood what he **coveted**, warning that he **might even** be surprised by its true nature, **for which** he **must** be prepared. \"Is this knowledge truly **worth** the pursuit?\" Kael inquired.\n" +
                "\n" +
                "Aris nodded slowly, saying he believed he did. He explained that it was a key to ancient knowledge, a way **by which** ancient truths **might** be revealed, **for which** he had dedicated his life. **Therefore**, he stated he **must** proceed. He **was certainly supposed to bring** **much** of this invaluable knowledge back to the world, for its intrinsic **worth**.\n" +
                "\n" +
                "Kael questioned what if those truths **were far too much** to bear. He added that many **more** unfortunate individuals **probably could have avoided** spiritual shattering if they **hadn't tried** to harness its power recklessly. He advised Aris that he **should rather have put out** the gnawing fear in his heart before coming that far, explaining that the sun **used to** **raise** their hopes, but they **would lay** them down in despair. He stressed that Aris **definitely must** learn from their tragic mistakes, concluding that he didn't want to share their fate, and **neither did** Aris, he imagined, as it was **not worth** the eternal torment.\n" +
                "\n" +
                "Aris stated firmly that he was aware of the risks but could not **just** **walk away** then, not after all that time. He pondered what **would befall** him if he **abandoned** that quest, adding that he **would have regretted** it for the rest of his entire life if he **had abandoned** it then. **Despite the many risks**, he **certainly must** see this through, for the ultimate revelation would be **worth** it.\n" +
                "\n" +
                "Kael explained that the path to the relic was fraught with choices. He said that the ancient relic itself was **neither** hidden **nor** easily accessible; it was protected, **for which** **a great deal of** protective effort had been required. He added that he **would have liked** to simply hand it over, but Aris's inherent **ability** to wield its profound knowledge responsibly **definitely must be proven**. He declared that he **couldn't stand** to see **one more** soul **being corrupted** by its dark influence. **What's more**, he stated he could not allow that immense power to fall into the wrong hands. He explained that he **was supposed to** **prevent** that, **for which** he had been appointed guardian, as the relic's true **worth** lay in its careful stewardship.\n" +
                "\n" +
                "Kael gestured towards the shrine, revealing that that was the true core. He explained that the actual relic, a fragment of ancient consciousness, **which** was housed within that fragile vessel, **must** be reverently touched to awaken it. But he warned Aris that its awakening **was going to cause** a seismic shift in that cavern and that the narrow passage **through which** he had entered **is already definitely about to collapse**. He concluded that **even if** Aris was ready, the way back would be cut off.\n" +
                "\n" +
                "Aris looked from Kael to the glowing stone, then back, asking if he had to choose knowledge but no way back. **About this momentous decision**, he **certainly wished** he **didn't have too much** of a difficult choice, wondering if the knowledge was **worth** the isolation.\n" +
                "\n" +
                "\"Exactly,\" Kael affirmed. \"For the first time in weeks, you **have been brought** face to face with the ultimate decision. Is the profound truth **even** worth the increasingly steep price of isolation? Remember, that's certainly not how it had gone for the others. They sought power. You **rather** seek genuine understanding. Given that you are here, you must understand the gravity of this. Be patient, and aim to keep this invaluable knowledge secure for the future. Be safe.\"\n" +
                "\n" +
                "Aris took a deep, fortifying **breath**, **approaching** the pedestal. He reached out, his fingers **were tingling** with a strange energy as contact **was made** with the warm, smooth stone. He **was just about to be completely dazzled** by an intense wave of light that **surged** from the relic. An increasingly immense, overwhelming influx of knowledge, images, and forgotten languages **was filling** his mind, **expanding** his **consciousness** beyond anything he **had ever known**. The vast cavern began to **violently tremble**, dust and small rocks **were falling** from the ceiling. **As if** the world itself was protesting, the ground **was felt to shake**, and the ominous sound of cracking stone **kept on increasing more and more** in volume. He **certainly couldn't help but wish** the ground **would stop shaking** so violently. The exit path **was about to collapse**, just as Kael **had already warned**. He **definitely must** **make** a decisive move **right away**, for the knowledge he was receiving was **worth** any personal cost.\n" +
                "\n" +
                "\"Go!\" Kael shouted over the roar of falling rock. \"The falling, jagged debris **must be evaded** by you, **for which** you **must** act quickly! You **definitely could have been crushed** if you **had stayed** there **any longer**! There is another way out, through the spring! It's me who **is certainly about to slow down** the rapid collapse, **just** to **let** you pass. I won't abandon you, and **neither should** you abandon yourself.\"\n" +
                "\n" +
                "Aris, now **having been imbued** with an unsettling, profound clarity, understood instantly. He scrambled towards the pool, **being helped** by the newly acquired knowledge to assess the fastest route. A desperate, plunging **leap** **was made** into the dark water, **even** as a massive, ancient boulder, **the sheer force of which** then **slammed** into the spot where he **had just been standing**, effectively **blocking** his original, now-blocked entry point. He **certainly could have waited** **a lot** longer, but that **would have meant** certain doom. The pool wasn't deep, but it was a narrow, twisting subterranean channel, **through which** he **was propelled** upwards. He **was propelled** through the watery darkness, **being guided** by his new insights through the twists and turns of the underground river. He emerged, gasping for breath, into a secluded, verdant grotto, the open sky a welcoming sight above. He **still definitely couldn't help but wish** he **had had** more time to recover before facing what **lay** ahead, **the unknown challenges of which** he could only guess. He felt he **could no longer** ignore his deep physical exhaustion.\n" +
                "\n" +
                "**Eventually**, he sat on the damp, cool earth, exhausted, yet exhilarated. The ancient, powerful relic, now **having become** a part of him, **was resonating** with a gentle, internal **gleam** within his mind. Kael **had gone**, **probably** having been trapped or **perhaps** having used his own mysterious means to escape, a sudden fact **for which** Aris felt a sharp pang of regret. **However**, Aris **definitely couldn't help but wish** Kael **were** still there so he **could have properly thanked** him. The **picturesque**, natural surroundings **were noted** by Aris as he looked around. He **rarely**, if ever, saw such breathtakingly picturesque surroundings, **the sheer beauty of which** was **rather** overwhelming. **As well as that**, the sun, a **brilliance** in the vast sky, **was felt** like a new dawn. It **was definitely known** to him that his entire life **would never be** the same **From now on**. He **certainly would have liked** to believe his old, academic life **could have continued**, but he knew it **couldn't** **any longer**. A journey **had been embarked upon** by him, hoping to **uncover** history, and instead, he **had become** a part of it. **Therefore**, its living, ancient knowledge **was always** being carried within him, a heavy burden **to which** he was **just** getting **used to**. The world **had now become** an **increasingly complex**, profound tapestry of deeper meanings, and he **was certainly about to** do something truly extraordinary with his newfound, ancient wisdom. His understanding **had been profoundly altered**, his perception **was no longer** limited by what it **should have been**, but **rather** expanded far beyond measure. The legacy of Aethel **was carried** by him, **neither** as a mere object of study **nor** a forgotten tale, but as a living, vital truth, the immense weight **of which** he was now responsible for, **ready to be shared**, carefully and patiently, with a world that **had been speculating** its profound secrets, and **to which** he **was supposed to** **bring** this truth. He would **no longer** be **just** a conventional archaeologist, but **rather** something **much more** profound: a living bridge to the past, a role he now knew was **worth** every sacrifice.";
    }
}
