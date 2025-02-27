package and.learn.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


/**
 * Richiamato in fase di /actuator/health
 * */

//Il nome del bean AppHealthCustom sarà una prop nel json dell'actuator health
@Component("AppHealthCustom")
public class MyCustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return
                Health.up().withDetail("ServizioEsterno", "Online").withDetail("altro dettaglio","tutto ok").build();
                //Health.down().withDetail("ServizioEsterno", "Offline!").build();
                //Health.outOfService().withDetail("ServizioEsterno", "Out of Service").build();
    }


}

