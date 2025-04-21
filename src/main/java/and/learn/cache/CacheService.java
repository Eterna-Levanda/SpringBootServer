package and.learn.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CacheService {

    //variabile incrementata di 1 ogni volta che il metodo viene invocato
    private static int numInvoke;

    /**Usa la cache definita in ehcache.xml.
     * La cache contiene massimo 2 elementi, ognuno conservato per 20 secondi.
     * Se arriva un terzo elemento da inserire in cache, il più veccho viene rimosso (evict)*/
    @Cacheable("cacheBase")
    public String metodoConCache(String input) {
        numInvoke++;
        log.info("Metodo del service con cache richiato per la "+numInvoke+"a volta");
        return input.toUpperCase();
    }
}
