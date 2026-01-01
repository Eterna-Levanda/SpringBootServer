package and.learn.ai.englishtextgenerator.googleservice;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DocsManager extends AbstractGoogleMananger<Docs> {

    protected DocsManager(GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        super(credentials);
    }

    @Override
    protected Docs createService(GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        return new Docs.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Gemini-Doc-Integrator")//TODO capire cos'è!!
                .build();
    }

    /**
     * Aggiunge testo in fondo a un Google Doc esistente.
     *
     * @param documentId  ID del Google Doc
     * @param textToAppend testo da aggiungere
     */
    public void appendToDocument(
            String documentId,
            String textToAppend) throws IOException {

        // Recupera il documento per conoscere la lunghezza
        Document document = serviceClient.documents()
                .get(documentId)
                .execute();

        int endIndex = document.getBody().getContent()
                .get(document.getBody().getContent().size() - 1)
                .getEndIndex();

        Request insertText = new Request()
                .setInsertText(new InsertTextRequest()
                        .setText(textToAppend)
                        .setLocation(new Location().setIndex(endIndex - 1))
                );

        BatchUpdateDocumentRequest body =
                new BatchUpdateDocumentRequest()
                        .setRequests(List.of(insertText));

        serviceClient.documents()
                .batchUpdate(documentId, body)
                .execute();
    }
}
