package and.learn.mains;

import java.util.Optional;

/**
 * Questa classe serve a mostrare la differenza di utilizzo tra i metodi della classe Optional:
 * OrElse e orElseGet.
 *
 * Tenendo a mente che:
 * 1) Il metodo OrElse prende in input un valore e:
 *  se l'Optional è pieno restituisce cuò che contiene
 *  altrimenti restituisce quel valore passato in input al metodo OrElse
 *
 * 2) Il metodo orElseGet invece prende in input una funzione (Interface Supplier, metodo get)
 * che viene eseguita solo se l'optinal è vuoto.
 *
 * Osserviamo che:
 * Quando viene invocato il metodo OrElse e orElseGet gli deve essere passato un valore.
 * Quando viene eseguito il metodo OrElse, per potergli passare in input un valore, deve essere eseguito il metodo metodoEseguitoAncheSeOptionaEVuoto.
 * Quando invece viene eseguito il metodo orElseGet gli basta ricevere in input una funzione, la quale verrà eseguita solo se l'optional non è vuoto.
 *
 * In conclusione:
 * Quello che ci mostra questo esempio è che il metodo metodoEseguitoAncheSeOptionaEVuoto:
 * 1) viene sempre eseguito solo se richiamato abbianto a OrElse,
 * 2) viene eseguito solo se l'optional è vuoto se lo usiamo abbinato al metodo OrElseGet.
 *
 * In sostanza fare:
 * Optional.ofNullable(valore).orElseGet(metodo());
 * è la scelta migliore se vogliamo implementare qualosa equivalente a questo:
 * if(valore!=null)
 *      return valore;
 * else
 *      return metodo();
 *
 * */
public class OptionalOrElseGet {

    /**
     Output di questa classe:

     esempio con nome valorizzato e orElseGet e nome valorizzato
     Sono entrato nel ramo toUpperCase
     MARIO

     esempio con nome vuoto e orElseGet e nome vuoto
     Sono entrato nel metodo metodoEseguitoAncheSeOptionaEVuoto
     Valore di default

     esempio usando il metodo orElse con optional valorizzato
     Sono entrato nel ramo toUpperCase
     Sono entrato nel metodo metodoEseguitoAncheSeOptionaEVuoto
     MARIO

     esempio usando il metodo orElse con optional vuoto
     Sono entrato nel metodo metodoEseguitoAncheSeOptionaEVuoto
     Valore di default

     * */



    public static void main(String[] args) {

        //**********   2 ESEMPI CON OR_ELSE_GET

        //esempio con nome valorizzato e usando il metodo orElse
        System.out.println("esempio con nome valorizzato e orElseGet e nome valorizzato");
        Optional<String> nome = Optional.of("Mario");
        String risultato = getValoreConMetodoOrElseGet(nome);
        System.out.println(risultato + "\n");  // Output: MARIO

        //esempio con nome vuoto e usando il metodo orElse
        System.out.println("esempio con nome vuoto e orElseGet e nome vuoto");
        Optional<String> nomeVuoto = Optional.empty();
        String risultatoVuoto = getValoreConMetodoOrElseGet(nomeVuoto);
        System.out.println(risultatoVuoto + "\n");  // Output: Default




        //**********   2 ESEMPI CON OR_ELSE
        //esempio usando il metodo orElse e con optional valorizzato
        System.out.println("esempio usando il metodo orElse con optional valorizzato");
        Optional<String> nomeVal = Optional.of("Mario");
        risultato = getValoreConMetodoOrElse(nomeVal);
        System.out.println(risultato + "\n");  // Output: MARIO

        //esempio usando il metodo orElse e con optional vuoto
        System.out.println("esempio usando il metodo orElse con optional vuoto");
        nomeVal = Optional.empty();
        risultato = getValoreConMetodoOrElse(nomeVal);
        System.out.println(risultato + "\n");  // Output: Valore di default
    }

    private static String getValoreConMetodoOrElseGet(Optional<String> optional) {
        return optional
                .map(s -> {
                    // Se presente, converte in maiuscolo
                    System.out.println("Sono entrato nel ramo toUpperCase");
                    return s.toUpperCase();
                })
                .orElseGet(
                        /*Al metodo orElseGet viene passata in input una funzione,
                        * la quale verrà eseguita solo se l'optional è vuoto,
                        * altrienti no e il metodo metodoEseguitoAncheSeOptionaEVuoto non verrà eseguito*/
                        OptionalOrElseGet::metodoEseguitoAncheSeOptionaEVuoto
                );
    }

    private static String getValoreConMetodoOrElse(Optional<String> optional) {
        return optional
                .map(s -> {
                    System.out.println("Sono entrato nel ramo toUpperCase");
                    return s.toUpperCase();
                })
                .orElse(
                        /*Per passare in input un valore al metodo orElse
                         deve per forza essere eseguito il metodo metodoEseguitoAncheSeOptionaEVuoto,
                         sia se l'optional sia se vuoto.
                         Anzi, non c'entra proprio nulla se l'optional è pieno oppure no,
                         si tratta semplicemente di invocare il metodo orElse
                         il quale richiede in input un valore e non una funzione*/
                        metodoEseguitoAncheSeOptionaEVuoto()
                );
    }

    private static String metodoEseguitoAncheSeOptionaEVuoto() {
        System.out.println("Sono entrato nel metodo metodoEseguitoAncheSeOptionaEVuoto");
        return "Valore di default";
    }
}
