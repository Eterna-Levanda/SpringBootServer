package and.learn.ai.englishtextgenerator;

import and.learn.ai.englishtextgenerator.behaviour.ChatGeminiBehaviour;
import and.learn.ai.englishtextgenerator.behaviour.ChatGeminiBehaviourSwitcher;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.log4j.Log4j2;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Log4j2
public class EnglishTextGeneratorMain {
    // Caricamento della chiave API e del Service Account
    public static final String API_KEY;
    public static final String URL_API_GEMINI;
    public static final double TEMPERATURE = 0.5;
    public static final String GEMINI_VERSION = "gemini-flash-latest";
    private static final String SERVICE_ACCOUNT_FILE = "src/main/resources/ai/config/englishtextgenerator/service-account.json";
    // ID dei tuoi Google Docs
    private static final String DOC_ID_PROMPT;
    private static final String DOC_ID_ERRORI_FREQUENTI;
    private static final String DOC_ID_SINONIMI;

    public static final String APPLICATION_PDF = "application/pdf";

    static {
        //inizializzazione delle costanti
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

        URL_API_GEMINI = "https://generativelanguage.googleapis.com/v1beta/models/"+GEMINI_VERSION+":generateContent?key="
                + API_KEY;

    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        // Inizializzazione Servizio Drive
        Drive driveService = getDriveService();

        // Esportazione dei documenti Drive (Operazione specifica per file nativi)
        String prompt = extractNativeDocText(driveService, DOC_ID_PROMPT);
        byte[] contentSinonimi = exportNativeDoc(driveService, DOC_ID_SINONIMI);
        byte[] contentErroriFrequenti = exportNativeDoc(driveService, DOC_ID_ERRORI_FREQUENTI);

        System.out.println("Documenti Drive scaricati con successo. Ora verrà generato il testo con Gemini.");

        long start = System.currentTimeMillis();
        // Chiamata a Gemini
        String risposta = ChatGeminiBehaviourSwitcher.getInstance(ChatGeminiBehaviour.UPLOAD_FILES_API)
                .chiamaGemini(contentSinonimi, contentErroriFrequenti, prompt);
        long end = System.currentTimeMillis();
        //System.out.println("Tempo impegato in millisecondi: " + (end - start));
        System.out.println(risposta);
    }


    private static Drive getDriveService() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_FILE))
                .createScoped(Collections.singleton(DriveScopes.DRIVE_READONLY));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Gemini-Doc-Integrator")
                .build();
    }

    /**
     * Estrae il contenuto di un Google Doc nativo.
     * I documenti nativi NON supportano il metodo 'get' per il download del file binario.
     * Devono essere esportati specificando un MIME type di destinazione.
     *
     * @param driveService Il servizio Drive già autorizzato.
     * @param fileId       L'ID del documento (estratto dall'URL).
     * @return Stringa contenente il testo del documento.
     * @throws IOException In caso di problemi di rete o permessi.
     */
    private static String extractNativeDocText(Drive driveService, String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Esportiamo il file in formato testo semplice (text/plain).
        // Se preferissi mantenere la formattazione per Gemini, potresti usare "application/pdf".
        driveService.files().export(fileId, "text/plain")
                .executeMediaAndDownloadTo(outputStream);

        // Restituisce il contenuto come stringa UTF-8
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    /**
     * Scarico il file da Drive
     */
    private static byte[] exportNativeDoc(Drive service, String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Per i file nativi Google, l'API 'export' è l'unica via per ottenere il contenuto
        service.files().export(fileId, APPLICATION_PDF)
                .executeMediaAndDownloadTo(outputStream);

        return outputStream.toByteArray();
    }
}
