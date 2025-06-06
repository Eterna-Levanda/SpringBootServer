package and.learn;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

/* Necessario per far rilevare @WebFilter*/
@ServletComponentScan
//Abilita la funzionalità di Retry usata da vari service
@EnableRetry
//Abilita la funzionalità di cache con EhCache
@EnableCaching
@SpringBootApplication
public class SpringBootServerApplication {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpringBootServerApplication.class, args);

       /* System.out.println("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }*/
    }

    //I metodi annotati con @Bean o stanno nella classe SpringBootApplication o in una annotata con @Configuration, come AsyncMethodConfiguration
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

           /*System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }*/

        };
    }


}
