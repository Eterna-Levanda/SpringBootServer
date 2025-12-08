package and.learn.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    private JobLauncher jobLauncher;

    /*Spring cerca un bean di tipo Job col nome della variabile.
    * Se vuoi un nome diverso devi specificarlo come parametro a Qualifier*/
    @Autowired
    private Job basicJob;

    @Autowired
    private Job complexJob;

    @Autowired
    private Job errorManagerJob;

    @Autowired
    @Qualifier("batchWithDecider")
    private Job deciderJob;

    /**Lancia un batch base: 1 job e 1 step.
     * Per provarlo:
     * http://localhost:8080/batch/startBasicBatch
     * */
    @GetMapping("/startBasicBatch")
    public String startBasicBatch() throws Exception {
        //notare la lista di parametri in input al metodo vuota
        JobExecution execution = jobLauncher.run(basicJob, new JobParameters());
        return "Job lanciato con status: " + execution.getStatus();
    }

    /**Lancia un batch complesso: 2 job e 3 step in tutto.
     * Per provarlo:
     * http://localhost:8080/batch/startComplexBatch
     * */
    @GetMapping("/startComplexBatch")
    public String startComplexBatch() throws Exception {

        //Parametri in input al Job
        JobParameters params = new JobParametersBuilder()
                .addString("nomeUtente", "andrea")
                // per rendere il job univoco e poterlo riavviare ogni volta
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(complexJob, params);
        return "Job lanciato con status: " + execution.getStatus();
    }

    /**Lancia un batch avanzato: 1 job e 2 step, con un decider.
     * Per provarlo:
     * http://localhost:8080/batch/deciderBatch
     * */
    @GetMapping("/deciderBatch")
    public String deciderBatch() throws Exception {
        JobParameters params = new JobParametersBuilder()
                // per rendere il job univoco e poterlo riavviare ogni volta
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(deciderJob, params);
        return "Job avanzato lanciato con status: " + execution.getStatus();
    }

    /**Lancia un batch con gestione degli errori
     * Per provarlo:
     * http://localhost:8080/batch/errorManagerJob
     * */
    @GetMapping("/errorManagerJob")
    public String errorManagerJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                // per rendere il job univoco e poterlo riavviare ogni volta
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(errorManagerJob, new JobParameters());
        return "Job lanciato con status: " + execution.getStatus();
    }


}