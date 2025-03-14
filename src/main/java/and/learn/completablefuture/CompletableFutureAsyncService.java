package and.learn.completablefuture;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Log4j2
@Service
public class CompletableFutureAsyncService {

    /* Se vuoi togliere l'asincronia dei thread e usarne uno solo,
    * basta commentare l'annotarion
    * */
    @Async("executorAsync")
    public CompletableFuture<String> metodoAsincrono(String input){
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
