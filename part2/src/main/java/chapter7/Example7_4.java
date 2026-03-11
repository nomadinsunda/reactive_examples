package chapter7;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class Example7_4 {
    public static void main(String[] args) throws InterruptedException {
        // 1. WebClient 빌드
        WebClient webClient = WebClient.builder()
                .baseUrl("https://timeapi.io/api/Time/current/zone")
                .build();

        // 2. .cache()를 추가하여 Hot Sequence로 변환
        // 이제 이 Mono는 첫 번째 구독 시 데이터를 가져오고, 이후 구독자에게는 캐시된 데이터를 공유합니다.
        Mono<String> mono = getCurrentTime(webClient).cache();

        log.info("# --- 첫 번째 구독 시작 (네트워크 요청 발생) ---");
        mono.subscribe(dateTime -> log.info("# dateTime 1: {}", dateTime));

        // 2초 대기 후 다시 구독
        Thread.sleep(2000);

        log.info("# --- 두 번째 구독 시작 (캐시된 데이터 사용) ---");
        mono.subscribe(dateTime -> log.info("# dateTime 2: {}", dateTime));

        // 모든 로그 출력을 확인하기 위해 대기
        Thread.sleep(1000);
    }

    private static Mono<String> getCurrentTime(WebClient webClient) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("timeZone", "Asia/Seoul")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> JsonPath.parse(json).read("$.dateTime", String.class))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("# API 에러 발생: {}", e.getMessage());
                    return Mono.just("Fallback-Time-Value");
                });
    }
}