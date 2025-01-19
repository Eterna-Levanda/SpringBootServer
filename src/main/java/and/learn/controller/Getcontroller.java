package and.learn.controller;

import org.springframework.web.bind.annotation.*;

@RestController("nomeBeanCrudController")
@RequestMapping("/GetController")
public class Getcontroller {

    /*
    * METODI CON LAVORI SUI PARAMETRI IN INPUT
    *
    * */
    @GetMapping("/get")
    public String get() {
        return "get!";
    }

    @GetMapping("/getWithRequestParams")
    //http://localhost:8080/crud/getWithRequestParams?valueString=prova&integer=45&bool=true
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
    //http://localhost:8080/GetController/getWithPathParams/ciao/2
    public String getWithPathParams(@PathVariable String param,
                                    @PathVariable(value = "param2") Integer paramInteger){
        return "getWithPathParams. param = " + param + ", param2 = " + paramInteger;
    }

    @GetMapping(value = { "/getWithPathParamsOptional", "/getWithPathParamsOptional/{param}", "/getWithPathParamsOptional/{param}/{anotherParam}" })
    //http://localhost:8080/GetController/getWithPathParamsOptional/ciao/erre
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


    /*TODO
    *  Provare un input complesso magari con:
    * 1) Param Header
    * 1) il Validate per controllare la validità dei campi
    * 2) Cambiare il nome della variabili dal json alla variabile java
    * */

    /*
     * TODO Provare un output complesso
     * */

    /*
     * TODO Impostare un catturatore di eccezioni
     * */
}
