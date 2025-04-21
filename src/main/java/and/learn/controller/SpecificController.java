package and.learn.controller;

import and.learn.cache.CacheService;
import and.learn.retryable.RetryTemplateService;
import and.learn.retryable.RetryableDoppioMetodoDueRecoverService;
import and.learn.retryable.RetryableDoppioMetodoUnRecoverService;
import and.learn.retryable.RetryableService;
import and.learn.completablefuture.CompletableFutureAsyncService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Questo controller serve per innescare funzionalità specifiche
 * */
@Log4j2
@RestController()
@RequestMapping("/SpecificController")
public class SpecificController {

    @Autowired
    private CompletableFutureAsyncService completableFutureAsyncService;

    @Autowired
    RetryableService retryableService;

    @Autowired
    RetryableDoppioMetodoUnRecoverService retryableDoppioMetodoUnRecoverService;

    @Autowired
    RetryableDoppioMetodoDueRecoverService retryableDoppioMetodoDueRecoverService;

    @Autowired
    RetryTemplateService retryableConTemplateService;

    @Autowired
    CacheService cacheService;

    /**
     * Tecnica che utilizza l'annotation @Async sul metodo che verrà eseguito su thread separato,
     * come spiegato nella guida ufficiale
     * <a href="https://spring.io/guides/gs/async-method">...</a>
     *
     @param numOfThreadToExecute = numero di thread da eseguire
     @param eseguiAllOf vale true se deve lanciare i thread tramite
     CompletableFuture.allOf(listOfCompletableFuture).join();
     come indicato nella guida ufficiale,
     altrimenti lancia i thread singolarmente uno a uno.

     Per provarlo:
     http://localhost:8080/SpecificController/completableFuture?numOfThreadToExecute=4&eseguiAllOf=true
      * */
    @GetMapping("/completableFuture")
    public String completableFuture(@RequestParam Integer numOfThreadToExecute, @RequestParam(defaultValue = "true") boolean eseguiAllOf) {
        return completableFutureAsyncService.executeAsyncThreads(numOfThreadToExecute, eseguiAllOf);
    }

    /**Serve a testare il funzionamento dell'annotation @Retryable su un metodo,
     * in modo da eseguirlo più volte in caso di fallimento.
     * Esempio flessibile perchè permette di testare il fallimento e il successo.
     *  Per provarlo:
       http://localhost:8080/SpecificController/retryableApi?successoAllNEsimoTenativo=2
     * */
    @GetMapping("/retryableApi")
    public String retryableApi(@RequestParam int successoAllNEsimoTenativo) {
        return retryableService.retryableMethod(successoAllNEsimoTenativo);
    }

    /**Serve a testare il funzionamento dell'annotation @Retryable
     *  in un caso in cui in un service ci sono 2 metodi diversi che lanciano la stessa eccezione
     *  motivo per cui ricadono nello stesso metodo di Recover.
     *  Per provarlo:
     http://localhost:8080/SpecificController/retryableMethod
     * */
    @GetMapping("/retryableMethod")
    public String retryableMethod() {
        //Entrambi i metodi 1 o 2 portano allo stesso comportamento
        //return retryableDoppioMetodoService.retryableMethod1();
        return retryableDoppioMetodoUnRecoverService.retryableMethod2();
    }

    /**Serve a testare il funzionamento dell'annotation @Retryable
     *  in un caso in cui in un service ci sono 2 metodi diversi che lanciano la stessa eccezione.
     *  Qui voglio due metodi di recover diversi, per gestire i due metodi diversamente.
     *  Per provarlo:
     http://localhost:8080/SpecificController/retryableMethodRecoverDoppio
     * */
    @GetMapping("/retryableMethodRecoverDoppio")
    public String retryableMethodRecoverDoppio() {
        //I metodi 1 e 2 sono gestiti da metodi recover diversi sebbene lancino la stessa eccezione
        // return retryableDoppioMetodoDueRecoverService.retryableMethod1(0);
        return retryableDoppioMetodoDueRecoverService.retryableMethod2("");
    }

    /**Serve a testare il funzionamento del Retryable ma senza annotation,
     * usando invece il RetryTemplate.
     *  Per provarlo:
     http://localhost:8080/SpecificController/retryTemplate
     * */
    @GetMapping("/retryTemplate")
    public String retryTemplate() {

        //dopo il fallimento dell'ultimo tentativo, viene lanciata eccezione e gestita in un try
        //return retryableConTemplateService.retryableMethodSenzaRecover();

        //dopo il fallimento dell'ultimo tentativo, viene eseguita una funzione di recover
        return retryableConTemplateService.retryableMethodConRecover();
    }

    /**
     * Metodo che restituisce la stringa presa in input in maiuscolo usando una cache
     *  Per provarlo:
     *      http://localhost:8080/SpecificController/toUpperCaseWithCache?input=a
     * */
    @GetMapping("/toUpperCaseWithCache")
    public String toUpperCaseWithCache(@RequestParam String input){
        return cacheService.metodoConCache(input);
    }


}
