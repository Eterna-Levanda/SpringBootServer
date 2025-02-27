package and.learn.controller;

import and.learn.config.PropertyReader;
import and.learn.request.plain.Esterno;
import and.learn.request.propsrinominate.EsternoPropsRinominate;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Log4j2
@RestController("nomeBeanCrudController")
@RequestMapping("/Controller")
public class Controller {


    /*
     * METODI CON LAVORI SUI PARAMETRI IN INPUT
     *
     * */
    @GetMapping("/get")
    //http://localhost:8080
    public String get() {
        return "get!";
    }

    /*
     * http://localhost:8080/Controller/getWithRequestParams?valueString=prova&integer=45&bool=true
     * RequestParam opzionali
     * */
    @GetMapping("/getWithRequestParams")
    public String getWithRequestParams(
            //definisco solo la variabile, quindi il nome sarà il nome del parametro sarà uguale al nome della variabile e l'obbligatorietà è true
            @RequestParam String valueString,
            //definisco il nome del parametro sovrascrivendo quello della variabile, l'obbligatorietà è true
            @RequestParam(name = "integer") Integer valueInteger,
            //definisco solo l'obbligatorietà a false, il nome del parametro sarà quello della variabile
            @RequestParam(required = false) Boolean bool) {
        return "getWithRequestParams. valueString = " + valueString + ", valueInteger = " + valueInteger + ", bool = " + bool;
    }

    /*
    PathVariable (detto anche pathParam con altre librerie) multipli obbligatori con rinomina dei parametri
    http://localhost:8080/Controller/getWithPathParams/ciao/2
    */
    @GetMapping("/getWithPathParams/{param}/{param2}")
    public String getWithPathParams(@PathVariable String param,
                                    @PathVariable(value = "param2") Integer paramInteger) {
        return "getWithPathParams. param = " + param + ", param2 = " + paramInteger;
    }

    /*
    http://localhost:8080/Controller/getWithPathParamsOptional/ciao/erre
    * PathVariable (detto anche pathParam con altre librerie) opzionali con rinomina dei parametri
    * */
    @GetMapping(value = {"/getWithPathParamsOptional", "/getWithPathParamsOptional/{param}", "/getWithPathParamsOptional/{param}/{anotherParam}"})
    public String getWithPathParamsOptional(@PathVariable(required = false) String param,
                                            @PathVariable(name = "anotherParam", required = false) String param2) {
        if (param == null) {
            return "param null";
        } else if (param2 == null) {
            return "param = " + param;
        } else {
            return "param = " + param + ", anotherParam = " + param2;
        }
    }

    /*
      http://localhost:8080/Controller/postRequestBody
      Serve a controllare la validità dei campi in cascata
        {
          "interno": {
              "intero": 1,
              "string": "stringa",
              "booleano": null,
              "data": "23/12/1980",
              "localDate": "1980-12-23"
          }
      }
      Nel PostMapping non serve specificare:
       consumes = MediaType.APPLICATION_JSON_VALUE
       anche se a rigore di logica si dovrebbe, dato che il client deve specificare nell'header:
       Content-type = application/json
     * */
    @PostMapping("/postRequestBody")
    public Esterno postComplesso(@RequestBody(required = false) @Valid Esterno esterno) {
        return esterno;
    }

    /*
    http://localhost:8080/Controller/postParamFormUrlEncoded
    * Parametri passati nel body con x-www-form-urlencoded*/
    @PostMapping(value = "/postParamFormUrlEncoded", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String postParamFormUrlEncoded(@RequestParam String param) {
        return "postParamFormUrlEncoded. Ricevuto param = " + param;
    }

    //Il caso con APPLICATION_FORM_URLENCODED_VALUE e input complesso è sconsigliato, va usato application/json


    /*
    *  http://localhost:8080/Controller/postRequestBodyPropRinominate
       {
          "internoRin": {
              "varNomeBello": "funziona!"
          }
      }
    * Property rinominate nella request, non prendono il nome della variabile java
    * */
    @PostMapping("/postRequestBodyPropRinominate")
    public EsternoPropsRinominate postRequestBodyPropRinominate(@Valid @RequestBody EsternoPropsRinominate esterno) {
        return esterno;
    }

    /*
     * http://localhost:8080/Controller/getStatusCode?statusCode=201
     * */
    @GetMapping("/getStatusCode")
    public ResponseEntity<String> getStatusCode(@RequestParam Optional<Integer> statusCode) {
        return statusCode
                //se status code è valorizzato esegue solo questo
                .map(integer -> ResponseEntity.status(integer).body("Restituisco lo stato che mi hai chiesto, ovvero: " + integer))
                //se status code è vuoto viene eseguito questo, che restituisce sempre un ResponseEntity ma in modo diverso
                .orElseGet(() -> new ResponseEntity<>("Restituisco lo status code di defaul: 200", HttpStatus.OK));

        /*
        L'istruzione sopra è assolutamente equivalente a fare questo
        if(statusCode.isPresent()) {
            return ResponseEntity.status(statusCode.get()).body("Restituisco lo stato che mi hai chiesto, ovvero: " + statusCode.get());
        } else {
            //questo è un altro modo per impostare uno status code, meno bello secondo me
            return new ResponseEntity<>("Restituisco lo status code di defaul: 200", HttpStatus.OK);
        }*/
    }

    /*  Prende in input nell'header un parametro chiamato "paramHeaderRequest" e lo restituisce con lo stesso valore nella response con il nome "paramHeaderResponse"
     *  http://localhost:8080/Controller/headerParamsRequestResponse
     * */
    @GetMapping("/headerParamsRequestResponse")
    public ResponseEntity<String> headerParamsInOut(@RequestHeader(value = "paramHeaderRequest", required = false) String paramHeader) {

        //restituisco il valore ricevuto nella request cambiando solo il nome del parametro nella response header
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("paramHeaderResponse", paramHeader);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(null);
    }

    //GESTIONE ECCEZIONI VALIDO SOLO PER QUESTO CONTROLLER
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body("Errore catturato all'interno del controller: " + ex.getMessage());  // HTTP 400
    }

    /*  Lancia un'eccezione che viene catturata dal ExceptionHandler definito in questa classe
     *  http://localhost:8080/Controller/catchIllegalArgumentException
     * */
    @GetMapping("/catchIllegalArgumentException")
    public void catchIllegalArgumentException(){
        throw new IllegalArgumentException("Eccezione generata nell'api /catchIllegalArgumentException");
    }

    /*  Lancia un'eccezione che viene catturata dalla classe globale ExceptionsHandler
     *  http://localhost:8080/Controller/catchNullPointerException
     * */
    @GetMapping("/catchNullPointerException")
    public void catchNullPointerException(){
        throw new NullPointerException("Eccezione generata nell'api /catchNullPointerException e verrà catturata nell'ExceptionsHandler globale");
    }

    /*  Lancia un'eccezione specifica che non ha un catturatore dedicato come nei casi precendenti,
        nè in questa classe nè nella classe globale ExceptionsHandler,
        ma verrà comunque catturata da ExceptionsHandler nel metodo che definisce la generica Exception
     *  http://localhost:8080/Controller/catchQualunqueAltraException
     * */
    @GetMapping("/catchQualunqueAltraException")
    public void catchQualunqueAltraException(){
        throw new IndexOutOfBoundsException("Eccezione generata nell'api /catchQualunqueAltraException e verrà catturata nell'ExceptionsHandler globale");
    }
    //FINE API DI CATCH EXCEPTION

    @Value("${nome.ambiente:nome non settato}")
    private String nomeAmbiente;

    /*  Mostra il valore della property "nome.ambiente"
     *  http://localhost:8080/Controller/readSingleProp
     * */
    @GetMapping("/readSingleProp")
    public String readSingleProp() {
        //settare la variabile d'ambiente spring_profiles_active=ambiente1
        return "valore della property 'nome.ambiente' = " + nomeAmbiente + " , variabile d'ambiente 'spring_profiles_active' = "+System.getenv("spring_profiles_active");
    }


    //mi serve per leggere le property usando la classe Environment di Spring
    @Autowired
    private Environment environment;

    /*  Usa la classe Environment per mostrare il valore della property "nome.ambiente"
     *  http://localhost:8080/Controller/readSinglePropUsingEnvironment
     * */
    @GetMapping("/readSinglePropUsingEnvironment")
    public String readSinglePropUsingEnvironment() {
        //settare la variabile d'ambiente spring_profiles_active=ambiente1
        return "valore della property 'nome.ambiente' letta tramite classe Environment = " + environment.getProperty("nome.ambiente");

        /*NB: questa istruzione restituisce null perchè "spring_profiles_active" è una variabile d'ambiente,
        mentre la classe Environment legge solo le properties degli yml
        environment.getProperty("spring_profiles_active")*/
    }

    @Autowired
    PropertyReader propertyReader;

    /*  Usa la classe PropertyReader interna al progetto per mostrare i valori delle properties che contiene
     *  http://localhost:8080/Controller/readMultiProps
     * */
    @GetMapping("/readMultiProps")
    public String readMultiProps(){
        return propertyReader.getProp11() + " " + propertyReader.getProp2() + " " + propertyReader.getProp12().getProp121() + " " + propertyReader.getData();
    }

    /*  Serve a loggare
     *  http://localhost:8080/Controller/log
     * */
    @GetMapping("/log")
    public String log(){
        log.trace("log di livello trace");//trace è il livello di log più fine
        log.debug("log di livello debug");//debug è il livello più comprensivo, se setti questo livello vedi tutto, trane trace
        log.info("log di livello info");//se setti info non vedrai debug
        log.warn("log di livello warn");//se setti warn, non vedrai info e debug
        log.error("log di livello error");//se setti error, vedrai solo error
        //log.fatal("log di livello fatal");//il FATAL per come è configurato viene visto sempre come ERROR
        return "Log presenti in: " + environment.getProperty("logging.file.name");
    }


    /*TODO
     * 1) www-url-encoded (FATTO)
     * 2) Cambiare il nome delle variabili dal json alla variabile java (FATTO)
     * 3) Settare lo status code (FATTO)
     * 4) Param Header su singola api (FATTO) e tutte le api tramite filtro(FATTO)
     * 5) Impostare un catturatore di eccezioni (FATTO)
     * 6) yml e lettore di properties singolo (FATTO) e multiplo (FATTO)
     * 7) Log (FATTO)
     * 8) Actuator (FATTO)
     * 9) Future e Retryable
     *
     * */
}
