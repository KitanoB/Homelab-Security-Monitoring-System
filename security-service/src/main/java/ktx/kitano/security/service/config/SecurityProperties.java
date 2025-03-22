package ktx.kitano.security.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.login")
public class SecurityProperties {

    /**
     * Max failed attempts before triggering a security rule.
     */
    private int maxFailures = 5;

    /**
     * Time window in minutes to count login failures.
     */
    private int failureWindowMinutes = 10;

    /**
     * Max number of distinct IPs before triggering alert.
     */
    private int maxIpCount = 3;

    // Getters and Setters
    public int getMaxFailures() {
        return maxFailures;
    }

    public void setMaxFailures(int maxFailures) {
        this.maxFailures = maxFailures;
    }

    public int getFailureWindowMinutes() {
        return failureWindowMinutes;
    }

    public void setFailureWindowMinutes(int failureWindowMinutes) {
        this.failureWindowMinutes = failureWindowMinutes;
    }

    public int getMaxIpCount() {
        return maxIpCount;
    }

    public void setMaxIpCount(int maxIpCount) {
        this.maxIpCount = maxIpCount;
    }
}