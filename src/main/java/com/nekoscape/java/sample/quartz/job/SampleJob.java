package com.nekoscape.java.sample.quartz.job;

import com.nekoscape.java.sample.quartz.dao.SampleDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class SampleJob extends QuartzJobBean {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private SampleDao sampleDao;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("start job: "+ jobExecutionContext.getJobDetail().getKey());

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        int id = jobDataMap.getInt("ID");
        String text = jobDataMap.getString("TEXT");

        sampleDao.saveEntity(id, text);
    }
}
