package com.nekoscape.java.sample.quartz.job;

import com.nekoscape.java.sample.quartz.dao.SampleDao;
import com.nekoscape.java.sample.quartz.job.factory.OwnJobFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SampleJobTest {
    @Mock
    private Appender mockAppender;

    @Captor
    private ArgumentCaptor<LogEvent> logCaptor;

    @MockBean
    private SampleDao sampleDao;

    @Autowired
    private ApplicationContext context;

    private Scheduler scheduler;

    @Before
    public void before() throws SchedulerException {
        Mockito.reset(mockAppender);
        Mockito.when(mockAppender.getName()).thenReturn("MockAppender");
        Mockito.when(mockAppender.isStarted()).thenReturn(true);
        Mockito.when(mockAppender.isStopped()).thenReturn(false);

        createScheduler();

        setLogLevel(Level.INFO);
    }

    @After
    public void after() throws SchedulerException {
        scheduler.shutdown(true);
    }

    @Test
    public void executeJob() throws SchedulerException, InterruptedException {
        JobKey jobKey = new JobKey("test-name", "test-group");
        TriggerKey triggerKey = new TriggerKey("test-name", "test-group");
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(triggerKey).startNow();
        JobDetail jobDetail = JobBuilder.newJob(SampleJob.class).withIdentity(jobKey).build();

        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put("ID", 1);
        jobDataMap.put("TEXT", "test-record");

        TestJobListener testJobListener = new TestJobListener();
        scheduler.getListenerManager().addJobListener(testJobListener);

        scheduler.scheduleJob(jobDetail, triggerBuilder.build());

        testJobListener.waitForJobToFinish(TimeUnit.SECONDS.toMillis(10));

        Mockito.verify(mockAppender).append(logCaptor.capture());

        assertThat(logCaptor.getValue().getMessage().getFormattedMessage(), is("start job: test-group.test-name"));
        assertThat(logCaptor.getValue().getLevel(), is(Level.INFO));

        Mockito.verify(sampleDao, times(1)).saveEntity(1, "test-record");
    }

    private void createScheduler() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        OwnJobFactory ownJobFactory = new OwnJobFactory();
        ownJobFactory.setApplicationContext(context);
        scheduler.setJobFactory(ownJobFactory);

        scheduler.start();
    }

    private void setLogLevel(Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.removeAppender("MockAppender");

        loggerConfig.setLevel(level);
        loggerConfig.addAppender(mockAppender, level, null);
        ctx.updateLoggers();
    }

}
