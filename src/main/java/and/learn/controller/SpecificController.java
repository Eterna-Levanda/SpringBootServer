package and.learn.controller;

import and.learn.completablefuture.CompletableFutureAsyncService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Questo controller serve per innescare funzionalità specifiche
 * */
@Log4j2
@RestController()
@RequestMapping("/SpecificController")
public class SpecificController {

    @Autowired
    private CompletableFutureAsyncService completableFutureAsyncService;

    /*Con questa tecnica noi non sappiamo quando esattamente partirà un thread,
     * sappiamo solo quando finiranno tutti, ovvero quando termina il metodo join.
     * */
    @GetMapping("/completableFuture")
    public String completableFuture(@RequestParam Integer numOfThreadToExecute) {

        //questo array conterrà tutti i CompletableFuture
        CompletableFuture<String>[] listOfCompletableFuture = new CompletableFuture[numOfThreadToExecute];

        //cronometro la durata dell'esecuzione dei thread
        long start = System.currentTimeMillis();

        for(int i=0; i<numOfThreadToExecute; i++) {
            log.info("Sto per invocare il metodo asincorno n. " + (i+1));
            /* Invocare il metodo @Async "metodoAsincrono" serve solo a creare il thread,
            sarà poi il suo gestore ThreadPoolTaskExecutor a decidere come gestire i thread e
            quando farli partire.
            Il primo thread può partire anche subito, non c'è un avvio esplicito. */
            CompletableFuture<String> completableFuture = completableFutureAsyncService.metodoAsincrono("input " + (i+1));

            //inserisco il CompletableFuture nell'array contenitore
            listOfCompletableFuture[i] = completableFuture;
        }
        log.info("Sono uscito dal for dopo aver chiamato gli " + numOfThreadToExecute + " metodi asincroni");
        log.info("Adesso chiamo CompletableFuture.allOf");

        //Di tutti i CompletableFuture ne ottengo uno solo
        CompletableFuture<Void> singleVoidCompletFuture = CompletableFuture.allOf(listOfCompletableFuture);
        log.info("Adesso chiamo il metodo join");

        /*Chiamando join attendo la terminazione di tutti i thread termineranno,
        quindi si può restare fermi qui anche a lungo */
        singleVoidCompletFuture.join();


        log.info("Adesso estraggo i valori uno ad uno con il get");

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
