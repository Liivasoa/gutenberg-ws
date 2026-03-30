package mg.msys.gutenber_ws.batch.task;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeleteCatalogFileTasklet implements Tasklet {

    @Value("${batch.catalog.download-path:/tmp/abde/pg_catalog.csv}")
    private String catalogDownloadPath;

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Files.deleteIfExists(Path.of(catalogDownloadPath));
        return RepeatStatus.FINISHED;
    }

}
