package and.learn.mains.completablefuture;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class CompletableFutureMain {
    public static void main(String[] args) {

        /*Creo un pool di thread di 5, in modo che non ci siano mai più di 5 thread attivi contemporaneamente.
         * Se ne arrivano altri, aspetteranno*/
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Simuliamo una lista di sorgenti dati
        List<String> dataSources = List.of("Source1", "Source2", "Source3", "Source4", "Source5");

        // Da una lista di input (dataSources) si passa a una lista di CompletableFuture per quell'input (futures)
        List<CompletableFuture<String>> futures = dataSources.stream()
                .map(source -> CompletableFuture.supplyAsync(() -> metodoReturnRisultatod(source), executorService)
                              /* lo tolgo temporaneamente per semplicità
                               // Gestione eccezioni in ogni futuro
                               .exceptionally(ex -> {
                                   System.err.println("Errore durante il fetch dei dati da " + source + ": " + ex.getMessage());
                                   return "Errore per " + source; // Valore di fallback
                               })
                               // Timeout per ogni operazione
                               .completeOnTimeout("Timeout per " + source, 2, TimeUnit.SECONDS)*/)
                .collect(Collectors.toList());






        try {
            /*METODO 1:
            copiato da generali, cicla SUBITO sulla lista di completble future.
             E' diverso dal metodo sotto più articolato, capire le differenze.
            Fino al primo get non succede nulla.
            Poi ciclando estrae i dati in ordine numerico da 1 a 5,
             il primo get sul n.1 fa eseguire tutti i thread ma l'output si ferma fino al completamento del n.1 e di quelli che hanno terminato prima
              Al secondo giro estrae i dati del n.2 che porta a termine tutti gli altri thread.*/
           /* for(CompletableFuture<String> f : futures){
                System.out.println("Ottenuto " + f.get());
            }*/

            /*METODO 2:
            * più classico e preso da chat GPT*/

            // Combina tutti i CompletableFuture e raccoglie i risultati
            CompletableFuture<List<String>> allResults = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream().map(CompletableFuture::join) // Recupera i risultati
                            .collect(Collectors.toList()));


            // Risultati finali

            /*Fino a qui non è stato ancora eseguito nulla dei thread, nessuno è entrato nel metodo fetchDataFromSource
             * Con l'istruzione get invece ci blocchiamo fino al termine di tutti i thread,
             * i cui risultati vengono messi in result*/
            List<String> results = allResults.get();
            System.out.println("Risultati finali: " + results);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Errore durante il completamento dei CompletableFuture: " + e.getMessage());
        }

        // Shutdown del thread pool
        executorService.shutdown();
    }

    // Metodo simulato per ottenere dati da una sorgente
    private static String metodoReturnRisultatod(String source) {
        Random random = new Random();
        int sleepTime = random.nextInt(3000); // Simula tempo di elaborazione fino a 3 secondi
        try {
            System.out.println("Fetching data da " + source + " (ci vorranno " + sleepTime + " ms) per il thread "+Thread.currentThread().getName());
            Thread.sleep(sleepTime);
           /* if (random.nextBoolean()) { // Simula errore casuale
                throw new RuntimeException("Errore di rete per " + source);
            }*/
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrotto durante il fetch dei dati da " + source);
        }
        System.out.println("Terminato il thread "+source+" chiamato "+Thread.currentThread().getName());
        return "Dati da " + source;
    }

    /*Stesso metodo di sopra ma senza restituire risultati*/
    private static void metodoReturnVoid(String source) {
        Random random = new Random();
        int sleepTime = random.nextInt(3000); // Simula tempo di elaborazione fino a 3 secondi
        try {
            System.out.println("Fetching data ci vorranno " + sleepTime + " ms)");
            Thread.sleep(sleepTime);
           /* if (random.nextBoolean()) { // Simula errore casuale
                throw new RuntimeException("Errore di rete per " + source);
            }*/
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrotto durante il fetch dei dati");
        }

    }

    private static void prova() {
        ExecutorService executorService = Executors.newFixedThreadPool(3); // Puoi regolare il numero di thread a seconda delle tue esigenze

        CompletableFuture<String>[] futures = new CompletableFuture[3];

        //Lista di possibili vincolatari associati alla proposta
        //TOdo capire meglio come usare il runAsync.
       /* futures[0] = CompletableFuture.runAsync(() -> metodoReturnVoid("1"), executorService);
        futures[1] = CompletableFuture.runAsync(() -> metodoReturnVoid("2"), executorService);
        futures[2] = CompletableFuture.runAsync(() -> metodoReturnVoid("3"), executorService);*/

        // Attendere il completamento di tutte le operazioni
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures);
        allOf.join();

        // Chiudere l'ExecutorService quando non è più necessario
        executorService.shutdown();
    }


}



