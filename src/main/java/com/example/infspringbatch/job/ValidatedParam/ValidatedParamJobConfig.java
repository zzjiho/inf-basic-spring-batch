package com.example.infspringbatch.job.ValidatedParam;

import com.example.infspringbatch.job.ValidatedParam.Validator.FileParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * run: --spring.batch.job.names=validatedParamJob -fileName=test.csv
 *
 * 학습목표:
 * 이 job을 호출시 특정 파일이나 승인 날짜 같은 값을 파라미터로 넘겨준다고 하면
 * 그 파라미터를 어떻게 받을 수 있는지 그리고 검증을 어떻게 할 것인지
 */
@Configuration
@RequiredArgsConstructor
public class ValidatedParamJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job ValidatedParamJob(Step validatedParamStep) {
        return jobBuilderFactory.get("validatedParamJob")
                .incrementer(new RunIdIncrementer())
//                .validator(new FileParamValidator()) // job 실행전 파라미터 검증 , 단수
                .validator(multipleValidator()) // 복수
                .start(validatedParamStep)
                .build();
    }

    private CompositeJobParametersValidator multipleValidator() { // 다수 validator 등록해서 검증
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(new FileParamValidator()));
        return validator;
    }

    @Bean
    @JobScope // job 실행시에만 해당 빈을 생성
    public Step validatedParamStep(Tasklet validatedParamJobTasklet) {
        return stepBuilderFactory.get("validatedParamStep")
                .tasklet(validatedParamJobTasklet)
                .build();
    }

    @Bean
    @StepScope // step 실행시에만 해당 빈을 생성
    public Tasklet validatedParamJobTasklet(@Value("#{jobParameters['fileName']}") String fileName) {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println(fileName);
                System.out.println("validated Param Tasklet");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
