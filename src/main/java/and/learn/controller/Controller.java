package and.learn.controller;

import and.learn.request.plain.Esterno;
import and.learn.request.propsrinominate.EsternoPropsRinominate;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
                                    @PathVariable(value = "param2") Integer paramInteger){
        return "getWithPathParams. param = " + param + ", param2 = " + paramInteger;
    }

    /*
    http://localhost:8080/Controller/getWithPathParamsOptional/ciao/erre
    * PathVariable (detto anche pathParam con altre librerie) opzionali con rinomina dei parametri
    * */
    @GetMapping(value = { "/getWithPathParamsOptional", "/getWithPathParamsOptional/{param}", "/getWithPathParamsOptional/{param}/{anotherParam}" })
    public String getWithPathParamsOptional(@PathVariable(required = false) String param,
                                            @PathVariable(name = "anotherParam", required = false) String param2) {
        if (param == null) {
            return "param null";
        } else if(param2 == null){
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
    public Esterno postComplesso(@RequestBody(required = false) @Valid Esterno esterno){
        return esterno;
    }

    /*
    http://localhost:8080/Controller/postParamFormUrlEncoded
    * Parametri passati nel body con x-www-form-urlencoded*/
    @PostMapping(value= "/postParamFormUrlEncoded", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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



    /*TODO
     * 1) www-url-encoded (FATTO)
     * 2) Cambiare il nome delle variabili dal json alla variabile java (FATTO)
     * 3) Output complesso formattando le date
     * 4) Settare lo status code
     * 5) Param Header
     * 6) Impostare un catturatore di eccezioni
     * 7) yml e lettore di properties
     * */
}
