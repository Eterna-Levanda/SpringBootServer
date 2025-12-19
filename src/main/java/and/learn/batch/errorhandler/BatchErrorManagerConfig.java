package and.learn.batch.errorhandler;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

/*
    Batch con un solo job e un solo step
    con gestione degli errori
*/
@Log4j2
@Configuration
public class BatchErrorManagerConfig {

    private final IllegalArgumentException EXCEPTION_TO_SKIP = new IllegalArgumentException("Eccezione definita per lo skip dell'elemento");
    private final IllegalAccessException EXCEPTION_TO_RETRY = new IllegalAccessException("Eccezione definita per il retry dell'elemento");
    private final ArrayIndexOutOfBoundsException EXCEPTION_TO_NO_SKIP = new ArrayIndexOutOfBoundsException("Eccezione definita per NON SKIPPARE l'elemento e interrompere il batch");
    private static final String NOME_FASE_ERRORE = "Fase fallita";

    //CONFIGURAZIONE DEL JOB
    @Bean()
    public Job errorManagerJob(JobRepository jobRepository, Step errorManagerStep) {
        return new JobBuilder("JobErrorManager", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(errorManagerStep)
                .build();
    }

    //CONFIGURAZIONE DELLO STEP
    @Bean
    public Step errorManagerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("StepErrorManager", jobRepository)
                .<Integer, Integer>chunk(4, transactionManager)

                //gestione degli errori
                .faultTolerant()
                //poltiche si skip, per scartare un elemento
                .skip(EXCEPTION_TO_SKIP.getClass())//per saltare le eccezioni specificate
                .skipLimit(2)//numero massimo di elementi che si possono skippare, al successivo il batch si interrompe

                /*Con questa configurazione viene definito che lo skip dell'elemento che causa un'eccezione
                 viene fatto per tutte le Exception tranne quella specificata nel metodo noSkip.
                 Scegli tu se usare skip() oppure skip() + noSkip().
                .skip(Exception.class)
                .noSkip(EXCEPTION_TO_NO_SKIP.getClass())*/

                //politiche di retry per riprovare l'intero chunk quando un elemento lancia eccezione di retry
                .retry(EXCEPTION_TO_RETRY.getClass()) //per riprovare l'intero chunk in caso di eccezioni specifiche
                .retryLimit(2) //se retryLimit = N, un elemento può lanciare eccezione di retry ed essere RI-processato solo N-1 volte, all'N-esima volta che lancia eccezione di retry viene scartato o causare l'interruzione del batch.
                .skip(EXCEPTION_TO_RETRY.getClass()) //in questo modo viene definito cosa deve fare Spring nel caso in cui un elemento lanci un'eccezione di retry (ovvero definita nel metodo retry) per un numero di volte pari a retryLimit, ovvero scartare l'elemento (skip). Se così non fosse allora il batch verrebbe interrotto dopo il recovery del chunk sugli elementi andati a buon fine.

                .reader(createReaderPhase())
                .processor(createProcessorPhase())
                .writer(createWriterPhase())

                .listener(new ChunkListener() {

                    /*Questo metodo viene invocato a seguito di tutte le eccezioni lanciate durante l'esecuzione del batch,
                    compresa l'eccezione finale che termina il job (es: RetryExceededException e SkipLimitExceededException).
                     */
                    @Override
                    public void afterChunkError(ChunkContext context) {

                        //recupero il parametro passato a runtime che indica la fase in cui si è verificato l'errore
                        Object nomeFaseErrore = context.getStepContext().getStepExecution().getExecutionContext().get(NOME_FASE_ERRORE);
                        log.debug("Eccezione lanciata che ha terminato il chunk: {}, durante la fase {}", context.getAttribute("sb_rollback_exception"), nomeFaseErrore);
                    }
                })
                .listener(new ItemProcessListener<>() {

                    @Override
                    public void onProcessError(Integer item, Exception e) {
                        log.debug("Eccezione durante la fase di process. Item in errore: {}, eccezione: {}", item, e.toString());
                        setParametroNomeFaseErrore("Process");
                    }
                })
                .listener(new ItemReadListener<>() {
                    @Override
                    public void onReadError(Exception ex) {
                        log.debug("Eccezione durante la fase di lettura: {}", ex.toString());
                        setParametroNomeFaseErrore("Read");
                    }
                })
                .listener(new ItemWriteListener<>() {

                    @Override
                    public void onWriteError(Exception exception, Chunk<? extends Integer> items) {
                        log.debug("Eccezione durante la fase di scrittura: {}", exception.toString());
                        if (items != null && !items.isEmpty()) {
                            setParametroNomeFaseErrore("Write");
                        }
                    }
                })
                .build();
    }

    //CONFIGURAZIONE DELLE 3 FASI
    @Bean
    @StepScope
    public ListItemReader<Integer> createReaderPhase() {
        return new ListItemReader<>(Arrays.asList(1, 2, 3, 4));
    }

    @Bean
    @StepScope
    public ItemProcessor<Integer, Integer> createProcessorPhase() {

        return n -> {

           log.debug("Processo il numero {}", n);
           if(n == 1) {
                log.debug("Viene lanciata un'eccezione che fa skippare l'elemento");
                throw EXCEPTION_TO_SKIP;
           } else if (n == 3) {
                log.debug("Viene lanciata un'eccezione per tentare un retry dell'elemento");
                throw EXCEPTION_TO_RETRY;
           } else if (n == 4) {
               log.debug("Viene lanciata un'eccezione che NON fa skippare l'elemento e interrompe il batch dopo il recovery del chunk sugli elementi andati a buon fine");
               throw EXCEPTION_TO_NO_SKIP;
           }

            return n;
        };
    }

    /**
     * Puoi scegliere di creare un'istanza di ItemWriter tramite functional interface,
     * oppure creando e istanzando una classe che implementi ItemWriter.
     */
    @Bean
    @StepScope
    public ItemWriter<Integer> createWriterPhase() {

        return chunk -> {
            log.debug("Chunk items: " + chunk.getItems());
        };
    }

    /* Salva un parametro a runtime sul nome della fase che ha dato errore, per poi leggerla nel ChunkListener*/
    private void setParametroNomeFaseErrore(String nomeFase) {
        if (StepSynchronizationManager.getContext() != null && StepSynchronizationManager.getContext().getStepExecution() != null) {
            StepSynchronizationManager.getContext().getStepExecution().getExecutionContext().put(NOME_FASE_ERRORE, nomeFase);
        }
    }
}