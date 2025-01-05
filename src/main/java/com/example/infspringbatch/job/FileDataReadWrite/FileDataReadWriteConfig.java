package com.example.infspringbatch.job.FileDataReadWrite;

import com.example.infspringbatch.job.DbDataReadWrite.dto.Player;
import com.example.infspringbatch.job.DbDataReadWrite.dto.PlayerYears;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

/**
 * desc: 파일 데이터 읽기/쓰기
 * run: --job.names=fileReadWriteJob
 */
@Configuration
@RequiredArgsConstructor
public class FileDataReadWriteConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job fileReadWriteJob(Step fileReadWriteStep) {
        return jobBuilderFactory.get("fileReadWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(fileReadWriteStep)
                .build();
    }

    @Bean
    @JobScope
    public Step fileReadWriteStep(ItemReader playerItemReader, ItemProcessor playerItemProcessor, ItemWriter playerItemWriter) {
        return stepBuilderFactory.get("fileReadWriteStep")
                .<Player, PlayerYears>chunk(5)
                .reader(playerItemReader) // 읽고
//                .writer(new ItemWriter() {
//                    @Override
//                    public void write(List items) throws Exception {
//                        items.forEach(System.out::println);
//                    }
//                })
                .processor(playerItemProcessor) // 처리하고
                .writer(playerItemWriter) // 쓰기
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Player, PlayerYears> playerItemProcessor() {
        return new ItemProcessor<Player, PlayerYears>() {
            @Override
            public PlayerYears process(Player item) throws Exception {
                return new PlayerYears(item);
            }
        };
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Player> playerItemReader() {
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerItemReader") // reader 이름
                .resource(new FileSystemResource("Players.csv")) // 읽어올 파일 위치
                .lineTokenizer(new DelimitedLineTokenizer()) // 구분자 설정 (기본값: 콤마)
                .fieldSetMapper(new PlayerFieldSetMapper()) // 읽어온 데이터를 객체로 매핑
                .linesToSkip(1) // 첫번째 줄은 건너뜀
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<PlayerYears> playerItemWriter() {
        BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>(); // 객체를 필드로 추출
        fieldExtractor.setNames(new String[]{"ID", "lastName", "position", "yearsExperience"}); // 필드명 설정
        fieldExtractor.afterPropertiesSet(); // 필드 추출 설정

        DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>(); // 필드를 구분자로 묶음
        lineAggregator.setDelimiter(","); // 구분자 설정
        lineAggregator.setFieldExtractor(fieldExtractor); // 필드 추출 설정

        FileSystemResource outputResource = new FileSystemResource("players_output.txt"); // 쓰기 파일 위치

        return new FlatFileItemWriterBuilder<PlayerYears>()
                .name("playerItemWriter") // writer 이름
                .resource(outputResource) // 쓰기 파일 위치
                .lineAggregator(lineAggregator) // 필드를 구분자로 묶음
                .build();
    }

}
