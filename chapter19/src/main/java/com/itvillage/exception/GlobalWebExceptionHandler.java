package com.itvillage.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itvillage.book.v10.ErrorResponse;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(-2)
@Component // Configuration 대신 Component 권장
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    public GlobalWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();

        // 1. 상태 코드 및 에러 응답 결정
        HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse;

        if (throwable instanceof BusinessLogicException ex) {
            status = HttpStatus.valueOf(ex.getExceptionCode().getStatus());
            errorResponse = ErrorResponse.of(ex.getExceptionCode().getStatus(), ex.getMessage());
        } else if (throwable instanceof ResponseStatusException ex) {
            status = ex.getStatusCode(); // getStatus() -> getStatusCode()
            errorResponse = ErrorResponse.of(status.value(), ex.getReason());
        } else {
            errorResponse = ErrorResponse.of(status.value(), throwable.getMessage());
        }

        // 2. 응답 설정
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 3. 직렬화 및 쓰기
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
//@Order(-2)
//@Configuration
//public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {
//    private final ObjectMapper objectMapper;
//
//    public GlobalWebExceptionHandler(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    @Override
//    public Mono<Void> handle(ServerWebExchange serverWebExchange,
//                             Throwable throwable) {
//        return handleException(serverWebExchange, throwable);
//    }
//
//    private Mono<Void> handleException(ServerWebExchange serverWebExchange,
//                                       Throwable throwable) {
//        ErrorResponse errorResponse = null;
//        DataBuffer dataBuffer = null;
//
//        DataBufferFactory bufferFactory =
//                                serverWebExchange.getResponse().bufferFactory();
//        serverWebExchange.getResponse().getHeaders()
//                                        .setContentType(MediaType.APPLICATION_JSON);
//
//        if (throwable instanceof BusinessLogicException) {
//            BusinessLogicException ex = (BusinessLogicException) throwable;
//            ExceptionCode exceptionCode = ex.getExceptionCode();
//            errorResponse = ErrorResponse.of(exceptionCode.getStatus(),
//                                                exceptionCode.getMessage());
//            serverWebExchange.getResponse()
//                        .setStatusCode(HttpStatus.valueOf(exceptionCode.getStatus()));
//        } else if (throwable instanceof ResponseStatusException) {
//            ResponseStatusException ex = (ResponseStatusException) throwable;
//            errorResponse = ErrorResponse.of(ex.getStatusCode().value(), ex.getMessage());
//            serverWebExchange.getResponse().setStatusCode(ex.getStatusCode());
//        } else {
//            errorResponse = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                                                            throwable.getMessage());
//            serverWebExchange.getResponse()
//                                    .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//        try {
//            dataBuffer =
//                    bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
//        } catch (JsonProcessingException e) {
//            bufferFactory.wrap("".getBytes());
//        }
//
//        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
//    }
//}
