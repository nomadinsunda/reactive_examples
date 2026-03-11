package chapter13;

import reactor.core.publisher.Mono;
import java.util.Base64;

/**
 * Reactor Context 활용 리팩토링 예제
 */
public class ContextTestExample {

    public static Mono<String> getSecretMessage(Mono<String> keySource) {
        return keySource
                .flatMap(key -> Mono.deferContextual(ctx -> {
                    // 1. Context에서 안전하게 값 가져오기 (Type-safe)
                    String secretKey = ctx.getOrDefault("secretKey", "");
                    String secretMessage = ctx.getOrDefault("secretMessage", "");

                    // 2. Java 표준 Base64 디코딩 사용
                    String decodedKey = new String(Base64.getDecoder().decode(secretKey));

                    // 3. 로직 처리: 키가 일치하면 메시지 반환, 아니면 빈 Mono
                    if (key.equals(decodedKey)) {
                        return Mono.just(secretMessage);
                    } else {
                        return Mono.empty();
                    }
                }));
    }
}