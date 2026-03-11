package chapter6;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j; // 로그 추적을 위해 권장
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j // 실무에서는 System.out 대신 로그를 사용합니다.
public class Example6_3 {

    private static final String TIME_API_URL = "https://timeapi.io/api/Time/current/zone?timeZone=Asia/Seoul";

    public static void main(String[] args) throws InterruptedException {
        // 1. WebClient 설정 최적화 (타임아웃 추가 권장)
        WebClient webClient = WebClient.builder()
                .baseUrl(TIME_API_URL)
                .build();

        // 2. 비즈니스 로직 파이프라인
        fetchSeoulTime(webClient)
                .subscribe(
                        data -> log.info("# emitted data: {}", data),
                        error -> log.error("# Fatal Error: {}", error.getMessage()),
                        () -> log.info("# emitted onComplete signal")
                );

        // 비동기 실행을 지켜보기 위한 대기
        Thread.sleep(5000);
    }

    private static Mono<String> fetchSeoulTime(WebClient webClient) {
        return webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                // JsonPath 읽기 로직을 더 명확하게 표현
                .map(json -> JsonPath.parse(json).read("$.dateTime", String.class))

                // 네트워크 지연에 대비한 타임아웃 (서버가 응답 없을 때 3초 후 끊기)
                .timeout(Duration.ofSeconds(3))

                // 지수 백오프(Exponential Backoff) 재시도 전략 (서버 부하 방지 및 성공 확률 상향)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(signal -> log.warn("# Retry attempt: {}", signal.totalRetries() + 1)))

                // 최종 실패 시 폴백 데이터 생성
                .onErrorResume(e -> {
                    log.error("# All retry attempts failed. Cause: {}", e.getMessage());
                    return Mono.just("Fallback: " + LocalDateTime.now() + " (System Error)");
                });
    }
}