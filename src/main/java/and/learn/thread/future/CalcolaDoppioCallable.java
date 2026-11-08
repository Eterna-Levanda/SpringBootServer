package and.learn.thread.future;

import lombok.extern.log4j.Log4j2;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Callable = task eseguibile in un thread che RESTITUISCE un valore (a differenza di Runnable)
 * e che può lanciare eccezioni checked.
 * Il valore restituito dal metodo call() sarà recuperabile tramite Future.get().
 */
@Log4j2
public class CalcolaDoppioCallable implements Callable<NumeroIntero> {

    //vale true se vuoi simulare un errore casuale dentro il thread
    private static final boolean SIMULA_ERRORE = false;

    private final int numeroDaRaddoppiare;

    public CalcolaDoppioCallable(int numeroDaRaddoppiare) {
        this.numeroDaRaddoppiare = numeroDaRaddoppiare;
    }

    @Override
    public NumeroIntero call() throws Exception {
        Random random = new Random();
        int durata = random.nextInt(3000); // simula un calcolo che dura fino a 3 secondi
        log.info("Inizio calcolo del doppio di " + numeroDaRaddoppiare + " (" + durata + " ms di calcolo) sul thread " + Thread.currentThread().getName());

        /* Thread.sleep lancia InterruptedException: se il thread viene interrotto
           (es. future.cancel(true) o executor.shutdownNow()) l'eccezione esce da call()
           e chi fa future.get() riceverà una ExecutionException */
        Thread.sleep(durata);

        if (SIMULA_ERRORE && random.nextBoolean()) {
            throw new IllegalStateException("Errore nel calcolo del doppio di " + numeroDaRaddoppiare);
        }

        //Calcolo il doppio
        BigInteger doppio = new BigInteger("" + (numeroDaRaddoppiare * 2));

        log.info("Fine calcolo del doppio " + numeroDaRaddoppiare + " sul thread " + Thread.currentThread().getName());
        return new NumeroIntero(doppio, Thread.currentThread().getName());
    }
}
