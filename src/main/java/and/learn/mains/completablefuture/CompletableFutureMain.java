package and.learn.mains.completablefuture;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CompletableFutureMain {
    public static void main(String[] args) {

        /*Creo un pool di thread di 5, in modo che non ci siano mai più di 5 thread attivi contemporaneamente.
         * Se ne arrivano altri, aspetteranno.
         * Se non hai necessità di limitare il numero di thread, forse puoi fare a meno di crearlo,
         * verrà creato un thread con un numero più ampio di thread possibili*/
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Simuliamo una lista di sorgenti dati, ogni input è per un thread diverso
        List<String> dataSources = List.of("Source1", "Source2", "Source3", "Source4", "Source5");

        // Da una lista di input (dataSources) si passa a una lista di CompletableFuture
        List<CompletableFuture<String>> futures = dataSources.stream()
                .map(source -> CompletableFuture.supplyAsync(() -> metodoReturnRisultato(source), executorService)
                              /* lo tolgo temporaneamente per semplicità
                               // Gestione eccezioni in ogni futuro
                               .exceptionally(ex -> {
                                   System.err.println("Errore durante il fetch dei dati da " + source + ": " + ex.getMessage());
                                   return "Errore per " + source; // Valore di fallback
                               })
                               // Timeout per ogni operazione
                               .completeOnTimeout("Timeout per " + source, 2, TimeUnit.SECONDS)*/)
                .collect(Collectors.toList());


        //Espongo 3 metodi diversi per ottenere i risultati dell'esecuzione dei thread
        List<String> results;

        /*piccola nota: se crei un CompletableFuture separato, verrà comunque eseguito insieme a tutti gli altri, anche se usi un diverso ThreadPool
        quindi non vanno creati nuovi CompletableFuture o nuovi ExecutorService se i precedenti non sono terminati*/
       /* ExecutorService executor = Executors.newFixedThreadPool(1);
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> metodoReturnRisultato("source"), executor);
        String result = cf.get();
        executor.shutdown();
        System.out.println("Risultato del singolo CompletableFuture " + result);*/


        //results = metodo1UsingGet(futures);
        //results = metodo2UsingJoin(futures);
        results = metodo3UsingACombiantion(futures);

        //dentro results ho la lista dei risultati ottenuti al termine del thread
        System.out.println("Risultati finali: " + results);

        // Shutdown del thread pool
        executorService.shutdown();

        /*****
         * APPROCCIO SIMILE MA USANDO RUNASYNC PER ESEGUIRE METODI CHE NON RESTITUISCONO RISULTATI
         * *******/
        //TODO capire se posso usare RunAsync ma restituendo dei risultati.
        provaConRunAsyncEMetodoVoid();
    }


    /*
    Usa il metodo get() per ottenere i risultati, ma per definizione lancia eccezioni
    * */
    private static List<String> metodo1UsingGet(List<CompletableFuture<String>> futures) throws InterruptedException, ExecutionException {

        List<String> results;

        //uso le funzioni e avvio i thread solo quando chiamo toList, scelta migliore
        results = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        //ciclo, ed avvio i thread al primo get, non è un'ottima scelta perchè poi alcuni per andare a termine richiedono i successivi get
        /*for(CompletableFuture<String> cf : futures) {
            results.add(cf.get());
        }*/

        return results;
    }

    /*
    Usa il metodo join() per ottenere i risultati, che per definizione NON lancia eccezioni
    * */
    private static List<String> metodo2UsingJoin(List<CompletableFuture<String>> futures) {

        List<String> results;

        //uso le funzioni e avvio i thread solo quando chiamo toList, scelta migliore
        Prova p = new Prova();
        Stream<String> stream = futures.stream().map(p /*f->f.join()*/);
        results = stream.toList();

        //ciclo, ed avvio i thread al primo join, non è un'ottima scelta perchè poi alcuni per andare a termine richiedono i successivi join
        /*results = new ArrayList<>();
        for(CompletableFuture<String> cf : futures) {
            results.add(cf.join());
        }*/
        return results;
    }

    /*Questa classe l'ho crata solo per esercitarmi sull'alternanza function/classe*/
    public static class Prova implements Function<CompletableFuture<String>, String> {

        @Override
        public String apply(CompletableFuture<String> stringCompletableFuture) {
            return stringCompletableFuture.join();
        }
    }

    /*
    Metodo più articolato che combina tutti i CompletableFuture in input in un solo CompletableFuture per gestirlo singolarmente

    * */
    private static List<String> metodo3UsingACombiantion(List<CompletableFuture<String>> futures) {


        //converto una lista di CompletableFuture in un array
        CompletableFuture<String>[] futuresArray = futures.toArray(new CompletableFuture[0]);

        //Creo un nuovo singolo CompletableFuture che sarà completato solo quando la lista di CompletableFuture è completata
        //questa funzione è identica a quella che trovi nel metodo metodo2UsingJoin
        Function<Void, List<String>> voidListFunction = v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

        CompletableFuture<List<String>> allResults = CompletableFuture.allOf(futuresArray)
                .thenApply(voidListFunction);

        // Lancio i thread e attendo che abbiano tutti finito per restituire la lista dei risultati
            /*Qui puoi scegliere se invocare il metodo get o join.
            La differenza è la solita: get dichiara di lanciare eccezioni, join no.
            Entrambi i metodi però terminano se un thread termina con un'eccezione, su questo sono uguali
            */
        return allResults.join();
    }


    /*Capire se runAsync va in coppia con il metodo void restituito da metodoReturnVoid
     * */
    private static void provaConRunAsyncEMetodoVoid() {
        ExecutorService executorService = Executors.newFixedThreadPool(3); // Puoi regolare il numero di thread a seconda delle tue esigenze

        /*Quando dichiari un CompletableFuture devi dichiarare cosa contiene, ovvero l'output del metodo da eseguire in parallelo.
        Se il metodo non restituisce un risultato, sarà un CompletableFuture<Void>*/

        //Un array di CompletableFuture<Void>
        CompletableFuture<Void>[] futures = new CompletableFuture[3];

        futures[0] = CompletableFuture.runAsync(() -> metodoReturnVoid("1"), executorService);
        futures[1] = CompletableFuture.runAsync(() -> metodoReturnVoid("2"), executorService);
        futures[2] = CompletableFuture.runAsync(() -> metodoReturnVoid("3"), executorService);

        // Attendere il completamento di tutte le operazioni
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures);
        allOf.join();

        // Chiudere l'ExecutorService quando non è più necessario
        executorService.shutdown();
    }

    /*
     * METODI CHE VERRANNO ESEGUITI IN THREAD SEPARATI E PARALLELI,
     * UNO RESTITUISCE UN RISULTATO, L'ALTRO NO
     * */

    // Metodo simulato per ottenere dati da una sorgente
    private static String metodoReturnRisultato(String input) {
        Random random = new Random();
        int sleepTime = random.nextInt(3000); // Simula tempo di elaborazione fino a 3 secondi
        try {
            System.out.println("Elaborazione input: " + input + " (ci vorranno " + sleepTime + " ms) per il thread " + Thread.currentThread().getName());
            Thread.sleep(sleepTime);
           /*if (random.nextBoolean()) { // Simula errore casuale
                throw new RuntimeException("Errore di rete per " + input);
            }*/
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrotto durante il fetch dei dati da " + input);
        }
        System.out.println("Terminato il thread " + input + " chiamato " + Thread.currentThread().getName());
        return "Dati da " + input;
    }

    /*Stesso metodo di sopra ma senza restituire risultati*/
    private static void metodoReturnVoid(String input) {
        Random random = new Random();
        int sleepTime = random.nextInt(3000); // Simula tempo di elaborazione fino a 3 secondi
        try {
            System.out.println("Elaborazione input: " + input + " (ci vorranno " + sleepTime + " ms) per il thread " + Thread.currentThread().getName());
            Thread.sleep(sleepTime);
           /*if (random.nextBoolean()) { // Simula errore casuale
                throw new RuntimeException("Errore di rete per " + input);
            }*/
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrotto durante il fetch dei dati da " + input);
        }
        System.out.println("Terminato il thread " + input + " chiamato " + Thread.currentThread().getName());
    }


}



