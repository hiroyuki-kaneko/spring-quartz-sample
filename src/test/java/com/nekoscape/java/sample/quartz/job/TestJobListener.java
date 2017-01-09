package com.nekoscape.java.sample.quartz.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

public class TestJobListener extends JobListenerSupport {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        synchronized (this) {
            notify();
        }
    }

    synchronized void waitForJobToFinish(long timeoutMillis) throws InterruptedException {
        wait(timeoutMillis);
    }
}
