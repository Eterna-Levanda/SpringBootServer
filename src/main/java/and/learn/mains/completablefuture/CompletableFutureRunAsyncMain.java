package and.learn.mains.completablefuture;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Per metodi di thread con valore di ritorno si usa supplyAsync per creare un CompletableFuture,
 * se invece il metodo non ha un valore di ritorno si usa runAsync che si rifà ai Runnable di Tread classici
 * */
public class CompletableFutureRunAsyncMain {

    //vale true se vuoi eseguire un metodo del thread normale, senza interruzioni con Thread.sleep poco realistici
    private static final boolean METODO_REALISTICO = true;
    //vale true se vuoi che il metodo del thread possa andare in errore
    private static final boolean SIMULA_ERRORE = false;

    public static void main(String[] args) {

        // Puoi regolare il numero di thread a seconda delle tue esigenze
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        /*Quando dichiari un CompletableFuture devi dichiarare cosa contiene, ovvero l'output del metodo da eseguire in parallelo.
        Se il metodo non restituisce un risultato, sarà un CompletableFuture<Void>*/

        //Un array di CompletableFuture<Void>
        CompletableFuture<Void>[] futures = new CompletableFuture[3];

        /* Il metodo runAsync restituisce un CompletableFuture<Void> perchè prende in input un Runnable, come un Thread normale che non ha un valore di ritorno.
        Quindi non può contenere un risultato di ritorno dal thread parallelo eseguito con il metodo metodoReturnVoid.
        RunAsync va usato sempre e solo per thread che non restituiscono un risultato.

         La cosa strana di questo approccio è che il primo thread parte già quando si invoca il secondo runAsync, ma vanno a termine solo alla fine di tutto
         */
        futures[0] = CompletableFuture.runAsync(() -> metodoReturnVoid("1"), executorService);
        futures[1] = CompletableFuture.runAsync(() -> metodoReturnVoid("2"), executorService);
        futures[2] = CompletableFuture.runAsync(() -> metodoReturnVoid("3"), executorService);

        //invece di invocare il join sui singoli CompletableFuture...
        /*for(CompletableFuture cf: futures){
            cf.join();
        }*/

        //... meglio ottenere un unico CompletableFuture che li contiene/rappresenta tutti e chiamo poi il join solo su questo per lanciarli tutti
        CompletableFuture<Void> allOfCompletableFuture = CompletableFuture.allOf(futures);

        /* L'esecuzione  del metodo join (che potrebbe essere anche qui sostituito da get())
        termina quando sono terminati  tutti i thread */
        allOfCompletableFuture.join();

        // Chiudere l'ExecutorService quando non è più necessario
        executorService.shutdown();
    }


    /*  Metodo che verrà eseguito in un thrad parallelo di durata casuale.
     * Lavorando sulle costanti si possono ottenere comportamenti diversi
     * */
    private static void metodoReturnVoid(String input) {
        Random random = new Random();
        int sleepTime = random.nextInt(4000); // Simula tempo di elaborazione fino a 3 secondi
        System.out.println("Elaborazione input: " + input + " (ci vorranno " + sleepTime + " ms) per il thread " + Thread.currentThread().getName());

        if(METODO_REALISTICO){
            for(int i=0;i<sleepTime;i++){}
        } else {
            try {
                /*questo metodo non è realistico perchè il thread viene messo appositamente in sleep e si vedono comportamenti strani,
                per esempio devi invocare i metodi join/get su tutti gli elementi per farli eseguire, altrimenti rimangono fermi
                e non ha senso in un caso realistico credo.
                 */
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrotto durante il fetch dei dati da " + input);
            }

            simulaErroreCasuale(input, random);
        }

        System.out.println("Terminato il thread " + input + " chiamato " + Thread.currentThread().getName());
    }

    private static void simulaErroreCasuale(String input, Random random) {
        if (SIMULA_ERRORE && random.nextBoolean()) {
            throw new RuntimeException("Errore di rete per " + input);
        }
    }
}



