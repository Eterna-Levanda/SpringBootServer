package and.learn.mains;



/**
 * Questi metodi sono curiosi perchè mostrano come,
 * per restituire un'istanza (di un'interfaccia funzionale),
 * posso usare vari modi,
 * che di per sè non sono niente di nuovo,
 * ma sono esposti in modo da passare da quello più "classico" (il primi)
 * a quello che è meno immediatamente comprensibile
 * */
import java.util.function.Function;

public class LambdaReturnDirectly {

    /**Metodo java < 8 con classe anonima*/
    public Function<String, String> method0() {
        return new Function<String, String>() {
            @Override
            public String apply(String item) {
                return item.toUpperCase();
            }
        };
    }

    /**Metodo con java >= 8 con funzione lambda esplicita*/
    public Function<String, String> method1() {
        Function<String, String> toUpperCase = (String s) -> s.toUpperCase();
        return toUpperCase;
    }

    /**Uguale al precedente ma senza dichiare il tipo in input alla funzione lambda*/
    public Function<String, String> method2() {
        Function<String, String> toUpperCase = s -> s.toUpperCase();
        return toUpperCase;
    }

    /**Uguale al precedente ma con la lambda usata con method reference e variabile dichiara da restituire*/
    public Function<String, String> method3() {
        Function<String, String> toUpperCase = String::toUpperCase;
        return toUpperCase;
    }

    /**
     * Uguale al precedente, ma con return diretto tramite lambda.
     *
     * Questo metodo può apparire strano se lo si vede la prima volta, ma seguendo i metodi precedenti lo si capisce meglio.
     * Appare strano perchè mostra che deve essere retituito un Function quando l'implementazione del metodo contiene solo un toUpperCase.
     * In realtà ad un'osservazione più attenta si vede che il metodo crea e resituisce una lambda,
     * ovvero l'implementazione dell'unico metodo non di default dell'interfaccia funzionale Function*/
    public Function<String, String> method4() {
        return String::toUpperCase;
    }
}

