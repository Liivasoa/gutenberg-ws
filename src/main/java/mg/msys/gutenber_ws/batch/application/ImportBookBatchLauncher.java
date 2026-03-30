package mg.msys.gutenber_ws.batch.application;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ImportBookBatchLauncher {

    private final JobOperator jobOperator;
    private final Job importBookJob;

    public ImportBookBatchLauncher(JobOperator jobOperator, @Qualifier("importBookJob") Job importBookJob) {
        this.jobOperator = jobOperator;
        this.importBookJob = importBookJob;
    }

    public JobExecution launch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("requestedAt", System.currentTimeMillis())
                .toJobParameters();

        return jobOperator.start(importBookJob, jobParameters);
    }
}
