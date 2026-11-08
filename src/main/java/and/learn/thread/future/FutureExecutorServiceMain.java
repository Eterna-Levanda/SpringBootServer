package and.learn.thread.future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import lombok.extern.log4j.Log4j2;

/**
 * Esempio d'uso dell'interfaccia Future con ExecutorService (approccio "classico", pre CompletableFuture).
 *
 * Flusso:
 * 1) creo un ExecutorService con un pool di thread
 * 2) sottometto (submit) un Callable per ogni input: il thread PARTE SUBITO (appena c'è un thread libero nel pool)
 *    e submit restituisce immediatamente un Future, cioè un "segnaposto" del risultato futuro
 * 3) ciclo sui Future e con get() attendo/recupero il risultato di ognuno
 * 4) chiudo l'ExecutorService
 *
 * Differenza importante rispetto a quanto si potrebbe pensare: NON è get() che fa partire i thread,
 * get() serve solo ad aspettare la fine del thread e a leggerne il risultato.
 * Mentre aspetto il primo Future, gli altri thread stanno già lavorando in parallelo.
 */
@Log4j2
public class FutureExecutorServiceMain {

    private static final int THREAD_DA_CREARE = 5;
    private static final int NUM_THREAD_IN_PARALLELO = 3;
    private static final long TIMEOUT_SECONDI = 300;

    public static void main(String[] args) {

        //due modi diversi per eseguire i thread: lanciare i thread uno alla volta o tutti assieme
        eseguiThreadConSubmit();

        eseguiThreadConInvokeAll();
    }

    private static void eseguiThreadConSubmit() {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD_IN_PARALLELO);
        //lista contenente i risultati
        List<Future<NumeroIntero>> listaFuture = new ArrayList<>();
        List<NumeroIntero> risultati = new ArrayList<>();

        try {
            // 1) sottometto i task: ogni submit avvia (o mette in coda) il thread e restituisce subito un Future
            for (int input = 1; input <= THREAD_DA_CREARE; input++) {
                CalcolaDoppioCallable callable = new CalcolaDoppioCallable(input);
                /*consegno (sottometto) i Callable all'ExecutorService uno alla volta (nell'altro esempio invece tutti assieme)
                In questa fase i future non contengono ancora i risultati, sono solo dei contenitori vuoti
                 */
                Future<NumeroIntero> futureConIlRisultato = executor.submit(callable);
                listaFuture.add(futureConIlRisultato);
            }
            log.info("Tutti i task sono stati sottomessi, il main prosegue senza aspettare");

            // 2) recupero i risultati: get() BLOCCA il thread main finché quel Future non è completato
            for (Future<NumeroIntero> future : listaFuture) {
                try {
                    /* get con timeout: se il risultato non arriva entro il tempo indicato lancia TimeoutException.
                       Esiste anche get() senza parametri che aspetta all'infinito */
                    NumeroIntero doppio = future.get(TIMEOUT_SECONDI, TimeUnit.SECONDS);
                    risultati.add(doppio);
                    log.info("Risultato ricevuto dal thread: {} = {}", doppio.getNomeThread(), doppio.getNumero());
                } catch (ExecutionException e) {
                    // il Callable ha lanciato un'eccezione: la causa originale è in getCause()
                    log.error("Errore nel thread: {}", e.getCause().getMessage());
                } catch (TimeoutException e) {
                    log.error("Timeout superato, annullo il task");
                    // true = interrompe il thread se è in esecuzione
                    future.cancel(true);
                }
            }
        } catch (InterruptedException e) {
            // il thread main è stato interrotto mentre aspettava in get(): ripristino il flag di interruzione
            Thread.currentThread().interrupt();
        } finally {
            // 3) chiusura dell'executor: senza shutdown i thread del pool restano vivi e la JVM non termina
            chiudiExecutor(executor);
        }

        log.info("Risultati finali ({}/{}): {}", risultati.size(), THREAD_DA_CREARE, risultati);
    }

    /**
     * Alternativa più compatta: invokeAll sottomette tutti i Callable e ATTENDE che siano tutti terminati,
     * restituendo la lista dei Future già completati (get() quindi non blocca più).
     * Con il timeout, i task non terminati in tempo vengono cancellati.
     */
    private static void eseguiThreadConInvokeAll() {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD_IN_PARALLELO);
        try {

            //creo una lista di Callable, ognuno di loro ha in input il numero di cui calcolare il doppio
            List<Callable<NumeroIntero>> callables = new ArrayList<>();
            for (int input = 1; input <= THREAD_DA_CREARE; input++) {
                callables.add(new CalcolaDoppioCallable(input));
            }

            /* consegno all'ExecutorService i Callable tutti assieme (nell'altro metodo li consegno/submit uno alla volta)
                e ottengo una lista di Future. Quindi input e output è di tipo List*/
            List<Future<NumeroIntero>> listaFuture = executor.invokeAll(callables, TIMEOUT_SECONDI, TimeUnit.SECONDS);

            for (Future<NumeroIntero> future : listaFuture) {
                if (future.isCancelled()) {
                    log.error("Task cancellato per timeout");
                    continue;
                }
                try {
                    log.info("Risultato ricevuto dal thread: {} = {}", future.get().getNomeThread(), future.get().getNumero());
                } catch (ExecutionException e) {
                    log.error("Errore nel thread: {}", e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            chiudiExecutor(executor);
        }
    }

    /**
     * Chiusura "pulita" dell'ExecutorService:
     * shutdown() non accetta nuovi task ma lascia finire quelli in corso,
     * awaitTermination aspetta al massimo il tempo indicato,
     * shutdownNow() interrompe forzatamente i thread ancora attivi.
     */
    private static void chiudiExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
