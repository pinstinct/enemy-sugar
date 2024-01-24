package com.limhm.enemy.sugar.strategy;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

/**
 * 행동 패턴 중 전략 패턴 사용 - 전략 인터페이스 생성
 * <p>
 * 특정 작업을 다양한 방식으로 수행하는 클래스를 선택한 후 모든 알고리즘을 전략들(strategies)이라는 별도의 인터페이스들로 추출할 것을 제안한다.
 * 콘텍스트(context)는 작업을 자체적으로 실행하는 대신 연결된 전략 객체에 위임한다. 콘텍스트는 작업에 적합한 알고리즘을 선택할 책임이 없다. 대신 클라이언트가 원하는
 * 전략을 콘텍스트에 전달한다. 콘텍스트는 선택된 전략 내에 캡슐화된 알고리즘을 작동시킬 단일 메서드만 노출한다.
 * <p>
 * 알고리즘을 동적으로 변경 가능하도록 분리시켜 각 클래스(데이터)에 맞게 엑셀을 작성한다.
 */
public interface ExcelExportable {

    /**
     * Abstract method: 구현 없이 선언된 메서드
     */
    void writeRow(Row row);

    /**
     * Default method: 본문을 가질 수 있는 메서드
     *
     * @implSpec 엑셀에 셀을 생성 후 값을 설정한다.
     */
    default void createCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
    }

    /**
     * @implSpec 엑셀의 셀을 생성 후 값과 서식을 설정한다.
     */
    default void createCellByStyle(Row row, int columnIndex, Double value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
