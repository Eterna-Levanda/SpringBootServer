package and.learn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/* Classe usata per rendere i metodi asincroni tramite l'uso dell'annotarion:
* org.springframework.scheduling.annotation.Async;*/
@EnableAsync
@Configuration
public class AsyncMethodConfiguration {

    @Bean("executorAsync")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("PrefixThread-");
        executor.initialize();
        return executor;
    }
}
