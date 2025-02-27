package and.learn.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
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

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> customInfo = new HashMap<>();
        customInfo.put("buildTimestamp", System.currentTimeMillis());
        //qui leggo una prop dallo yml
        customInfo.put("environment", environment.getProperty("nome.ambiente"));
        customInfo.put("Sistema Operativo", System.getProperty("os.name"));
        builder.withDetail("custom-info", customInfo);
    }
}
