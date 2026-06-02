package and.learn.ai.englishtextgenerator.googleservice;

import and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain;
import and.learn.ai.englishtextgenerator.EnglishTextGeneratorService;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Getter
public class GoogleServicesFactory {

    public static final String SERVICE_ACCOUNT_FILE = "src/main/resources/ai/englishtextgenerator/config/service-account.json";
    public static final String SERVICE_ACCOUNT_FILE_GCLOUD = "/secret/service-account.json";

    private final DriveManager driveManager;
    private final DocsManager docsManager;

    public GoogleServicesFactory() throws IOException, GeneralSecurityException {

        // 1. Definiamo gli scope necessari per tutta l'applicazione.
        // Se devono essere utilizzati altri servizi, aggiungerli alla lista
        List<String> ALL_SCOPES = Arrays.asList(
                DriveScopes.DRIVE_READONLY,  // Permessi per Drive, per accedere solo ai file condivisi con l'app
                DocsScopes.DOCUMENTS    // Permessi per Docs
        );

        // 2. Carichiamo le credenziali una volta sola, scegliendo dinamicamente il file in base all'ambiente di esecuzione (locale o GCloud)
        String pathToServiceAccount = EnglishTextGeneratorService.GOOGLE_CLOUD_ENABLED ? SERVICE_ACCOUNT_FILE_GCLOUD : SERVICE_ACCOUNT_FILE;
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(pathToServiceAccount))
                .createScoped(ALL_SCOPES);

        // 3. Istanzio i service
        driveManager = new DriveManager(credentials);
        docsManager = new DocsManager(credentials);

    }
}
