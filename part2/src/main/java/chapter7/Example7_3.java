package chapter7;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class Example7_3 {
    public static void main(String[] args) throws InterruptedException {
        // 1. WebClient 인스턴스 설정 (https 지원 및 기본 설정)
        WebClient webClient = WebClient.builder()
                .baseUrl("https://timeapi.io/api/Time/current/zone")
                .build();

        // 2. Cold Publisher 정의 (선언만 함, 실행 안 됨)
        Mono<String> mono = getCurrentTime(webClient);

        log.info("# --- 첫 번째 구독 시작 ---");
        mono.subscribe(dateTime -> log.info("# dateTime 1: {}", dateTime));

        // 두 구독 사이의 시간 차이를 벌려 Cold의 특징을 확인합니다.
        Thread.sleep(3000);

        log.info("# --- 두 번째 구독 시작 ---");
        mono.subscribe(dateTime -> log.info("# dateTime 2: {}", dateTime));

        // 비동기 로그 출력을 확인하기 위해 대기
        Thread.sleep(2000);
    }

    private static Mono<String> getCurrentTime(WebClient webClient) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("timeZone", "Asia/Seoul")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    // timeapi.io는 결과 필드명이 'dateTime'입니다.
                    return JsonPath.parse(json).read("$.dateTime", String.class);
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("# API 에러 발생: {}", e.getMessage());
                    return Mono.just("0000-00-00T00:00:00 (Error)");
                });
    }
}