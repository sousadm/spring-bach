package com.sousa.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JobRunner Tests")
class JobRunnerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job importUserJob;

    @Mock
    private JobExecution jobExecution;

    private static class TestableJobRunner extends JobRunner {

        private Integer exitCode = null;

        TestableJobRunner(JobLauncher jobLauncher, Job importUserJob) {
            super(jobLauncher, importUserJob);
        }

        @Override
        protected void exit(int code) {
            this.exitCode = code;
        }

        Integer getExitCode() {
            return exitCode;
        }
    }

    private TestableJobRunner jobRunner;

    @BeforeEach
    void setUp() throws Exception {
        jobRunner = new TestableJobRunner(jobLauncher, importUserJob);
        lenient().when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenReturn(jobExecution);
    }

    // -------------------------------------------------------------------------
    // Testes de argumentos insuficientes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve chamar exit(1) quando nenhum argumento for fornecido")
    void deveEncerrarComExit1QuandoNenhumArgumentoForFornecido() throws Exception {
        jobRunner.run();

        assertThat(jobRunner.getExitCode()).isEqualTo(1);
        verifyNoInteractions(jobLauncher);
    }

    @Test
    @DisplayName("Deve chamar exit(1) quando apenas um argumento for fornecido")
    void deveEncerrarComExit1QuandoApenasUmArgumentoForFornecido() throws Exception {
        jobRunner.run("CARGA");

        assertThat(jobRunner.getExitCode()).isEqualTo(1);
        verifyNoInteractions(jobLauncher);
    }

    @Test
    @DisplayName("Deve chamar exit(1) quando prefixo 'arquivo=' não for encontrado nos argumentos")
    void deveEncerrarComExit1QuandoPrefixoArquivoNaoForFornecido() throws Exception {
        // Requer que o JobRunner valide fileName == null antes de construir JobParameters:
        //   if (fileName == null) { exit(1); return; }
        jobRunner.run("CARGA", "outroArgumento");

        assertThat(jobRunner.getExitCode()).isEqualTo(1);
        verifyNoInteractions(jobLauncher);
    }

    // -------------------------------------------------------------------------
    // Testes de execução do job CARGA
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve executar importUserJob quando jobName for CARGA")
    void deveExecutarImportUserJobQuandoJobNameForCarga() throws Exception {
        jobRunner.run("CARGA", "arquivo=usuarios.csv");

        verify(jobLauncher, times(1)).run(eq(importUserJob), any(JobParameters.class));
        assertThat(jobRunner.getExitCode()).isNull();
    }

    @Test
    @DisplayName("Deve executar importUserJob quando jobName for carga (minúsculo)")
    void deveExecutarImportUserJobQuandoJobNameForCargaMinusculo() throws Exception {
        jobRunner.run("carga", "arquivo=usuarios.csv");

        verify(jobLauncher, times(1)).run(eq(importUserJob), any(JobParameters.class));
        assertThat(jobRunner.getExitCode()).isNull();
    }

    @Test
    @DisplayName("Deve passar o nome do arquivo corretamente nos JobParameters")
    void devePassarNomeDoArquivoNosJobParameters() throws Exception {
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);

        jobRunner.run("CARGA", "arquivo=usuarios.csv");

        verify(jobLauncher).run(eq(importUserJob), captor.capture());
        assertThat(captor.getValue().getString("arquivo")).isEqualTo("usuarios.csv");
    }

    @Test
    @DisplayName("Deve incluir JobID nos JobParameters")
    void deveIncluirJobIdNosJobParameters() throws Exception {
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);

        jobRunner.run("CARGA", "arquivo=usuarios.csv");

        verify(jobLauncher).run(eq(importUserJob), captor.capture());
        assertThat(captor.getValue().getString("JobID")).isNotBlank();
    }

    // -------------------------------------------------------------------------
    // Testes de job não encontrado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve chamar exit(1) quando jobName for desconhecido")
    void deveEncerrarComExit1QuandoJobNameForDesconhecido() throws Exception {
        jobRunner.run("JOB_INEXISTENTE", "arquivo=test.csv");

        assertThat(jobRunner.getExitCode()).isEqualTo(1);
        verifyNoInteractions(jobLauncher);
    }

    // -------------------------------------------------------------------------
    // Testes de tratamento de exceção
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve chamar exit(2) quando JobLauncher lançar exceção")
    void deveEncerrarComExit2QuandoJobLauncherLancarExcecao() throws Exception {
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Erro simulado no launcher"));

        jobRunner.run("CARGA", "arquivo=usuarios.csv");

        assertThat(jobRunner.getExitCode()).isEqualTo(2);
        verify(jobLauncher, times(1)).run(eq(importUserJob), any(JobParameters.class));
    }
}