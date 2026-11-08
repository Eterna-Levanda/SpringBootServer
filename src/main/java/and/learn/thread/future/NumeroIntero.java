package and.learn.thread.future;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * Oggetto che funge da output per ogni thread.
 * Contiene il risultato di un calcolo su numeri interi.
 */
@Data
@AllArgsConstructor
public class NumeroIntero {
    private BigInteger numero;
    private String nomeThread;
}
