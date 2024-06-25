package com.example.webfluxplay.api;

import com.example.webfluxplay.dao.SomeEntityDao;
import com.example.webfluxplay.model.SomeEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.List;

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

    public Mono<ServerResponse> listSomeEntities() {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(dao.findAll(), SomeEntity.class);
    }

    public Mono<ServerResponse> createSomeEntity(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(request.bodyToMono(SomeEntity.class)
                        .doOnNext(this::validate)
                        .flatMap(dao::save), SomeEntity.class);
    }

    public Mono<ServerResponse> createSomeEntities(ServerRequest request) {
        ParameterizedTypeReference<List<SomeEntity>> ptr = new ParameterizedTypeReference<>() {};
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(request.bodyToMono(ptr)
                        .doOnNext(this::validateAll)
                        .flatMapMany(dao::saveAll), SomeEntity.class);
    }

    public Mono<ServerResponse> getSomeEntity(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(dao.findById(Long.valueOf(request.pathVariable("id"))), SomeEntity.class);
    }

    public Mono<ServerResponse> deleteSomeEntity(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(dao.deleteById(Long.valueOf(request.pathVariable("id"))), Long.class);
    }

    private void validate(SomeEntity someEntity) {
        Errors errors = new BeanPropertyBindingResult(someEntity, "SomeEntity");
        validator.validate(someEntity, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

    private void validateAll(List<SomeEntity> someEntities) {
        Errors errors = new BeanPropertyBindingResult(someEntities, "SomeEntity");
        someEntities.forEach(someEntity -> validator.validate(someEntity, errors));
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

    public Mono<ServerResponse> updateSomeEntity(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(request.bodyToMono(SomeEntity.class)
                        .doOnNext(this::validate)
                        .flatMap(updateEntity-> dao.findById(updateEntity.getId())
                                .switchIfEmpty(Mono.error(new Exception("Entity not found")))
                                .map(updateEntity::merge))
                        .flatMap(dao::update), Long.class);
    }
}