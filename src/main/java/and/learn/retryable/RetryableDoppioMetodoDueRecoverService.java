package and.learn.retryable;

import lombok.extern.log4j.Log4j2;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

/*
 *  Service dove hai 2 metodi di retry che lanciano la stessa eccezione.
 *  In questo caso voglio avere 2 metodi di recover, per gestire diversamente i due metodi.
 *  Sono quindi obbligato ad avere input diversi per differenziarli, sia nel metodo Retry sia nel recovery, per poterli distinguere.
 *
 * Se invece i due metodi lanciassero eccezioni diverse, non sarei obbligato a specificare i parametri in input nei metodi recover.
 * */
@Log4j2
@Service
public class RetryableDoppioMetodoDueRecoverService {

    static final int MAX_ATTEMPTS = 3;

    @Retryable(retryFor = IllegalArgumentException.class, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = 2000))
    public String retryableMethod1(int input) {
        int attempt = RetrySynchronizationManager.getContext().getRetryCount() + 1;
        log.info("⏳ Tentativo n." + attempt);
        throw new IllegalArgumentException();
    }

    @Retryable(retryFor = IllegalArgumentException.class, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = 2000))
    public String retryableMethod2(String input) {
        int attempt = RetrySynchronizationManager.getContext().getRetryCount() + 1;
        log.info("⏳ Tentativo n." + attempt);
        throw new IllegalArgumentException();
    }

    @Recover
    public String recover(IllegalArgumentException ex, int input) {
        String s = "Metodo 1 fallito nonostante i " + MAX_ATTEMPTS + " tentativi";
        log.error(s);
        return s;
    }

    @Recover
    public String recover(IllegalArgumentException ex, String input) {
        String s = "Metodo 2 fallito nonostante i " + MAX_ATTEMPTS + " tentativi";
        log.error(s);
        return s;
    }
}
