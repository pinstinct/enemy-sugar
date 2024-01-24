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
     */
    Flux<Beverage> createBeverage();
}
