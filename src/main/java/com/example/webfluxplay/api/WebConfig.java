package com.example.webfluxplay.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {
    @Bean
    public RouterFunction<?> routerFunctions(SomeEntityHandler handler) {
        return route()
                .path("/api/someentity", b1 -> b1
                        .nest(accept(APPLICATION_JSON), b2 -> b2
                                .GET("/{id}", handler::getSomeEntity)
                                .GET(handler::listSomeEntities))
                        .POST(handler::createSomeEntity))
                .build();
    }
}
