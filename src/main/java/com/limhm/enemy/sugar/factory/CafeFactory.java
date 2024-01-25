package com.limhm.enemy.sugar.factory;

import com.limhm.enemy.sugar.domain.Beverage;
import reactor.core.publisher.Flux;

/**
 * 생성 패턴 중 추상 팩토리 패턴
 * <p>
 * 추상 제품들을 생성하기 위한 생성 메서드들이 목록화되어 있는 인터페이스이다.
 */
public interface CafeFactory {

    /**
     * 추상 제품을 반환한다. 카페의 홈페이지를 비동기로 크롤링한다.
     * <p>
     * 프로젝트 리액터는 두 유형의 퍼블리셔를 정의한다. Mono, 0 또는 1개 요소를 방출한다. Flux, 0에서 n개 혹은 정의된 수의 요소를 방출한다. Mono와
     * Flux 모두 Reactive Stream의 Publisher 인터페이스를 구현하고 있으며, 리액터에서 제공하는 연산자들의 조합을 통해 스트림을 표현할 수 있다.
     */
    Flux<Beverage> createBeverage();
}
