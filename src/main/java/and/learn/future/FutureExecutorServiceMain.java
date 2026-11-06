package and.learn.future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
public class FutureExecutorServiceMain {

    private static final int NUM_PROPOSTE = 5;
    //dimensione del pool: se è minore di NUM_PROPOSTE, i task in eccesso aspettano in coda un thread libero
    private static final int NUM_THREAD = 3;
    private static final long TIMEOUT_SECONDI = 300;

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
        List<Future<DatiPolizza>> listaFuture = new ArrayList<>();
        List<DatiPolizza> risultati = new ArrayList<>();

        try {
            // 1) sottometto i task: ogni submit avvia (o mette in coda) il thread e restituisce subito un Future
            for (int input = 1; input <= NUM_PROPOSTE; input++) {
                CalcolaPremioCallable callable = new CalcolaPremioCallable(input);
                Future<DatiPolizza> future = executor.submit(callable);
                listaFuture.add(future);
            }
            System.out.println("Tutti i task sono stati sottomessi, il main prosegue senza aspettare");

            // 2) recupero i risultati: get() BLOCCA il thread main finché quel Future non è completato
            for (Future<DatiPolizza> future : listaFuture) {
                try {
                    /* get con timeout: se il risultato non arriva entro il tempo indicato lancia TimeoutException.
                       Esiste anche get() senza parametri che aspetta all'infinito */
                    DatiPolizza dp = future.get(TIMEOUT_SECONDI, TimeUnit.SECONDS);
                    risultati.add(dp);
                    System.out.println("Risultato ricevuto: " + dp);
                } catch (ExecutionException e) {
                    // il Callable ha lanciato un'eccezione: la causa originale è in getCause()
                    System.err.println("Errore nel thread: " + e.getCause().getMessage());
                } catch (TimeoutException e) {
                    System.err.println("Timeout superato, annullo il task");
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

        System.out.println("Risultati finali (" + risultati.size() + "/" + NUM_PROPOSTE + "): " + risultati);

        //esempio alternativo con invokeAll, decommenta per provarlo
        //esempioInvokeAll();
    }

    /**
     * Alternativa più compatta: invokeAll sottomette tutti i Callable e ATTENDE che siano tutti terminati,
     * restituendo la lista dei Future già completati (get() quindi non blocca più).
     * Con il timeout, i task non terminati in tempo vengono cancellati.
     */
    private static void esempioInvokeAll() {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
        try {
            List<Callable<DatiPolizza>> callables = new ArrayList<>();
            for (int input = 1; input <= NUM_PROPOSTE; input++) {
                callables.add(new CalcolaPremioCallable(input));
            }

            List<Future<DatiPolizza>> listaFuture = executor.invokeAll(callables, TIMEOUT_SECONDI, TimeUnit.SECONDS);

            for (Future<DatiPolizza> future : listaFuture) {
                if (future.isCancelled()) {
                    System.err.println("Task cancellato per timeout");
                    continue;
                }
                try {
                    System.out.println("Risultato invokeAll: " + future.get());
                } catch (ExecutionException e) {
                    System.err.println("Errore nel thread: " + e.getCause().getMessage());
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
