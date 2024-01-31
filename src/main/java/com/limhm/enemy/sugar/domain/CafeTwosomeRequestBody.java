package com.limhm.enemy.sugar.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CafeTwosomeRequestBody {

    private String menuName;
    private String menuCode;
    private String temperatureOption;
    private String sizeOption;
}
