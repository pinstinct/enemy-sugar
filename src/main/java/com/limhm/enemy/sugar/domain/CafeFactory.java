package com.limhm.enemy.sugar.domain;

import reactor.core.publisher.Flux;

public interface CafeFactory {
    Flux<CafeDrink> createBeverages();
}
