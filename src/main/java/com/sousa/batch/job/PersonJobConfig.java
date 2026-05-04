package com.sousa.batch.job;

import jakarta.persistence.EntityManagerFactory; // Importação correta para Spring Boot 3+
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;

import com.sousa.batch.model.Person;
import com.sousa.batch.processor.PersonItemProcessor;

@Configuration
public class PersonJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<Person> reader(@Value("#{jobParameters['arquivo']}") @NonNull String nomeArquivo) {

        ClassPathResource resource = new ClassPathResource(nomeArquivo);

        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(resource) 
                .linesToSkip(1)
                .delimited()
                .names("nome", "email", "idade")
                .targetType(Person.class)
                .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JpaItemWriter<Person> writer(@NonNull EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Person>()
            .entityManagerFactory(entityManagerFactory)
            .build();
    }

    @Bean
    public Step step(@NonNull JobRepository jobRepository,
                     @NonNull  PlatformTransactionManager transactionManager,
                     @NonNull  FlatFileItemReader<Person> reader,
                     @NonNull  PersonItemProcessor processor,
                     @NonNull  JpaItemWriter<Person> writer) { // Parâmetro atualizado
        return new StepBuilder("step", jobRepository)
            .<Person, Person>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    @Bean
    public Job importUserJob(@NonNull JobRepository jobRepository, @NonNull Step step) {
        return new JobBuilder("importUserJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(step)
            .build();
    }

}