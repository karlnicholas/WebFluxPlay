package com.example.webfluxplay.dao;

import com.example.webfluxplay.model.SomeEntity;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.r2dbc.h2.H2ConnectionFactoryProvider.H2_DRIVER;
import static io.r2dbc.h2.H2ConnectionFactoryProvider.URL;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Service
public final class SomeEntityDao {

    private final ConnectionFactory connectionFactory;
    private final Publisher<? extends Connection> connection;

    public SomeEntityDao() {
        connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, H2_DRIVER)
                .option(PASSWORD, "sa")
//                .option(URL, "mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4")
                .option(URL, "mem:test;DB_CLOSE_DELAY=-1")
                .option(USER, "sa")
                .build());
        connection = connectionFactory.create();
    }

    public Mono<Integer> createTable() {
        return Mono.from(connection)
                .map(con -> con.createStatement("create table some_entity (id bigint not null auto_increment, value varchar(255) not null, primary key (id))").execute())
                .flatMap(result -> Mono.from(result).flatMap(res -> Mono.from(res.getRowsUpdated())));
    }

    public Flux<SomeEntity> findAll() {
        return Mono.from(connection).map(con -> con.createStatement("select * from some_entity").execute())
                .flatMapMany(resultPublisher -> Flux.from(resultPublisher).flatMap(result -> result.map((row, rowMetadata) -> {
                    SomeEntity someEntity = new SomeEntity();
                    someEntity.setId(row.get(0, Long.class));
                    someEntity.setValue(row.get(1, String.class));
                    return someEntity;
                })));
    }

    public Mono<SomeEntity> save(SomeEntity someEntity) {
        return Mono.from(connection).map(con -> con.createStatement("insert into some_entity(value) values (?)")
                .bind(0, someEntity.getValue())
                .returnGeneratedValues()
                .execute())
                .flatMap(resultPublisher -> Mono.from(resultPublisher).flatMap(result -> Mono.from(result.map((row, rowMetadata) -> {
                    someEntity.setId(row.get(0, Long.class));
                    return someEntity;
                }))));
    }

    public Mono<SomeEntity> findById(Long id) {
        return Mono.from(connection).map(con -> con.createStatement("select * from some_entity where id = ?")
                .bind(0, id)
                .execute())
                .flatMap(resultPublisher -> Mono.from(resultPublisher).flatMap(result -> Mono.from(result.map((row, rowMetadata) -> {
                    SomeEntity someEntity = new SomeEntity();
                    someEntity.setId(row.get(0, Long.class));
                    someEntity.setValue(row.get(1, String.class));
                    return someEntity;
                }))));
    }

}