package com.example.webfluxplay.api;

import com.example.webfluxplay.model.SomeEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class SomeEntityHandler {

    private final Validator validator;

    public SomeEntityHandler(
            Validator validator
    ) {
        this.validator = validator;
    }

    public Mono<ServerResponse> listSomeEntities(ServerRequest request) {
        return Mono.fromSupplier(() -> null /*dao.findAll()*/)
                .flatMap(someEntities -> ok().bodyValue(someEntities))
                .onErrorResume(throwable -> badRequest().bodyValue(throwable.getMessage()))
                .switchIfEmpty(badRequest().bodyValue("No entities found"));
    }

    public Mono<ServerResponse> createSomeEntity(ServerRequest request) {
        return request.bodyToMono(SomeEntity.class)
                .doOnNext(this::validate)
                .map(null /*dao.findAll()*/)
                .flatMap(someEntity -> ok().bodyValue(someEntity))
                .onErrorResume(throwable -> badRequest().bodyValue(throwable.getMessage()));
    }

    public Mono<ServerResponse> getSomeEntity(ServerRequest request) {
        return Mono.just(request.pathVariable("id"))
                .map(Long::valueOf)
                .map(t->Optional.ofNullable(null) /*dao.findAll()*/)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(someEntity -> ok().bodyValue(someEntity))
                .onErrorResume(throwable -> badRequest().bodyValue(throwable.getMessage()))
                .switchIfEmpty(badRequest().bodyValue("Some entity not found"));
    }

    private void validate(SomeEntity someEntity) {
        Errors errors = new BeanPropertyBindingResult(someEntity, "SomeEntity");
        validator.validate(someEntity, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}