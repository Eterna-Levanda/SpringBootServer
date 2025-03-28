package and.learn.retryable;

import lombok.extern.log4j.Log4j2;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

/**Classe con un singolo metodo di retry e di recovery.
 * Il metodo @Recovery deve avere in input la stessa eccezione del metodo @Retry,
 * infatti se il metodo @Retry lancia l'eccezione X, verrà chiamato il metodo @Recover che prende in input l'eccezione X.
 *
 * Se hai più metodi @Retry che lanciano la stessa eccezione e vuoi catturarli in metodi diversi,
 * allora si devono distinguere dai parametri in input e quegli stessi parametri in input devono esserci anche nel metodo di @Recover associato. */
@Log4j2
@Service
public class RetryableService {

    static final int MAX_ATTEMPTS = 3;

    /**
     * @param successoAllNesimoTentativo specifica a quale tentativo il metodo termina con successo.
     *                                   Se successoAllNesimoTentativo = 1 terminerà al primo tentativo, senza eccezioni
     *                                   Se successoAllNesimoTentativo > 1 ma < MAX_ATTEMPTS il metodo terminerà con successo ma prima lancerà successoAllNesimoTentativo-1 eccezioni
     *                                   Se successoAllNesimoTentativo > MAX_ATTEMPTS, il metodo non terminerà con successo e dopo MAX_ATTEMPTS tentativi verrà richiamato il metodo recover
     *
     * @Retryable definisce i dettagli della politica di retry.
     *      value: definisce per quale eccezione dev'essere effettuato un retry. Se viene lanciata un'eccezione diversa, il metodo termina
     *      maxAttempts: massimo numero di tenativi per rieseguire il metodo. Superato quello, al posto di rieseguire il metodo viene invocato il metodo recover avente in input la stessa eccezione e, se necessario, anche gli stessi parametri in input al metodo fallito.
     *      backOff:
     *          delay: dopo quanti millisecondi effettuare il tentativo successivo
     *          multiplier: fattore di moltiplicazione del tempo di delay dopo ogni tentativo
     *          maxDelay: limite massimo del delay moltiplicato, per non esagerare con l'attesa
     * */
    @Retryable(value = IllegalArgumentException.class, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000))
    public String retryableMethod(int successoAllNesimoTentativo) {

        // Ottiene il contesto del retry
        RetryContext context = RetrySynchronizationManager.getContext();

        // Tentativo attuale (parte da 1)
        int attempt = context.getRetryCount() + 1;
        log.info("⏳ Tentativo n." + attempt);

        if (successoAllNesimoTentativo > attempt) {
            String message = "❌ Errore nella chiamata API!";
            log.error(message);
            throw new IllegalArgumentException(message);
        }

        String msg = "✅ API terminata con successo al tenativo n. " + attempt;
        log.info(msg);
        return msg;
    }

    /**Metodo eseguito quando sono esauriti i tentativi di retry del metodo sopra.
     * @param successoAllNesimoTentativo parametro opzionale perchè l'associazione tra i due metodi è sufficiente farla tramite l'eccezione.
     *                                   Il parametro è lo stesso passato in input al metodo di @Retry*/
    @Recover
    public String recover(IllegalArgumentException ex, int successoAllNesimoTentativo) {

        String s = "Metodo fallito nonostante i " + MAX_ATTEMPTS + " tentativi. Si voleva farlo funzionare al "+successoAllNesimoTentativo+"°";
        log.error(s);
        return s;
    }
}
