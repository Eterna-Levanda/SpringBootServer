package and.learn.batch.errorhandler;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

    private final IllegalArgumentException EXCEPTION_TO_SKIP = new IllegalArgumentException();
    private final IllegalAccessException EXCEPTION_TO_RETRY = new IllegalAccessException();
    private final Exception EXCEPTION_NOT_HANDLED = new Exception();


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
                .<Integer, Integer>chunk(2, transactionManager)

                //gestione degli errori
                .faultTolerant()
                .skip(EXCEPTION_TO_SKIP.getClass())//per saltare le eccezioni specificate
                .skip(EXCEPTION_TO_RETRY.getClass())
                .retry(EXCEPTION_TO_RETRY.getClass()) //per riprovare in caso di eccezioni specifiche
                //.noRollback(EXCEPTION_TO_RETRY.getClass()) //non rollbackare il chunk per queste eccezioni
                .retryLimit(2)//numero massimo di volte in cui un elemento viene processato a fronte di un'eccezione di retry
                .reader(createReaderPhase())
                .processor(createProcessorPhase())
                .writer(createWriterPhase())
                .build();
    }

    //CONFIGURAZIONE DELLE 3 FASI
    @Bean
    public ListItemReader<Integer> createReaderPhase() {
        return new ListItemReader<>(Arrays.asList(1, 2, 3, 4));
    }

    @Bean
    public ItemProcessor<Integer, Integer> createProcessorPhase() {

        return n -> {

            log.debug("Processo il numero {}", n);
            if(n == 1) {
                log.debug("Viene lanciata un'eccezione che fa skippare l'elemento");
                throw EXCEPTION_TO_SKIP;
            } else if (n == 2) {
                log.debug("Viene lanciata un'eccezione per tentare un retry dell'elemento");
                throw EXCEPTION_TO_RETRY;
            } else if(n == 4) {
                log.debug("Viene lanciata un'eccezione non gestita dallo step");
                throw EXCEPTION_NOT_HANDLED;
            }

            return n;
        };
    }


    /**
     * Puoi scegliere di creare un'istanza di ItemWriter tramite functional interface,
     * oppure creando e istanzando una classe che implementi ItemWriter.
     */
    @Bean
    public ItemWriter<Integer> createWriterPhase() {

        //Metodo con lambda
        return chunk -> {
            log.debug("Chunk items: " + chunk.getItems());
        };

    }


}