package and.learn.ai.englishtextgenerator.googleservice;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**Classe che generalizza i manager dei servizi Google (Drive, Docs...)
 * Il generico T rappresenta la classe delle API Google del servizio specifico
 * */
public abstract class AbstractGoogleMananger<T> {

    protected T serviceClient;

    protected AbstractGoogleMananger(GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        serviceClient = createService(credentials);
    }

    protected abstract T createService(GoogleCredentials credentials) throws GeneralSecurityException, IOException;

}
