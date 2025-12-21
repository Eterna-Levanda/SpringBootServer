package and.learn.batch.basic;

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

/**
    Batch con un solo job e un solo step.
    Nessun parametro in input
    Lavora con Chunk.
    Nessuna configurazione aggiuntiva.
*/
@Log4j2
@Configuration
public class BasicBatchConfig {

    //CONFIGURAZIONE DEL JOB
    @Bean(/*Il nome del bean Job sarà uguale al nome del metodo,
    se lo vuoi diverso devi specificarlo dentro l'annotation*/)
    public Job basicJob(JobRepository jobRepository, Step basicStep
    /*Il nome della variabile basicStep è uguale al nome del bean,
    altrimenti dovresti usare un @Qualifier per specificare quale Bran di tipo Step vuoi utilizzare*/) {
        return new JobBuilder("Nome Job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(basicStep)
                .build();
    }

    //CONFIGURAZIONE DELLO STEP
    @Bean
    public Step basicStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("Nome Step", jobRepository)
                .<Integer, Integer>chunk(2, transactionManager)
                .reader(createReader())
                .processor(createProcessor())
                .writer(createWriter())
                .build();
    }

    //CONFIGURAZIONE DELLE 3 FASI
    @Bean
    public ListItemReader<Integer> createReader() {
        return new ListItemReader<>(Arrays.asList(1, 2, 3, 4, 5));
    }

    /**Puoi scegliere di creare un'istanza di ItemProcessor tramite functional interface,
     * oppure creando e istanzando una classe che implementi ItemProcessor*/
    @Bean
    public ItemProcessor<Integer, Integer> createProcessor() {

        //Metodo con lambda
        /*return n -> {
            log.debug("Processing number: " + n);
            return n*2; // Esempio di elaborazione: moltiplica il numero per 2
        };*/

        //metodo con classe che implementa ItemProcessor
        return new NumberProcessor();
    }

    /**Puoi scegliere di creare un'istanza di ItemWriter tramite functional interface,
     * oppure creando e istanzando una classe che implementi ItemWriter.
     */
    @Bean
    public ItemWriter<Integer> createWriter() {

        //Metodo con lambda
        //return chunk -> log.debug("Writing chunk: " + chunk.getItems());

        //Metodo con classe che implementa ItemWriter
        return new NumberWriter();
    }
}