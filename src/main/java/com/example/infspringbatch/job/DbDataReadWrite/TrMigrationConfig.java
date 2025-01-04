package com.example.infspringbatch.job.DbDataReadWrite;

import com.example.infspringbatch.core.domain.accounts.Accounts;
import com.example.infspringbatch.core.domain.accounts.AccountsRepository;
import com.example.infspringbatch.core.domain.orders.Orders;
import com.example.infspringbatch.core.domain.orders.OrdersRepository;
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
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;

/**
 * desc: 주문 테이블 -> 정산 테이블 데이터 이관
 * run: --job.name=trMigrationJob
 *
 * 학습목표: DB에서 데이터를 읽고 쓰는 작업 진행
 * 주문테이블에서 정산테이블로 데이터를 이관하는 작업을 한다.
 *
 * 데이터를 읽어올때 DB로부터 데이터를 읽어오고
 * DB로 쓸때는 객체에 담아서 읽고 씀
 */
@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {

    private final AccountsRepository accountsRepository;
    private final OrdersRepository ordersRepository;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job trMigrationJob(Step trMigrationStep) {
        return jobBuilderFactory.get("trMigrationJob")
                .incrementer(new RunIdIncrementer())
                .start(trMigrationStep)
                .build();
    }

    @Bean
    @JobScope
    public Step trMigrationStep(ItemReader trOrdersReader, ItemProcessor trOrderProcessor, ItemWriter trOrdersWriter) {
        return stepBuilderFactory.get("trMigrationStep")
                .<Orders, Accounts>chunk(5) // 5개씩 묶어서 데이터 처리후 데이터 커밋, 읽어온 데이터: oreder, 쓰는 데이터: account
                .reader(trOrdersReader) // 데이터 읽기
//                .writer(new ItemWriter() {
//                    @Override
//                    public void write(List items) throws Exception {
//                        items.forEach(System.out::println);
//                    }
//                })
                .processor(trOrderProcessor) // 데이터 가공
                .writer(trOrdersWriter) // 데이터 저장
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemWriter<Accounts> trOrdersWriter() {
        return new RepositoryItemWriterBuilder<Accounts>()
                .repository(accountsRepository) // 저장할 repository
                .methodName("save") // 저장할 메소드
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Orders, Accounts> trOrderProcessor() {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                return new Accounts(item);
            }
        };
    }

    @Bean
    @StepScope
    // 주문 테이블 데이터 읽기, 즉 Order 객체에서 5개의 단위로 데이터를 추출 후
    // 이렇게 만들어진 Reader를 Step에 주입
    public RepositoryItemReader<Orders> trOrdersReader() {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader") // reader 이름
                .repository(ordersRepository) // 읽어올 데이터가 있는 repository
                .methodName("findAll") // repository에서 데이터를 읽어올 메소드
                .pageSize(5) // 한번에 읽어올 데이터 양
                .arguments(Arrays.asList())
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC)) // 데이터 정렬
                .build();
    }

}