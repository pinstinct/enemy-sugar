package com.limhm.enemy.sugar.factory;

import com.limhm.enemy.sugar.domain.CafeDrink;
import reactor.core.publisher.Flux;

public interface CafeFactory {

    Flux<CafeDrink> createBeverages();
}
