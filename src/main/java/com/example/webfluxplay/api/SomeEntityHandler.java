package com.example.webfluxplay.api;

import com.example.webfluxplay.dao.SomeEntityDao;
import com.example.webfluxplay.model.SomeEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
public class SomeEntityHandler {

    private final Validator validator;
    private final SomeEntityDao dao;

    public SomeEntityHandler(
            Validator validator,
            SomeEntityDao dao
    ) {
        this.validator = validator;
        this.dao = dao;
    }

    public Mono<ServerResponse> listSomeEntities(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(dao.findAll(), SomeEntity.class);
    }

    public Mono<ServerResponse> createSomeEntity(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(request.bodyToMono(SomeEntity.class)
                        .doOnNext(this::validate)
                        .flatMap(dao::save), SomeEntity.class);
    }

    public Mono<ServerResponse> getSomeEntity(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(dao.findById(Long.valueOf(request.pathVariable("id"))), SomeEntity.class);
    }

    private void validate(SomeEntity someEntity) {
        Errors errors = new BeanPropertyBindingResult(someEntity, "SomeEntity");
        validator.validate(someEntity, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}