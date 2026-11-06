package and.learn.future;

import java.math.BigDecimal;

/**
 * Oggetto restituito da ogni thread: rappresenta il risultato del calcolo del premio di una proposta.
 */
public record DatiPolizza(int numeroProposta, BigDecimal premio, String nomeThread) {
}
