package com.sousa.batch;

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

    // Permite sobrescrever em testes
    protected void exit(int code) {
        System.exit(code);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 2) {
            log.error("Argumentos insuficientes.");
            log.error("Uso: java -jar app.jar <NOME_DO_JOB> <NOME_DO_ARQUIVO>");
            log.error("Exemplo: java -jar app.jar CARGA usuarios.csv");
            exit(1);
            return;
        }

        String jobName = args[0].toUpperCase();
        String fileName = null;
        for (String arg : args) {
            if (arg.startsWith("arquivo=")) {
                fileName = arg.split("=")[1];
            }
        }

        if (fileName == null) {
            log.error("Parâmetro 'arquivo=' não encontrado nos argumentos.");
            exit(1);
            return;
        }

        var jobParameters = new JobParametersBuilder()
            .addString("JobID", String.valueOf(System.currentTimeMillis()))
            .addString("arquivo", fileName)
            .toJobParameters();

        try {
            log.info("-> Iniciando execução do job: {} com o arquivo: {}", jobName, fileName);

            if ("CARGA".equals(jobName)) {
                jobLauncher.run(importUserJob, jobParameters);
            } else {
                log.error("Job '{}' não encontrado.", jobName);
                exit(1);
            }
        } catch (Exception e) {
            log.error("Erro fatal ao executar o job {}: {}", jobName, e.getMessage());
            exit(2);
        }
    }

}