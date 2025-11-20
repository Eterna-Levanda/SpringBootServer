package and.learn.batch.advanced;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Random;

/**
 * Batch configurato in modo che ci sia un job iniziale che genera un numero casuale e,
 * a seconda che questo sia pari o dispari, il batch esegue uno step anzichè un altro.
 *
 * Notare che dove viene usato StepContext e ExecutionContext,
 * si potrebbe usare convertire usando invece i rispettivi omologhi JobContext e ExecutionContext.
 * */
@Log4j2
@Configuration
public class DeciderBatchConfig {


    @Bean("batchWithDecider")
    Job createJobOfDeciderBatch(JobRepository jobRepository, JobExecutionDecider decider, Step stepIniziale, Step stepPari, Step stepDispari){
        return new JobBuilder("advancedJob", jobRepository)
                //primo step del Job
                .start(stepIniziale)
                //il decider lo metti nel punto in cui ti serve, eventualmente anche come primo step
                .next(decider)
                //dall'esecuzione del decider, in base al risultato, si determina il prossimo step
                .from(decider).on("PARI").to(stepPari)
                .from(decider).on("DISPARI").to(stepDispari)
                .end()
                .build();
    }

    @Bean("stepIniziale")
    Step stepIniziale(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepIniziale", jobRepository)

                //per semplicità uso un tasklet, ma si potrebbero usare le 3 fasi: reader, processor, writer
                .tasklet((contribution, chunkContext) -> {
                    log.info("Esecuzione dello step iniziale");

                    /*Genero un numero casuale e lo salvo nell'ExecutionContext del Job,
                    in modo che il decider possa leggerlo e decidere il flusso.*/
                    int random = new Random().nextInt(10);
                    log.info("Numero casuale generato: " + random);
                    chunkContext.getStepContext().getStepExecution().getJobExecution()
                            .getExecutionContext()
                            .putInt("random", random);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean("decider")
    JobExecutionDecider getDecider() {

        /* Stessa implementazione ma con Lambda
        return (jobExecution, stepExecution) -> {
             int random = jobExecution.getExecutionContext().getInt("random", 0);
                random = stepExecution.getExecutionContext().getInt("random", 0);

                System.out.println("Il decider ha ricevut come parametro = " + random);

                return (random % 2 == 0) ?
                        new FlowExecutionStatus("PARI") :
                        new FlowExecutionStatus("DISPARI");
        };*/

        //Stessa implementazione ma con classe anonima
        return new JobExecutionDecider() {
            @Override
            public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

                //lettura del parametro dal JobExecutionContext, passato dallo step iniziale
                int random = jobExecution.getExecutionContext().getInt("random", 0);
                log.info("Il decider ha ricevut come parametro = " + random);

                return (random % 2 == 0) ?
                        new FlowExecutionStatus("PARI") :
                        new FlowExecutionStatus("DISPARI");
            }
        };
    }
    
    @Bean("stepPari")
    Step creaStepPari(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepPari", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Esecuzione dello step pari");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean("stepDispari")
    Step creaStepDispari(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepDispari", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Esecuzione dello step dispari");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
