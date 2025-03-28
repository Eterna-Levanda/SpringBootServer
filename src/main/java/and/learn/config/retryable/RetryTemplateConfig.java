package and.learn.config.retryable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**Questa configurazione viene usata dal RetryTemplateService.
 *
 * Utilizzare questo tipo di configurazione, anzichè l'annotation @Retryable,
 *      è vantaggioso perchè ti permette di configurare dinamicamente
 *      (ad esempio tramite property sullo yml)
 *      i vari valori,
 *      dato che l'annotation vuole valori costanti in input
 *      e non è possibile fornirglieli dinamicamente con property.*/

/*Dovrei aggiungere l'annotation @Retry ma l'avevo già definita nel ApplicationMain
per far funzionare le classi Service che usano l'annotation @Retry.
L'annotation @EnableRetry o la metti nel main che lancia Spring Boot
o la metti in una classe annotata con @Configuration come questa.
Questa è una regola generale
*/
@Configuration
public class RetryTemplateConfig {


    @Bean("retryTemplateMax2Tentativi")
    public RetryTemplate retryTemplateMax2Tentativi() {
        RetryTemplate retryTemplate = new RetryTemplate();
        configureTemplate(retryTemplate, 2);
        return retryTemplate;
    }

    @Bean("retryTemplateMax3Tentativi")
    public RetryTemplate retryTemplateMax3Tentativi() {
        RetryTemplate retryTemplate = new RetryTemplate();
        configureTemplate(retryTemplate, 3);
        return retryTemplate;
    }

    private static void configureTemplate(RetryTemplate retryTemplate, int maxAttempts) {

        /*Elenco di più eccezioni gestibili.
        * Se non utilizzi questa mappa (basta non passarla al costruttore)
        * le eccezioni vengono viste tutte come default true,
        * ossia si innesca il meccanismo di retry per ogni eccezione lanciata.
        *
        * Se invece utilizzi la mappa e il metodo lancia un'eccezione non qui definita,
        * viene considerato default false, ossia interrompe tutto,
        * quindi ha senso definire solo quelle true, per le quali si deve ripetere il metodo*/
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(RuntimeException.class, false);      // Interrompe tutto per  RuntimeException
        exceptionMap.put(IllegalArgumentException.class, true); // Effettua retry per  IllegalArgumentException

        // Configuriamo la politica di retry
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, exceptionMap);

        // Configuriamo il backoff tra un tentativo e l'altro
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000); // Primo ritardo di 1 secondo
        backOffPolicy.setMultiplier(2.0); // Il ritardo raddoppia ogni volta
        backOffPolicy.setMaxInterval(5000); // Ritardo massimo di 5 secondi

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
    }
}

