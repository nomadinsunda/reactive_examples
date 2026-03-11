package chapter14.operator_6_time;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 시간 측정 예제
 *  - elapsed Operator
 *      - emit된 데이터 사이의 경과된 시간을 측정한다.
 *      - emit된 첫번째 데이터는 onSubscribe Signal과 첫번째 데이터 사이의 시간을 기준으로 측정한다.
 *      - 측정된 시간 단위는 milliseconds이다.
 */
@Slf4j
public class Example14_52 {
    public static void main(String[] args) throws InterruptedException {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://timeapi.io/api/Time/current/zone?timeZone=Asia/Seoul")
                .build();

        // 1. Mono.defer를 통해 구독 시점에 API 호출이 발생하도록 설정
        Mono.defer(() -> webClient.get()
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(String.class))
                .repeat(4) // 총 5번 실행 (1회 + 4회 반복)
                .delayElements(Duration.ofMillis(500)) // 각 실행 사이에 의도적인 지연 추가 (측정 가시성)
                .elapsed() // 구독 시점 또는 이전 데이터 방출 시점으로부터의 경과 시간 측정
                .map(tuple -> {
                    long elapsedTime = tuple.getT1(); // 경과 시간 (ms)
                    String response = tuple.getT2();  // API 응답 바디

                    String dateTime = JsonPath.parse(response).read("$.dateTime", String.class);
                    return String.format("now: %s, elapsed time: %dms", dateTime, elapsedTime);
                })
                .subscribe(
                        log::info,
                        error -> log.error("# onError: {}", error.getMessage()),
                        () -> log.info("# onComplete")
                );

        // 비동기 작업 완료를 위해 충분히 대기
        Thread.sleep(7000);
    }
}