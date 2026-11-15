package and.learn.restclient;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client REST generico per l'invio di richieste HTTP utilizzando RestTemplate di Spring.
 * Supporta POST con body JSON e header personalizzati, e GET con header.
 */
public class RestClient {

    private final RestTemplate restTemplate;

    /**
     * Crea un nuovo RestClient con il RestTemplate fornito.
     *
     * @param restTemplate il RestTemplate di Spring da utilizzare per le richieste HTTP
     */
    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Invia una richiesta POST con body JSON e header personalizzati.
     *
     * @param url l'URL di destinazione
     * @param body il body della richiesta da serializzare come JSON
     * @param headers la mappa degli header da aggiungere alla richiesta
     * @param responseType il tipo di risposta atteso
     * @param <T> il tipo del body della richiesta
     * @param <R> il tipo del body della risposta
     * @return l'entità di risposta contenente il body e lo status
     */
    public <T, R> ResponseEntity<R> post(String url, T body, Map<String, String> headers, Class<R> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }

        HttpEntity<T> requestEntity = new HttpEntity<>(body, httpHeaders);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
    }

    /**
     * Invia una richiesta POST con body JSON e header personalizzati.
     *
     * @param url l'URL di destinazione
     * @param body il body della richiesta da serializzare come JSON
     * @param headers gli header HTTP da includere nella richiesta
     * @param responseType il tipo di risposta atteso
     * @param <T> il tipo del body della richiesta
     * @param <R> il tipo del body della risposta
     * @return l'entità di risposta contenente il body e lo status
     */
    public <T, R> ResponseEntity<R> post(String url, T body, HttpHeaders headers, Class<R> responseType) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
    }

    /**
     * Invia una richiesta GET con header personalizzati.
     *
     * @param url l'URL di destinazione
     * @param headers la mappa degli header da aggiungere alla richiesta
     * @param responseType il tipo di risposta atteso
     * @param <R> il tipo del body della risposta
     * @return l'entità di risposta contenente il body e lo status
     */
    public <R> ResponseEntity<R> get(String url, Map<String, String> headers, Class<R> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
    }
}