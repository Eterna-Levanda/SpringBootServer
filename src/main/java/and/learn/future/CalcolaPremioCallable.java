package and.learn.future;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Callable = task eseguibile in un thread che RESTITUISCE un valore (a differenza di Runnable)
 * e che può lanciare eccezioni checked.
 * Il valore restituito dal metodo call() sarà recuperabile tramite Future.get().
 */
public class CalcolaPremioCallable implements Callable<DatiPolizza> {

    //vale true se vuoi simulare un errore casuale dentro il thread
    private static final boolean SIMULA_ERRORE = false;

    private final int numeroProposta;

    public CalcolaPremioCallable(int numeroProposta) {
        this.numeroProposta = numeroProposta;
    }

    @Override
    public DatiPolizza call() throws Exception {
        Random random = new Random();
        int durata = random.nextInt(3000); // simula un calcolo che dura fino a 3 secondi
        System.out.println("Inizio calcolo proposta " + numeroProposta + " (" + durata + " ms) sul thread " + Thread.currentThread().getName());

        /* Thread.sleep lancia InterruptedException: se il thread viene interrotto
           (es. future.cancel(true) o executor.shutdownNow()) l'eccezione esce da call()
           e chi fa future.get() riceverà una ExecutionException */
        Thread.sleep(durata);

        if (SIMULA_ERRORE && random.nextBoolean()) {
            throw new IllegalStateException("Errore nel calcolo del premio della proposta " + numeroProposta);
        }

        BigDecimal premio = BigDecimal.valueOf(100 + random.nextDouble() * 900).setScale(2, RoundingMode.HALF_UP);
        System.out.println("Fine calcolo proposta " + numeroProposta + " sul thread " + Thread.currentThread().getName());
        return new DatiPolizza(numeroProposta, premio, Thread.currentThread().getName());
    }
}
