package and.learn.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service che espone metodi per lavorare sulla cache.
 * La cache viene vista come una mappa chiave-valore: se la chiave è già presente nella mappa, non viene eseguito il meotodo
 * (annotato con @Cachacble) ma restituito immediatamente il valore presente in cache
 * */
@Log4j2
@Service
public class CacheService {

    //variabile incrementata di 1 ogni volta che il metodo viene invocato
    private static int numInvoke;

    /**
     * Usa la cache definita in ehcache.xml.
     * La cache contiene massimo 2 elementi, ognuno conservato per 30 secondi.
     * Se arriva un terzo elemento da inserire in cache, il più veccho viene rimosso (evict)
     */
    @Cacheable("cacheSpecific")
    public String metodoConCache(String input) {
        numInvoke++;
        log.info("Metodo del service con cache richiato per la " + numInvoke + "a volta dall'avvio dell'app. Input:" + input);
        return input.toUpperCase();
    }

    /**Metodo usato per ripulire la cache.
     * L'annotation mi permette di specificare quale cache pulire e quanto pulirle,
     * infatti potrei pulirne più di una e non tutte quelle che uso*/
    @CacheEvict(cacheNames = {"cacheSpecific"}, allEntries = true)
    public void evictCache() {
        log.info("Svuoto la cache");
    }

    /** Aggiorna un singolo valore della cache con il nuovo valore associato.
     * Attraverso la prop "key" si specifica il nome della variabile (in input al metodo) che contiene la chiave-input dell'entry nella cache-mappa
        NB: Il metodo DEVE restituire il nuovo valore ricevuto, altrimenti da errore
     */
    @CachePut(value = "cacheSpecific", key = "#chiave")
    public String updateValueInCache(String chiave, String newValue) {
        log.info("Valore aggiornato per la chiave: " + chiave + ", nuovo valore: " + newValue);
        return newValue;
    }
}
