package com.example.webfluxplay.dao;

import com.example.webfluxplay.util.Assert;
import io.r2dbc.spi.Result;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * A wrapper for a {@link io.r2dbc.spi.Batch} providing additional convenience APIs
 */
public final class Batch implements ResultBearing {

    private final io.r2dbc.spi.Batch batch;

    Batch(io.r2dbc.spi.Batch batch) {
        this.batch = Assert.requireNonNull(batch, "batch must not be null");
    }

    /**
     * Add a statement to this batch.
     *
     * @param sql the statement to add
     * @return this {@link Batch}
     * @throws IllegalArgumentException if {@code sql} is {@code null}
     */
    public Batch add(String sql) {
        Assert.requireNonNull(sql, "sql must not be null");

        this.batch.add(sql);
        return this;
    }

    public <T> Flux<T> mapResult(Function<Result, ? extends Publisher<? extends T>> mappingFunction) {
        Assert.requireNonNull(mappingFunction, "mappingFunction must not be null");

        return Flux.from(this.batch.execute())
                .flatMap(mappingFunction);
    }

    @Override
    public String toString() {
        return "Batch{" +
                "batch=" + this.batch +
                '}';
    }

}