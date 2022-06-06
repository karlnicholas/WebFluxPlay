package com.example.webfluxplay.dao;

import com.example.webfluxplay.model.SomeEntity;
import io.r2dbc.spi.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

import static io.r2dbc.h2.H2ConnectionFactoryProvider.H2_DRIVER;
import static io.r2dbc.h2.H2ConnectionFactoryProvider.URL;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Service
public final class SomeEntityDao {

    private final Mono<? extends Connection> connection;

    public SomeEntityDao() {
        ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, H2_DRIVER)
                .option(PASSWORD, "")
//                .option(URL, "mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4")
                .option(URL, "mem:test;DB_CLOSE_DELAY=-1")
                .option(USER, "sa")
                .build());
        connection = Mono.from(connectionFactory.create()).cache();
    }

    public Mono<Integer> createTable() {
        return connection.flatMap(con -> Mono.from(con.createStatement("create table if not exists some_entity (id bigint not null auto_increment, value varchar(255) not null, primary key (id))")
                        .execute()))
                .flatMap(result -> Mono.from(result.getRowsUpdated()));

    }

    private final BiFunction<Row, RowMetadata, SomeEntity> mapper = (row, rowMetadata) -> {
        SomeEntity someEntity = new SomeEntity();
        someEntity.setId(row.get("id", Long.class));
        someEntity.setValue(row.get("value", String.class));
        return someEntity;
    };

    public Flux<SomeEntity> findAll() {
        return connection.flatMap(con -> Mono.from(con.createStatement("select * from some_entity")
                        .execute()))
                .flatMapMany(result -> result.map(mapper));
    }

    public Mono<SomeEntity> save(SomeEntity someEntity) {
        return connection.flatMap(con -> Mono.from(con.createStatement("insert into some_entity(value) values ($1)")
                        .bind("$1", someEntity.getValue())
                        .returnGeneratedValues()
                        .execute()))
                .flatMap(result -> Mono.from(result.map((row, rowMetadata) -> {
                    someEntity.setId(row.get("id", Long.class));
                    return someEntity;
                })));
    }

    public Mono<SomeEntity> update(SomeEntity someEntity) {
        return connection.flatMap(con -> Mono.from(con.createStatement("update some_entity set value = $1 where id = $2")
                        .bind("$1", someEntity.getValue())
                        .bind("$2", someEntity.getId())
                        .execute()))
                .flatMap(result -> Mono.from(result.map((row, rowMetadata) -> {
                    someEntity.setId(row.get("id", Long.class));
                    return someEntity;
                })));
    }

    public Mono<SomeEntity> findById(Long id) {
        return connection.flatMap(con -> Mono.from(con.createStatement("select * from some_entity where id = $1")
                        .bind("$1", id)
                        .execute()))
                .flatMap(result -> Mono.from(result.map(mapper)));
    }

    public Mono<Integer> deleteById(Long id) {
        return connection.flatMap(con -> Mono.from(con.createStatement("delete from some_entity where id = $1")
                        .bind("$1", id)
                        .execute()))
                .flatMap(res -> Mono.from(res.getRowsUpdated()));
    }

}