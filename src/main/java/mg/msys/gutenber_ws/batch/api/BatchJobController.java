package mg.msys.gutenber_ws.batch.api;

import java.time.Instant;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.msys.gutenber_ws.batch.application.ImportBookBatchLauncher;

@RestController
@RequestMapping("/api/v1/batch/jobs")
public class BatchJobController {

    private final ImportBookBatchLauncher importBookBatchLauncher;

    public BatchJobController(ImportBookBatchLauncher importBookBatchLauncher) {
        this.importBookBatchLauncher = importBookBatchLauncher;
    }

    @PostMapping("/import-books/executions")
    public ResponseEntity<BatchJobLaunchResponse> launchImportBooksJob() throws Exception {
        JobExecution execution = importBookBatchLauncher.launch();

        BatchJobLaunchResponse response = new BatchJobLaunchResponse(
                execution.getJobInstanceId(),
                execution.getId(),
                execution.getStatus().name(),
                Instant.now());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public record BatchJobLaunchResponse(Long jobInstanceId, Long jobExecutionId, String status, Instant triggeredAt) {
    }
}