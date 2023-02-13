package com.example.webfluxplay.api;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@EnableWebFlux
public class RoutingConfig implements WebFluxConfigurer {
    @Bean
    public RouterFunction<ServerResponse> routerFunctions(SomeEntityHandler handler) {
        return RouterFunctions.nest(path("/api/someentity"),
                RouterFunctions.nest(accept(APPLICATION_JSON),
                                RouterFunctions.route(GET("/{id}"), handler::getSomeEntity)
                                        .andRoute(GET(""), r -> handler.listSomeEntities())
                                        .andNest(contentType(APPLICATION_JSON),
                                                RouterFunctions.route(POST(""), handler::createSomeEntity)
                                                        .andRoute(PATCH(""), handler::updateSomeEntity)
                                        )
                        )
                        .andRoute(DELETE("/{id}"), handler::deleteSomeEntity)
        );
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
