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

import java.util.Optional;

import static io.r2dbc.h2.H2ConnectionFactoryProvider.H2_DRIVER;
import static io.r2dbc.h2.H2ConnectionFactoryProvider.URL;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Service
public final class SomeEntityDao {

    private final ConnectionFactory connectionFactory;
    private final Publisher<? extends Connection> connection;

    public SomeEntityDao() {
        connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, H2_DRIVER)
                .option(PASSWORD, "sa")
                .option(URL, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4")
                .option(USER, "sa")
                .build());
        connection = connectionFactory.create();
    }

    public Mono<Integer> createTable() {
        return Mono.from(connection)
                .map(con -> con.createStatement("create table some_entity (id bigint not null auto_increment, value varchar(255) not null, primary key (id)) engine=InnoDB;").execute())
                .flatMap(result->Mono.from(result).flatMap(res-> Mono.from(res.getRowsUpdated())));
    }
    public Flux<SomeEntity> findAll() {
        return Mono.from(connection).map(con -> con.createStatement("select * from some_entity").execute())
                .flatMapMany(resultPublisher -> Flux.from(resultPublisher).flatMap(result -> result.map((row, rowMetadata) -> {
                    SomeEntity someEntity = new SomeEntity();
                    someEntity.setId(row.get("id", Long.class));
                    someEntity.setValue(row.get("value", String.class));
                    return someEntity;
                })));
    }

    public Mono<SomeEntity> save(SomeEntity someEntity) {
        return Mono.from(connection).map(con -> con.createStatement("insert into some_entity('id', 'value') values(':id', ':value')")
                .bind("id", someEntity.getId())
                .bind("value", someEntity.getValue())
                .returnGeneratedValues()
                .execute())
                .flatMap(resultPublisher -> Mono.from(resultPublisher).flatMap(result -> Mono.from(result.map((row, rowMetadata) -> {
                    someEntity.setId(row.get("id", Long.class));
                    return someEntity;
                }))));
    }

    public Mono<Optional<SomeEntity>> findById(Long id) {
        return Mono.from(connection).map(con -> con.createStatement("select * some_entity where id = ':id'")
                .bind("id", id)
                .execute())
                .flatMap(resultPublisher -> Mono.from(resultPublisher).flatMap(possibleResult -> {
                    return Mono.from(possibleResult.getRowsUpdated()).map(i -> Optional.ofNullable(i > 0 ? possibleResult : null))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .flatMap(oResultFound -> Mono.from(oResultFound.map((row, rowMetadata) -> {
                                SomeEntity someEntity = new SomeEntity();
                                someEntity.setId(row.get("id", Long.class));
                                someEntity.setValue(row.get("value", String.class));
                                return someEntity;
                            })))
                            .map(Optional::of);
                }));
    }

}