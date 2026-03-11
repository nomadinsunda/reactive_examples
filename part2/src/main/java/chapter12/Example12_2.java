package chapter12;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class Example12_2 {
    public static void main(String[] args) {
        Flux.just(2, 4, 6, 8)
                .zipWith(
                        Flux.just(1, 2, 3, 0)
                                .filter(y -> y != 0), // 1. 분모가 0인 데이터를 사전에 제거 (가장 깔끔함)
                        (x, y) -> x / y
                )
                .map(num -> num + 2)
                // 2. 혹시 모를 에러 발생 시 해당 데이터만 건너뛰고 계속 진행
                .onErrorContinue((error, data) -> {
                    log.error("# 에러 발생 원인: {}", error.getMessage());
                    log.error("# 문제가 된 데이터: {}", data);
                })
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# 최종 에러: ", error), // 이제 이 로그는 찍히지 않습니다.
                        () -> log.info("# 모든 작업이 정상적으로 완료되었습니다.")
                );
    }
}