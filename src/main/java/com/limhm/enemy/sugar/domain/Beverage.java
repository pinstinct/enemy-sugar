package com.limhm.enemy.sugar.domain;

import com.limhm.enemy.sugar.strategy.ExcelExportable;

/**
 * ExcelExporter 클래스가 ExcelExportable 전략 인터페이스를 구현한 클래스를 파라미터로 받으므로, ExcelExportable 인터페이스를 상속한다.
 */
public interface Beverage extends ExcelExportable {

    Company getCompany();
}
