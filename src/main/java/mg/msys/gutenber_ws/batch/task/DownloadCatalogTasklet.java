package mg.msys.gutenber_ws.batch.task;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class DownloadCatalogTasklet implements Tasklet {

    @Value("${batch.catalog.url}")
    private String catalogUrl;

    @Value("${batch.catalog.download-path:/tmp/abde/pg_catalog.csv}")
    private String catalogDownloadPath;

    @Override
    @Retryable(maxAttemptsExpression = "${batch.catalog.download.max-attempts:3}", backoff = @Backoff(delayExpression = "${batch.catalog.download.retry-delay-ms:5000}"))
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Path outputPath = Path.of(catalogDownloadPath);
        Files.createDirectories(outputPath.getParent());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(catalogUrl))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to download catalog CSV. HTTP status: " + response.statusCode());
        }

        try (InputStream body = response.body()) {
            Files.copy(body, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return RepeatStatus.FINISHED;
    }

}
