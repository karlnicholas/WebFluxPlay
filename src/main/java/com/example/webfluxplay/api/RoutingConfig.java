package com.example.webfluxplay.api;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class RoutingConfig implements WebFluxConfigurer {
    @Bean
    public RouterFunction<?> routerFunctions(SomeEntityHandler handler) {
        return route()
                .path("/api/someentity", b1 -> b1
                        .nest(accept(APPLICATION_JSON), b2 -> b2
                                .GET("/{id}", handler::getSomeEntity)
                                .GET(handler::listSomeEntities))
                        .POST(contentType(APPLICATION_JSON), handler::createSomeEntity))
                .build();
    }
    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new MessageErrorAttributes();
    }

    class MessageErrorAttributes extends DefaultErrorAttributes {
        @Override
        public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
            return super.getErrorAttributes(request, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));
        }
    }

}
