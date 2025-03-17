package and.learn.completablefuture;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Log4j2
@Service
public class CompletableFutureAsyncService {

    @Autowired
    private AsyncMethodService asyncMethodService;

    /**@param eseguiAllOf Se posto a true avvierà i thread con un'unica esecuzione, tramite:
     CompletableFuture.allOf(listOfCompletableFuture).join();
     per poi estrarne i risultati già pronti ciclando singolarmente sui singoli CompletableFuture,
     come indicato nella guida ufficiale,
     altrimenti i thread verranno lanciati direttamente quando si cicla
     sui singoli CompletableFuture attraverso il metodo join*/
    public String executeAsyncThreads(int numOfThreadToExecute, boolean eseguiAllOf){
        //questo array conterrà tutti i CompletableFuture
        CompletableFuture<String>[] listOfCompletableFuture = new CompletableFuture[numOfThreadToExecute];

        //cronometro la durata dell'esecuzione dei thread
        long start = System.currentTimeMillis();

        for(int i=0; i<numOfThreadToExecute; i++) {
            log.info("Sto per invocare il metodo asincorno n. " + (i+1));
            /* Invocare il metodo @Async "metodoAsincrono" serve solo a creare il thread,
            sarà poi il suo gestore ThreadPoolTaskExecutor a decidere come gestire i thread e
            quando farli partire.
            Tenere presente però che il primo thread può partire anche subito, non c'è un avvio esplicito. */
            CompletableFuture<String> completableFuture = asyncMethodService.startAsynchMethod("input " + (i+1));

            //inserisco il CompletableFuture nell'array contenitore
            listOfCompletableFuture[i] = completableFuture;
        }
        log.info("Sono uscito dal for dopo aver chiamato gli " + numOfThreadToExecute + " metodi asincroni");

        if(eseguiAllOf){
            log.info("Adesso chiamo CompletableFuture.allOf");

            //Di tutti i CompletableFuture ne ottengo uno solo
            CompletableFuture<Void> singleVoidCompletFuture = CompletableFuture.allOf(listOfCompletableFuture);
            log.info("Adesso chiamo il metodo join");

            /*Chiamando join sul singolo CompletableFuture attendo la terminazione di tutti i thread,
            quindi si può restare fermi qui anche a lungo */
            singleVoidCompletFuture.join();
            log.info("Tutti i thread sono terminati, adesso estraggo i valori uno ad uno con il join");
        } else {
            log.info("Avvio i singoli thread e attendo i risultati uno per uno");
        }

        //Estraggo i valori dei singoli CompletableFuture presenti nell'array. Posso usare join o get.
        for(int i=0; i<numOfThreadToExecute; i++) {
            /*Da notare che in questo caso, ovvero creando un unico CompletableFuture contenitore,
             quando eseguo join sui singoli CompletableFuture io ottengo subito il valore di ritorno dal thread solo perchè
            prima ho invocato la join CompletableFuture contenitore.
            Altrimenti, se avessi usato sempre e solo i singoli CompletableFuture,
            adesso che in questo ciclo io invoco il metodo join,
            ad ogni esecuzione vedrei eseguirsi i vari thread,
            e aspetterei ogni volta che finiscano di essere eseguiti.
            La tecnica qui presente è la migliore perchè esposta nella documentazione ufficiale:
            https://spring.io/guides/gs/async-method*/
            log.info("Estraggo il valore ottenuto eseguendo il thread n. " + (i+1) + " ==> " + listOfCompletableFuture[i].join());
        }

        //fermo il cronometro e stampo la durata di esecuzione di tutti i thread
        long end = System.currentTimeMillis();
        double durata = (end - start) / 1000d;
        log.info("L'esecuzione totale dei thread è durata: " + durata);

        return "metodo terminato dopo " + durata + " secondi";
    }
}
