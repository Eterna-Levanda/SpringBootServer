package and.learn.controller;

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

    /**
     * Tecnica che utilizza l'annotation @Async su un metodo,
     * come spiegato nella guida ufficiale
     * https://spring.io/guides/gs/async-method
     *
     * @param numOfThreadToExecute = numero di thread da eseguire
       @param eseguiAllOf vale true se deve lanciare i thread tramite
       CompletableFuture.allOf(listOfCompletableFuture).join();
       come indicato nella guida ufficiale,
       altrimenti lancia i thread uno ad uno.
     *
       http://localhost:8080/SpecificController/completableFuture?numOfThreadToExecute=4&eseguiAllOf=true
     * */
    @GetMapping("/completableFuture")
    public String completableFuture(@RequestParam Integer numOfThreadToExecute, @RequestParam(defaultValue = "true") boolean eseguiAllOf) {
        return completableFutureAsyncService.executeAsyncThreads(numOfThreadToExecute, eseguiAllOf);
    }
}
