package and.learn.retryable;

import lombok.extern.log4j.Log4j2;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

/*
 *  Service dove hai 2 metodi di retry che lanciano la stessa eccezione.
 *  In questo caso hai un solo metodo di @recovery perchè entrambi verranno gestiti da quel metodo.
 *  Se vuoi averne 2 separati devono avere input diversi, sia nel metodo che nel recovery, per poterli distinguere
 * */
@Log4j2
@Service
public class RetryableDoppioMetodoUnRecoverService {

    static final int MAX_ATTEMPTS = 3;

    @Retryable(retryFor = IllegalArgumentException.class, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = 2000))
    public String retryableMethod1() {
        int attempt = RetrySynchronizationManager.getContext().getRetryCount() + 1;
        log.info("⏳ Tentativo n." + attempt);
        throw new IllegalArgumentException();
    }

    @Retryable(retryFor = IllegalArgumentException.class, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = 2000))
    public String retryableMethod2() {
        int attempt = RetrySynchronizationManager.getContext().getRetryCount() + 1;
        log.info("⏳ Tentativo n." + attempt);
        throw new IllegalArgumentException();
    }

    @Recover
    public String recover(IllegalArgumentException ex) {
        String s = "Metodo fallito nonostante i " + MAX_ATTEMPTS + " tentativi";
        log.error(s);
        return s;
    }
}
