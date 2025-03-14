package and.learn.completablefuture;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**Concetti imparati:
 *
 * 0) CompletableFuture è una classe parametrizzata, ovvero devi definire nelle parentesi angolari non cosa contiene,
 * ma il tipo di output che il thread che lancia conterrà.
 *
 * 1) I CompletableFuture servono a lanciare N thread e aspettare che terminino tutti prima di proseguire,
 * mantenendone il controllo sull'esito e sul valore di ritorno,
 * non a lanciare un thread separato di cui non si sa come termina perdendone completamente il controllo.
 *
 * 2) join e get terminano quando terminano tutti i thread (se richiamato sul CompletableFuture contenitore)
 * 3) join e get si differenziano solo perchè join non dichiara di lanciare eccezioni, get sì.
 * 4) Solo join e get lanciano i thread
 *
 * 5) Per metodi di thread con valore di ritorno si usa supplyAsync per creare un CompletableFuture,
 * se il metodo non ha un valore di ritorno si usa runAsync che si rifà ai Runnable di Tread classico*/
public class CompletableFutureSupplyAsyncMain {

    //vale true se vuoi eseguire un metodo del thread normale, senza interruzioni con Thread.sleep poco realistici
    private static final boolean METODO_REALISTICO = false;
    //vale true se vuoi che il metodo del thread possa andare in errore
    private static final boolean SIMULA_ERRORE = false;

    public static void main(String[] args) {

        /*Creo un pool di thread di 5, in modo che non ci siano mai più di 5 thread attivi contemporaneamente.
         * Se ne arrivano altri, aspetteranno.
         * Se non hai necessità di limitare il numero di thread, forse puoi fare a meno di crearlo,
         * verrà creato un thread con un numero più ampio di thread possibili*/
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // Simuliamo una lista di sorgenti dati, ogni input è per un thread diverso
        List<String> dataSources = List.of("Source1", "Source2", "Source3", "Source4", "Source5");

        /* Da una lista di input (dataSources) si costruisce una lista di CompletableFuture.
           I singoli CompletableFuture li costruisco tramite CompletableFuture.supplyAsync,
           metodo che si usa per i thread che resituiscono metodi */
        List<CompletableFuture<String>> futures = dataSources.stream()
                .map(source -> CompletableFuture.supplyAsync(() -> metodoReturnResult(source), executorService)
                              /* lo tolgo temporaneamente per semplicità di lettura
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

        /* Piccola nota: se crei un CompletableFuture separato lanciato su un ExecutorService separato,
           verrà comunque eseguito insieme a tutti gli altri,
           come se esistesse un unico ThreadPool.
           Quindi non vanno mai creati nuovi CompletableFuture o nuovi ExecutorService se i precedenti non sono terminati.
           Se vuoi provarlo decommenta questa parte sotto*/
       /* ExecutorService executor = Executors.newFixedThreadPool(1);
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> metodoReturnRisultato("source"), executor);
        String result = cf.get();
        executor.shutdown();
        System.out.println("Risultato del singolo CompletableFuture " + result);*/

        /* ******
               3 metodi per eseguire i thread, decommenta il metodo che vuoi eseguire
        ******/
        //results = metodo1UsingGet(futures);
        //results = metodo2UsingJoin(futures);
        //results = metodo3UsingAllOf(futures);
        results = metodo4JoinUnicoPoiRaccoltaRisultati(futures);

        //dentro results ho la lista dei risultati dei singoli thread
        System.out.println("Risultati finali: " + results);

        // Shutdown del thread pool
        executorService.shutdown();
    }


    /*
    Usa il metodo get() per ottenere i risultati, ma per definizione lancia eccezioni
    * */
    private static List<String> metodo1UsingGet(List<CompletableFuture<String>> futures) {

        List<String> results;

        /* I thread verranno lanciati solo quando chiamo toList,
           è la scelta migliore rispetto a invocare il metodo get() direttamente sui singoli CompletableFuture
           come mostrato  successivamente in questo metodo
        */
        results = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        /* Ciclo sui singoli CompletableFuture su cui invoco get() che inizia a far partire i thread.
           Non è un'ottima scelta perchè poi alcuni per andare a termine richiedono i successivi get.
         */
        /*
            results = new ArrayList<>();
            for(CompletableFuture<String> cf : futures) {
                results.add(cf.get());
            }
         */

        return results;
    }

    /*
    Usa il metodo join() per ottenere i risultati, che per definizione NON lancia eccezioni
    * */
    private static List<String> metodo2UsingJoin(List<CompletableFuture<String>> futures) {

        List<String> results;

        /* I thread verranno lanciati solo quando chiamo toList,
           è la scelta migliore rispetto a invocare il metodo join() direttamente sui singoli CompletableFuture
           come mostrato  successivamente in questo metodo
        */
        //Qui ho voluto crearmi una classe ridondante per esercitarmi con l'alternanza classe/funzioni. Per eliminarla basta decommentare la riga sotto
        FunctionCompletableFuture fcf = new FunctionCompletableFuture();
        Stream<String> stream = futures.stream().map(fcf /*f->f.join()*/);
        results = stream.toList();

        /* Ciclo sui singoli CompletableFuture su cui invoco get() che inizia a far partire i thread.
           Non è un'ottima scelta perchè poi alcuni per andare a termine richiedono i successivi get.
         */
        /*
        results = new ArrayList<>();
        for(CompletableFuture<String> cf : futures) {
            results.add(cf.join());
        }*/
        return results;
    }

    /*Questa classe l'ho crata solo per esercitarmi sull'alternanza function/classe*/
    public static class FunctionCompletableFuture implements Function<CompletableFuture<String>, String> {

        @Override
        public String apply(CompletableFuture<String> stringCompletableFuture) {
            return stringCompletableFuture.join();
        }
    }

    /*
    Metodo più articolato che combina tutti i CompletableFuture in input in un solo CompletableFuture per gestirlo singolarmente.
    Non si capisce quale possa essere il vantaggio rispetto ai precedenti.
    * */
    private static List<String> metodo3UsingAllOf(List<CompletableFuture<String>> futures) {


        //converto una lista (di CompletableFuture) in un array
        CompletableFuture<String>[] futuresArray = futures.toArray(new CompletableFuture[0]);

        /* Ottengo un unico CompletableFuture di tipo Void perchè così lo restituisce CompletableFuture.allOf.
         * Questo CompletableFuture che creo sarà completato solo quando tutti i thread/future saranno completati.
         * L'esecuzione del metodo CompletableFuture.allOf è presente anche nella mia classe CompletableFutureRunAsyncMain per i thread senza metodo,
         * con la differenza che in quel caso dopo allOf si esegue join per lanciare i thread senza return,
         * qui invece occorre successivamente estrare un risultato e si segue quindi una strada diversa.
         * */
        CompletableFuture<Void> allOfCompletableFuture = CompletableFuture.allOf(futuresArray);

        /* Definisco una funzione per far eseguire il metodo join su tutta la lista futures, in modo da lanciare tutti i thread.
         * Di fatto sono le stesse operazioni presenti nel metodo metodo2UsingJoin,
         * ma in quel metodo sono istruzioni che vengono eseguite, qui è solo la definizione di una funzione.
         * */
        Function<Void, List<String>> applyJoinFunction = paramVoidInutile -> futures.stream().map(f->f.join()).collect(Collectors.toList());

        /* Al CompletableFuture contenitore di tutti i Future eseguo thenApply
        per definirgli quale sarà l'azione-funzione da compiere quando su allResults eseguirò il metodo join che lancerà tutti i thread.
        In altre parole tramite thenApply gli dico che su tutti i suoi CompletableFuture che contiene
        (avendogli passato una lista di CompeltableFuture tramite il metodo allOf),
        deve eseguire la funzione definita sopra (applyJoinFunction),
        una funzione che esegue il metodo join sui singoli CompletableFuture per lanciare tutti i thread*/
        CompletableFuture<List<String>> allResults = allOfCompletableFuture.thenApply(applyJoinFunction);

        /*Qui puoi scegliere se invocare il metodo get o join.
        La differenza è la solita: get dichiara di lanciare eccezioni, join no.
        Entrambi i metodi però terminano se un thread termina con un'eccezione, su questo sono uguali
        */

        // Lancio i thread e attendo che abbiano tutti finito per restituire la lista dei risultati
        return allResults.join();
    }

    /*Questo metodo è una combinazione delle tecniche precedenti,
    * perchè prima si crea un unico CompletableFuture che serve a lanciare tutti i thread col metodo join,
    * poi sui singoli CompletableFuture in input invoca ancora la join per estrarre i risultai dati thread già terminati.
    *
    * Invece nei metodi 1 e 2, senza il CompletableFuture unico, era eseguendo il join sui singoli CompletableFuture
    * non ancora avviati/terminati che si lanciavano i thread uno ad uno, qui invece li si lancia tutti assieme.
    * */
    private static List<String> metodo4JoinUnicoPoiRaccoltaRisultati(List<CompletableFuture<String>> futures) {
        //converto una lista (di CompletableFuture) in un array
        CompletableFuture<String>[] futuresArray = futures.toArray(new CompletableFuture[0]);

        //in questo modo chiamo il join sull'oggetto contenitore, in modo da avviare tutti i thread
        CompletableFuture.allOf(futuresArray).join();

        /* Raccolgo tutte le response una ad una dai CompletableFuture in input chiamando join.
           E' importante sapere che in questo caso, ovvero con i thread già terminati,
           quando eseguo join vado solo ad estrarre il risultato */
        List<String> results = new ArrayList<>();
        for(CompletableFuture<String> cf : futures) {
            results.add(cf.join());
        }
        return results;
    }

    /*  Metodo che verrà eseguito in un thrad parallelo di durata casuale.
     * Lavorando sulle costanti si possono ottenere comportamenti diversi
     * */
    private static String metodoReturnResult(String input) {
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
        return "output ottenuto da input " + input;
    }

    private static void simulaErroreCasuale(String input, Random random) {
        if (SIMULA_ERRORE && random.nextBoolean()) {
            throw new RuntimeException("Errore di rete per " + input);
        }
    }
}



