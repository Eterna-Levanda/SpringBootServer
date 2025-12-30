package and.learn.ai.englishtextgenerator.drive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.APPLICATION_PDF;

/**
 * Classe con metodi statici che si interfaccia con Google Drive
 */
public class DriveManager {

    public static final String SERVICE_ACCOUNT_FILE = "src/main/resources/ai/config/englishtextgenerator/service-account.json";

    Drive driveService;

    public DriveManager() throws GeneralSecurityException, IOException {
        driveService = getDriveService();
    }

    public Drive getDriveService() throws GeneralSecurityException, IOException {
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
     * @param fileId       L'ID del documento (estratto dall'URL).
     * @return Stringa contenente il testo del documento.
     * @throws IOException In caso di problemi di rete o permessi.
     */
    public String extractNativeDocText(String fileId) throws IOException {
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
    public byte[] exportNativeDoc(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Per i file nativi Google, l'API 'export' è l'unica via per ottenere il contenuto
        driveService.files().export(fileId, APPLICATION_PDF)
                .executeMediaAndDownloadTo(outputStream);

        return outputStream.toByteArray();
    }
}
