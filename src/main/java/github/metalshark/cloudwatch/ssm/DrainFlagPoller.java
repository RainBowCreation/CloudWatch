package github.metalshark.cloudwatch.ssm;

import github.metalshark.cloudwatch.CloudWatch;
import github.metalshark.cloudwatch.DrainManager;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DrainFlagPoller implements Runnable {
    private final SsmClient ssm;
    private final String paramName;
    private final ScheduledExecutorService exec;
    private final int periodSeconds;

    public DrainFlagPoller(SsmClient ssm, String paramName, ScheduledExecutorService exec, int periodSeconds) {
        this.ssm = ssm;
        this.paramName = paramName;
        this.exec = exec;
        this.periodSeconds = periodSeconds;
    }

    @Override
    public void run() {
        if (DrainManager.isDraining()) return;
        try {
            GetParameterResponse resp = ssm.getParameter(GetParameterRequest.builder()
                    .name(paramName).withDecryption(false).build());
            String v = resp.parameter().value();
            if ("true".equalsIgnoreCase(v.trim())) {
                // fire drain
                DrainManager.drainAndShutdown(
                        "Server is scaling in; kicking players in {seconds}s…",
                        "Channel closed. Please rejoin in a moment.",
                        5
                );
                // stop polling this task
                exec.shutdown();
            }
        } catch (ParameterNotFoundException e) {
            // parameter not created yet – ignore
        } catch (Exception e) {
            CloudWatch.getPlugin().getLogger().warning("Drain poller error: " + e.getMessage());
        }
    }

    public void schedule() {
        exec.scheduleAtFixedRate(this, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }
}
