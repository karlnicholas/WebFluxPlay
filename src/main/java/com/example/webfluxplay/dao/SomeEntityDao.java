package com.example.webfluxplay.dao;

import com.example.webfluxplay.model.SomeEntity;
import io.r2dbc.spi.*;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.r2dbc.h2.H2ConnectionFactoryProvider.H2_DRIVER;
import static io.r2dbc.h2.H2ConnectionFactoryProvider.URL;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

import java.util.function.BiFunction;

@Service
public final class SomeEntityDao {
    private final Mono<? extends Connection> connection;

    public SomeEntityDao() {
        ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, H2_DRIVER)
                .option(PASSWORD, "")
//                .option(URL, "mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4")
                .option(URL, "mem:test;DB_CLOSE_DELAY=-1")
//                .option(URL, "tcp://localhost/~/test")
                .option(USER, "sa")
                .build());
        connection = Mono.from(connectionFactory.create()).cache();
    }

    public Mono<Long> createTable() {
        return connection.flatMap(con -> Mono.from(con.createStatement("create table if not exists some_entity (id bigint not null auto_increment, name varchar(255) not null, primary key (id))")
                        .execute()))
                .flatMap(result -> Mono.from(result.getRowsUpdated()));
    }

    private final BiFunction<Row, RowMetadata, SomeEntity> mapper = (row, rowMetadata) -> {
        SomeEntity someEntity = new SomeEntity();
        someEntity.setId(row.get("id", Long.class));
        someEntity.setName(row.get("name", String.class));
        return someEntity;
    };

    public Flux<SomeEntity> findAll() {
        return connection.flatMap(con -> Mono.from(con.createStatement("select * from some_entity")
                        .execute()))
                .flatMapMany(result -> result.map(mapper));
    }

    public Mono<SomeEntity> save(SomeEntity someEntity) {
        return connection.flatMap(con -> Mono.from(con.createStatement("insert into some_entity(name) values ($1)")
                        .bind("$1", someEntity.getName())
                        .returnGeneratedValues()
                        .execute()))
                .flatMap(result -> Mono.from(result.map((row, rowMetadata) -> {
                    someEntity.setId(row.get("id", Long.class));
                    return someEntity;
                })));
    }

    public Mono<Long> update(SomeEntity someEntity) {
        return connection.flatMap(con -> Mono.from(con.createStatement("update some_entity set name=$2 where id = $1")
                        .bind("$1", someEntity.getId())
                        .bind("$2", someEntity.getName())
                .execute()))
                .flatMap(result -> Mono.from(result.getRowsUpdated()));
    }

    public Mono<SomeEntity> findById(Long id) {
        return connection.flatMap(con -> Mono.from(con.createStatement("select * from some_entity where id = $1")
                        .bind("$1", id)
                        .execute()))
                .flatMap(result -> Mono.from(result.map(mapper)));
    }

    public Mono<Long> deleteById(Long id) {
        return connection.flatMap(con -> Mono.from(con.createStatement("delete from some_entity where id = $1")
                        .bind("$1", id)
                        .execute()))
                .flatMap(res -> Mono.from(res.getRowsUpdated()));
    }
}