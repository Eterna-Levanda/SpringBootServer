package and.learn.retryable;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

/*
 *  Service dove hai 2 metodi di retry che lanciano la stessa eccezione ma sono gestiti tramite configurazione globale,
 * non a livello di singolo metodo.
 * Il recovery è anche gestito diversamente:
 *  con una funzione per il metodo retryableMethodConRecover,
 *  con un catch per il metodo retryableMethodSenzaRecover.
 *
 * In questo modo, definendo la strategia di retry tramite bean,
 * puoi usare eventualmente delle properties yml per la configurazione dinamica.
 * E' anche possibile definire per quali eccezioni non deve scattare il retry.
 * Vedi classe RetryTemplateConfig
 * */
@Log4j2
@Service
public class RetryTemplateService {

    @Autowired
    RetryTemplate retryTemplateMax2Tentativi;

    @Autowired
    /*Non c'è bisogno di mettere il Qualifier sebbene i due bean siano dello stesso tipo,
    perchè il binding viene fatto col nome della variabile.
    Se cambiassi il nome allora sarebbe necessario usare Qualifier,
    altrimenti non si avvierebbe nemmeno il server

    @Qualifier("retryTemplateMax3Tentativi")
    * */
    RetryTemplate retryTemplateMax3Tentativi;


    public String retryableMethodConRecover() {

        /*NB: il metodo execute restituisce lo stesso valore restituito dalla funzione RetryCallBack (la prima, quella che prende in input retryContext),
        ovvero in questo esempio "metodo completato con successo"*/
        return retryTemplateMax3Tentativi.execute(retryContext -> {
            int attempt = retryContext.getRetryCount() + 1;
            log.info("⏳ Tentativo n." + attempt);

            //lancio fisso un'eccezione
            throw new IllegalArgumentException();

            //dovrebbe esserci un return, ma siccome lanciamo eccezione fissa non compilerebbe
            //return "metodo completato con successo";
        }, context -> {
            //Callback eseguito se tutti i tentativi falliscono
            String s = "Metodo fallito nonostante " + context.getRetryCount() + " tentativi. Eccezione = " + context.getLastThrowable().toString();
            log.error(s);
            return s;
            });
    }


    public String retryableMethodSenzaRecover() {

        try {
            /*NB: il metodo execute restituisce lo stesso valore restituito dalla funzione RetryCallBack,
            ovvero in questo esempio "metodo completato con successo"*/
            return retryTemplateMax2Tentativi.execute(retryContext -> {
                int attempt = retryContext.getRetryCount() + 1;
                log.info("⏳ Tentativo n." + attempt);
                //lancio fisso un'eccezione
                throw new IllegalArgumentException();

                //dovrebbe esserci un return nel caso in cui il metodo termini con successo, ma siccome lanciamo eccezione fissa lo commento altrimenti non compilerebbe
                //return "metodo completato con successo";

            } /*Senza il parametro RecoveryCallback come per il metodo retryableMethodConRecover,
                viene lanciata l'eccezione all'ultimo tentativo,
                anzichè richiamare un metodo.
                Il catch serve proprio a questo, funge da metodo di recover*/
            );

        } catch (IllegalArgumentException e){
            String msg = "Errore definitivo gestito con un try catch: " + e.toString();
            log.error(msg);
            return msg;
        }
    }


}
