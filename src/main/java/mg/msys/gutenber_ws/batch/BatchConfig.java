package mg.msys.gutenber_ws.batch;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;

import mg.msys.gutenber_ws.batch.entity.GutenbergBook;
import mg.msys.gutenber_ws.batch.task.CatalogBookWriter;
import mg.msys.gutenber_ws.batch.task.DeleteCatalogFileTasklet;
import mg.msys.gutenber_ws.batch.task.DownloadCatalogTasklet;

@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository(tablePrefix = "syst.BATCH_")
@EnableRetry
public class BatchConfig {

    @Value("${batch.catalog.download-path:/tmp/gutenberg-ws/pg_catalog.csv}")
    private String catalogDownloadPath;

    @Bean
    public Job importBookJob(JobRepository jobRepository,
            Step downloadCatalogStep,
            Step ingestionCatalogStep,
            Step cleanupCatalogFileStep) {
        return new JobBuilder("importBookJob", jobRepository)
                .start(downloadCatalogStep)
                .next(ingestionCatalogStep)
                .next(cleanupCatalogFileStep)
                .build();
    }

    @Bean
    public Step downloadCatalogStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DownloadCatalogTasklet downloadCatalogTasklet) {
        return new StepBuilder("downloadCatalogStep", jobRepository)
                .tasklet(downloadCatalogTasklet)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Step ingestionCatalogStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<GutenbergBook> reader,
            ItemWriter<GutenbergBook> writer) {

        return new StepBuilder("ingestionCatalogStep", jobRepository)
                .<GutenbergBook, GutenbergBook>chunk(100).transactionManager(transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public Step cleanupCatalogFileStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DeleteCatalogFileTasklet deleteCatalogFileTasklet) {
        return new StepBuilder("cleanupCatalogFileStep", jobRepository)
                .tasklet(deleteCatalogFileTasklet)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public FlatFileItemReader<GutenbergBook> reader() {
        return new FlatFileItemReaderBuilder<GutenbergBook>()
                .name("bookReader")
                // .resource(new ClassPathResource("pg_catalog.csv"))
                .resource(new FileSystemResource(catalogDownloadPath))
                .linesToSkip(1)
                .recordSeparatorPolicy(new DefaultRecordSeparatorPolicy())
                .delimited()
                .includedFields(0, 2, 3, 4, 5, 6, 8)
                .names("id", "issued", "title", "languages", "authors", "subjects", "bookshelves")
                .fieldSetMapper(fieldSet -> {
                    GutenbergBook book = new GutenbergBook();
                    book.setId(fieldSet.readLong("id"));
                    book.setIssued(parseIssued(fieldSet.readString("issued")));
                    book.setTitle(fieldSet.readString("title"));
                    book.setLanguages(fieldSet.readString("languages"));
                    book.setAuthors(fieldSet.readString("authors"));
                    book.setSubjects(fieldSet.readString("subjects"));
                    book.setBookshelves(fieldSet.readString("bookshelves"));
                    return book;
                })
                .build();
    }

    private static LocalDate parseIssued(String issuedValue) {
        if (issuedValue == null || issuedValue.isBlank()) {
            return null;
        }

        String normalizedValue = issuedValue.trim();
        if (normalizedValue.matches("\\d{4}")) {
            return LocalDate.of(Integer.parseInt(normalizedValue), 1, 1);
        }

        try {
            return LocalDate.parse(normalizedValue);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    @Bean
    public ItemWriter<GutenbergBook> writer(DataSource dataSource) {
        return CatalogBookWriter.create(dataSource);
    }

}
