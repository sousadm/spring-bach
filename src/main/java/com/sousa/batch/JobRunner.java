package com.sousa.batch;

import java.util.Objects;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class JobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job importUserJob;

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            log.error("Argumento do job não fornecido. Uso: java -jar seu-app.jar <NOME_DO_JOB>");
            log.error("Jobs disponíveis: CARGA");
            System.exit(1); // Encerra com erro de argumento
            return;
        }

        String jobName = args[0].toUpperCase();
        log.info("-> Iniciando execução do job: " + jobName);

        var jobParameters = new JobParametersBuilder()
                .addString("JobID", Objects.requireNonNull(String.valueOf(System.currentTimeMillis())))
                .toJobParameters();

        try {
            if ("CARGA".equals(jobName)) {
                Job job = Objects.requireNonNull(importUserJob);
                jobLauncher.run(job, jobParameters);
            } else {
                log.error("Job '" + jobName + "' não encontrado.");
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("Ocorreu um erro fatal ao executar o job " + jobName + ": " + e.getMessage());
            System.exit(2);
        }

    }

}