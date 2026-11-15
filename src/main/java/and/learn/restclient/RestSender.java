package and.learn.restclient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class RestSender {

    private final RestClient restClient;

    public RestSender() {

        this.restClient = new RestClient(new RestTemplate());
    }

    public RestSender(RestTemplate restTemplate) {

        this.restClient = new RestClient(restTemplate);
    }

    public <T, R> ResponseEntity<R> sendPost(String url, T body, Map<String, String> headers, Class<R> responseType) {
        return restClient.post(url, body, headers, responseType);
    }

    public <T, R> ResponseEntity<R> sendPost(String url, T body, HttpHeaders headers, Class<R> responseType) {
        return restClient.post(url, body, headers, responseType);
    }

    public <R> ResponseEntity<R> sendGet(String url, Map<String, String> headers, Class<R> responseType) {
        return restClient.get(url, headers, responseType);
    }
}