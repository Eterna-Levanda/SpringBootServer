package and.learn.controller;

import and.learn.request.Esterno;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController("nomeBeanCrudController")
@RequestMapping("/Controller")
public class GetController {

    /*
    * METODI CON LAVORI SUI PARAMETRI IN INPUT
    *
    * */
    @GetMapping("/get")
    //http://localhost:8080
    public String get() {
        return "get!";
    }

    @GetMapping("/getWithRequestParams")
    //http://localhost:8080/Controller/getWithRequestParams?valueString=prova&integer=45&bool=true
    public String getWithRequestParams(
            //definisco solo la variabile, quindi il nome sarà il nome del parametro sarà uguale al nome della variabile e l'obbligatorietà è true
            @RequestParam String valueString,
            //definisco il nome del parametro sovrascrivendo quello della variabile, l'obbligatorietà è true
            @RequestParam(name = "integer") Integer valueInteger,
            //definisco solo l'obbligatorietà a false, il nome del parametro sarà quello della variabile
            @RequestParam(required = false) Boolean bool) {
        return "getWithRequestParams. valueString = " + valueString + ", valueInteger = " + valueInteger + ", bool = " + bool;
    }

    @GetMapping("/getWithPathParams/{param}/{param2}")
    //http://localhost:8080/Controller/getWithPathParams/ciao/2
    public String getWithPathParams(@PathVariable String param,
                                    @PathVariable(value = "param2") Integer paramInteger){
        return "getWithPathParams. param = " + param + ", param2 = " + paramInteger;
    }

    @GetMapping(value = { "/getWithPathParamsOptional", "/getWithPathParamsOptional/{param}", "/getWithPathParamsOptional/{param}/{anotherParam}" })
    //http://localhost:8080/Controller/getWithPathParamsOptional/ciao/erre
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

    @PostMapping("/post")
    /*http://localhost:8080/Controller/post
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
    * */
    public Esterno post(@RequestBody(required = false) @Valid Esterno esterno){
        return esterno;
    }

    /*TODO
     * 1) Param Header
     * 2) Cambiare il nome della variabili dal json alla variabile java
     * 3) Cambiare la response settando lo status code
     * 4) Output complesso formattando le date, per esempio e cambiando il nome delle variabili
     * 5) Impostare un catturatore di eccezioni
     * */
}
