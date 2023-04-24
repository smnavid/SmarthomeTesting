package tartan.smarthome.health;

import com.codahale.metrics.health.HealthCheck;

public class TartanHealthCheck extends HealthCheck {

    /**
     * This health check does nothing. Perhaps something more robust is needed?
     * @return health check result
     * @throws Exception for an error
     */
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
