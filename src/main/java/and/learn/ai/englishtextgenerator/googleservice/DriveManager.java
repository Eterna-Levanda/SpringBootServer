package and.learn.ai.englishtextgenerator.googleservice;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import static and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain.APPLICATION_PDF;

/**
 * Classe con metodi statici che si interfaccia con Google Drive
 */
@Getter
public class DriveManager extends AbstractGoogleMananger<Drive> {

    public DriveManager(GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        super(credentials);
    }

    @Override
    protected Drive createService(GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        HttpCredentialsAdapter authInitializer = new HttpCredentialsAdapter(credentials);
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> {
                    authInitializer.initialize(request);
                    request.setConnectTimeout(100000);
                    request.setReadTimeout(100000);
                })
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
        serviceClient.files().export(fileId, "text/plain")
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
        serviceClient.files().export(fileId, APPLICATION_PDF)
                .executeMediaAndDownloadTo(outputStream);

        return outputStream.toByteArray();
    }
}
