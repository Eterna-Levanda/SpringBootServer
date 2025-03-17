package and.learn.completablefuture;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class AsyncMethodService {


    /* Se vuoi togliere l'asincronia dei thread e usarne uno solo,
     * basta commentare l'annotation.
     *
     * Il parametro dentro l'annotation serve a specificare il nome del bean
     * che rappresenta il ThreadPoolTaskExecutor,
     * in modo che si possano usare e configurare diversi ThreadPoolTaskExecutor,
     * a seconda delle diverse esigenze.
     *
     * Importante: affinchè il metodo sia davvero eseguito in modo asincrono,
     * occorrono 2 cose:
     * 1) che sia invocato sull'istanza singleton del service (banale, non devi creare il service a mano)
     * 2) la classe che lo chiama deve essere diversa dalla classe in cui risiede il metodo async.
     * In pratica la sintassi deve essere sempre questa: istanzaService.metodoAsync e non this.metodoAsync!
     * */
    @Async("executorAsync")
    public CompletableFuture<String> startAsynchMethod(String input){
        final String nomeThread = Thread.currentThread().getName();
        log.info("Avviato il metodo asincrono con input " + input + " sul thread " + nomeThread);
        String sToReturn = input + " aggiunto in modo asincrono";
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("Terminato il metodo asincrono con input " + input + " sul thread " + nomeThread);

        return CompletableFuture.completedFuture(sToReturn);
    }
}
