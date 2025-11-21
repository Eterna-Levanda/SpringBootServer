package and.learn.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Richiamato in fase di /actuator/info
 * */
@Component
public class MyInfoContributor implements InfoContributor {

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext ctx;

    //lettura dallo yml
    @Value("${nome.ambiente}")
    private String nomeAmbiente;

    @Override
    public void contribute(Info.Builder builder) {

        Map<String, Object> customInfo = new HashMap<>();
        customInfo.put("buildTimestamp", System.currentTimeMillis());
        customInfo.put("sistema operativo", System.getProperty("os.name"));
        //Lettura di property dallo yml in 2 modi diversi
        customInfo.put("nomeAmbiente1", environment.getProperty("nome.ambiente"));
        customInfo.put("nomeAmbiente2", nomeAmbiente);

        Map<String, String> buildDetails = new HashMap<>();
        buildDetails.put("package", getClass().getPackage().getName());

        //Lettura di valori dall'ApplicationContext di Spring
        Map<String, Object> contextDetails = new HashMap<>();
        contextDetails.put("bean-definition-count", ctx.getBeanDefinitionCount());
        contextDetails.put("startup-date", ctx.getStartupDate());

        //struttura esterna del json info mostrato a video
        builder.withDetail("custom-info", customInfo);
        builder.withDetail("context", contextDetails);
        builder.withDetail("build", buildDetails);
    }
}
