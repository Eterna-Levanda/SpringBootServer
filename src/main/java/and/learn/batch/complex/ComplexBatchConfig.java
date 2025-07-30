package and.learn.batch.complex;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

/*
    Batch complesso dove al termine del primo Job (composto da 2 Step) si vuole eseguire un altro Job (composto da un solo step).
    Per concatenare 2 job è necessario però che il secondo Job venga wrappato in uno Step che di fatto sarà il terzo step del job 1.
    Dunque l'archiettura 'concettaule' del batch è la seguente:
    Archietettura del batch definito in questa classe:
    Job1
        Step1Job1
            Reader
            Processor
            Writer
        Step2Job1
            Reader
            Processor
            Writer
    Job2
        Step1Job2
            Reader
            Processor
            Writer

    Ma in realtà è questa:
    Job1
        Step1Job1
            Reader
            Processor
            Writer
        Step2Job1
            Reader
            Processor  => Legge un parametro in input al Job
            Writer
        Job2Wrapper (Step che wrappa il Job2)
            Job2
                Step1Job2
                    Reader => Legge un parametro in input al Job
                    Processor
                    Writer

    Parametri in input => Sono descritti nel file online ma non li ho provati
    Lavora con Chunk.
    TODO Altre configurazioni aggiuntive.


*/
@Log4j2
@Configuration
public class ComplexBatchConfig {

    //CONFIGURAZIONE DEL JOB 1, QUELLO DI PARTENZA

    /*Il numero di Step in input dipende da quanti e quali Step vuoi utilizzare.
    * I nomi dei parametri Step hanno il nome del bean definito dai metodi sotto che restituiscono Step,
    * se vuoi nomi diversi devi usare l'annotation @Qualifier
    * */
    @Bean("complexJob")
    public Job getJob1(JobRepository jobRepository, Step step1Job1, Step step2Job1, Step job2Wrapper) {
        return new JobBuilder("Job1", jobRepository)
                .incrementer(new RunIdIncrementer())
                //definisco lo step di partenza
                .start(step1Job1)
                //specifico qual e' il secondo step.
                .next(step2Job1)
                //specifico qual è il secondo job da lanciare al termine del primo, ovvero uno Step che wrappa il Job2
                .next(job2Wrapper)
                .listener(new JobExecutionListener() {
                    /*Definisco un listener che viene eseguito prima dell'esecuzione del Job.
                    * In questo listener viene letto un parametro passato in input al batch*/
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        //lettura parametro nomeUtente passato in input al Job
                        String nomeUtente = jobExecution.getJobParameters().getString("nomeUtente");
                        log.debug("Parametro in input al job: " + nomeUtente);
                    }
                })
                .build();
    }

    //CONFIGURAZIONE DELLO STEP 1 del JOB 1
    @Bean("step1Job1")
    public Step getStep1Job1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("Step1 Job1", jobRepository)
                //Quando definisco il chunk specifico che tipo di dati legge e scrive il processor
                .<Integer, String>chunk(2, transactionManager)
                .reader(readerJob1Step1())
                .processor(processorJob1Step1())
                .writer(writerJob1Step1())
                .build();
    }

    //CONFIGURAZIONE DELLO STEP 2 DEL JOB 1
    @Bean("step2Job1")
    public Step getStep2Job1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("Step2Job1", jobRepository)
                .<Integer, Integer>chunk(2, transactionManager)
                .reader(readerJob1Step2())
                .processor(processorJob1Step2())
                .writer(writerJob1Step2())
                .build();
    }

    //CONFIGURAZIONE DEL JOB 2
    @Bean("job2")
    public Job getJob2(JobRepository jobRepository, Step step1Job2) {
        return new JobBuilder("Job2", jobRepository)
                .incrementer(new RunIdIncrementer())
                //definisco il job di partenza
                .start(step1Job2)
                .build();
    }

    //CONFIGURAZIONE DELLO STEP 1 del JOB 2
    @Bean("step1Job2")
    public Step getStep1Job2(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemReader<Integer> readerJob2Step1) {
        return new StepBuilder("Step1 Job2", jobRepository)
                .<Integer, Integer>chunk(2, transactionManager)
                .reader(readerJob2Step1)
                .processor(processorJob2Step1())
                .writer(writerJob2Step1())
                .build();
    }

    /*Siccome per un Job si possono definire solo quali sono gli Step che lo compongono,
     se deve eseguire un intero job successivo, occorre definire uno Step "wrapper" che contiene il Job 2 da eseguire successivamente.
     Il nome corretto per questo wrapper è JobStep.
     Questa è la definizione dello Step che wrappa il Job 2 da eseguire.
     */
    @Bean("job2Wrapper")
    public Step job2AsStep(JobRepository jobRepository, Job job2) {
        return new StepBuilder("Job2 Wrapper", jobRepository)
                .job(job2)
                .build();
    }

    //CONFIGURAZIONE DELLE 3 FASI DELLO STEP 1 DEL JOB 1
    @Bean()
    public ListItemReader<Integer> readerJob1Step1() {
        return new ListItemReader<>(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Bean()
    public ItemProcessor<Integer, String> processorJob1Step1() {
        //notare come in questo caso il processor cambi il tipo di dato: prende un Integer e restituisce una String
        return n -> {
            log.debug("Processing number: " + n + " in Step 1 of Job 1");
            return "Il doppio di " + n + " e' " + (n * 2);
        };
    }

    @Bean()
    public ItemWriter<String> writerJob1Step1() {
        return chunk -> log.debug("Writing chunk: " + chunk.getItems() + " in Step 1 of Job 1");
    }

    //CONFIGURAZIONE DELLE 3 FASI DELLO STEP 2 DEL JOB 1
    @Bean()
    public ListItemReader<Integer> readerJob1Step2() {
        return new ListItemReader<>(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Bean
    /*
    I bean annotati con @StepScope vengono inizializzati solo quando lo step associato viene eseguito
    e ne esiste un'istanza diversa per ogni esecuzione dello step, quindi non si tratta di Bean Singleton come gli altri!
    In questo modo non si rischia la sovrascrittura di parametri dovuto ad esecuzioni multiple dello stesso step.
    Si potrebbe usare anche @JobScope (in modo che viva per tutto il tempo del job) dovrebbe cambiare solo la durata di vita del Bean
    */
    @StepScope
    public ItemProcessor<Integer, Integer> processorJob1Step2() {

        return new ItemProcessor<Integer, Integer>() {

            //per leggere il parametro nomeUtente passato in input al Job, si usa l'annotation @Value
            @Value("#{jobParameters['nomeUtente']}")
            private String nomeUtenteParametro;

            @Override
            public Integer process(Integer n) throws Exception {
                //recupero del valore del parametro nomeUtente passato al Job tramite StepSynchronizationManager
                String nomeUtente = StepSynchronizationManager.getContext().getStepExecution().getJobParameters().getString("nomeUtente");
                log.debug("Parametro nomeUtente preso dinamicamente dal StepSynchronizationManager: " + nomeUtente);

                log.debug("Parametro nomeUtente passato al Processor del Job 1: " + nomeUtenteParametro);

                log.debug("Processing number: " + n + " in Step 2 of Job 1");
                return n * 2;
            }
        };
    }

    @Bean()
    public ItemWriter<Integer> writerJob1Step2() {
        return chunk -> log.debug("Writing chunk: " + chunk.getItems() + " in Step 2 of Job 1");
    }

    //CONFIGURAZIONE DELLE 3 FASI DELLO STEP 1 DEL JOB 2
    @Bean()
    /*
    I bean annotati con @StepScope vengono inizializzati solo quando lo step associato viene eseguito
    e ne esiste un'istanza per ogni esecuzione dello step, quindi non si tratta di Bean Singleton come gli altri!
    In questo modo non si rischia la sovrascrittura di parametri dovuto ad esecuzioni multiple dello stesso step.
    Si potrebbe usare anche @JobScope non dovrebbe cambiare nulla, solo la durata di vita del Bean
    */
    @StepScope
    public ListItemReader<Integer> readerJob2Step1(@Value("#{jobParameters['nomeUtente']}") String nomeUtente) {
        log.debug("Parametro nomeUtente passato al Reader del Job 2: " + nomeUtente);
        return new ListItemReader<>(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Bean()
    public ItemProcessor<Integer, Integer> processorJob2Step1() {
        return n -> {
            log.debug("Processing number: " + n + " in Step 1 of Job 2");
            return n * 2;
        };
    }

    @Bean()
    public ItemWriter<Integer> writerJob2Step1() {
        return chunk -> log.debug("Writing chunk: " + chunk.getItems() + " in Step 1 of Job 2");
    }
}