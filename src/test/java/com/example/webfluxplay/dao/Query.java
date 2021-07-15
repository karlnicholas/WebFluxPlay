package com.example.webfluxplay.dao;

import com.example.webfluxplay.util.Assert;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * A wrapper for a {@link Statement} providing additional convenience APIs for running queries such as {@code SELECT}.
 */
public final class Query implements ResultBearing {

    private final Statement statement;

    Query(Statement statement) {
        this.statement = Assert.requireNonNull(statement, "statement must not be null");
    }

    /**
     * Save the current binding and create a new one.
     *
     * @return this {@link Statement}
     */
    public Query add() {
        this.statement.add();
        return this;
    }

    /**
     * Bind a value.
     *
     * @param identifier the identifier to bind to
     * @param value      the value to bind
     * @return this {@link Statement}
     * @throws IllegalArgumentException if {@code identifier} or {@code value} is {@code null}
     */
    public Query bind(String identifier, Object value) {
        Assert.requireNonNull(identifier, "identifier must not be null");
        Assert.requireNonNull(value, "value must not be null");

        this.statement.bind(identifier, value);
        return this;
    }

    /**
     * Bind a value.
     *
     * @param index the index to bind to
     * @param value the value to bind
     * @return this {@link Statement}
     * @throws IllegalArgumentException if {@code identifier} or {@code value} is {@code null}
     */
    public Query bind(int index, Object value) {
        Assert.requireNonNull(value, "value must not be null");

        this.statement.bind(index, value);
        return this;
    }

    /**
     * Bind a {@code null} value.
     *
     * @param identifier the identifier to bind to
     * @param type       the type of null value
     * @return this {@link Statement}
     * @throws IllegalArgumentException if {@code identifier} or {@code type} is {@code null}
     */
    public Query bindNull(String identifier, Class<?> type) {
        Assert.requireNonNull(identifier, "identifier must not be null");
        Assert.requireNonNull(type, "type must not be null");

        this.statement.bindNull(identifier, type);
        return this;
    }

    /**
     * Bind a {@code null} value.
     *
     * @param index the index to bind to
     * @param type  the type of null value
     * @return this {@link Statement}
     * @throws IllegalArgumentException if {@code identifier} or {@code type} is {@code null}
     */
    public Query bindNull(int index, Class<?> type) {
        Assert.requireNonNull(type, "type must not be null");

        this.statement.bindNull(index, type);
        return this;
    }

    public <T> Flux<T> mapResult(Function<Result, ? extends Publisher<? extends T>> mappingFunction) {
        Assert.requireNonNull(mappingFunction, "mappingFunction must not be null");

        return Flux
                .from(this.statement.execute())
                .flatMap(mappingFunction);
    }

    @Override
    public String toString() {
        return "Query{" +
                "statement=" + this.statement +
                '}';
    }

}