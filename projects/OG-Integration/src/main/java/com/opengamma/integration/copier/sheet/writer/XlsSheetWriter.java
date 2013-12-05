/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.sheet.writer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class XlsSheetWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(XlsSheetWriter.class);

  private Sheet _sheet;
  private Workbook _workbook;
  private Integer _currentRow = 0;

  public XlsSheetWriter(HSSFWorkbook workbook, String sheetname) {

    ArgumentChecker.notEmpty(sheetname, "sheetname");
    ArgumentChecker.notNull(workbook, "workbook");

    _workbook = workbook;
    _sheet = _workbook.createSheet(sheetname);

  }

  private Row getCurrentRow() {
    Row row = _sheet.getRow(_currentRow);
    if (row == null) {
      row = _sheet.createRow(_currentRow);
    }
    return row;
  }

  private Row getRow(int rowIndex) {
    Row row = _sheet.getRow(rowIndex);
    if (row == null) {
      row = _sheet.createRow(rowIndex);
    }
    return row;
  }

  private Cell getCell(Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) {
      cell = row.createCell(index, Cell.CELL_TYPE_STRING);
    }
    return cell;
  }

  public void writeKeyValueBlock(Map<String, String> details) {
    ArgumentChecker.notNull(details, "details");



    for (Map.Entry<String, String> entry : details.entrySet()) {
      Row row = getCurrentRow();
      Cell keyCell = getCell(row, 0);

      Cell valueCell = getCell(row, 1);
      keyCell.setCellValue(entry.getKey());
      valueCell.setCellValue(entry.getValue());
      _currentRow++;
    }
    _currentRow++;

  }

  public void writePairBlock(Map<String, ObjectsPair<String, String>> details) {
    ArgumentChecker.notNull(details, "details");

    for (Map.Entry<String, ObjectsPair<String, String>> entry : details.entrySet()) {
      Row row = getCurrentRow();
      Cell keyCell = getCell(row, 0);
      keyCell.setCellValue(entry.getKey());
      if (entry.getValue().getFirst() != null) {
        Cell firstValueCell = getCell(row, 1);
        firstValueCell.setCellValue(entry.getValue().getFirst());
      }
      if (entry.getValue().getSecond() != null) {
        Cell secondValueCell = getCell(row, 2);
        secondValueCell.setCellValue(entry.getValue().getSecond());
      }
      _currentRow++;
    }
    _currentRow++;

  }

  public void writeMatrix(Set<String> xMap,
                          Set<String> yMap,
                          String label,
                          Map<Pair<String, String>, String> marketValueMap) {

    ArgumentChecker.notNull(xMap, "xMap");
    ArgumentChecker.notNull(yMap, "yMap");
    ArgumentChecker.notNull(marketValueMap, "marketValueMap");

    Map<String, Integer> xCol = new HashMap<>();
    Map<String, Integer> yRow = new HashMap<>();

    Row labelRow = getCurrentRow();
    Cell labelCell = getCell(labelRow, 0);
    labelCell.setCellValue(label);

    int colIndex = 1;
    for (String entry : xMap) {
      Row row = getCurrentRow();
      Cell cell = getCell(row, colIndex);
      cell.setCellValue(entry);
      xCol.put(entry, colIndex);
      colIndex++;
    }

    _currentRow++;
    for (String entry : yMap) {
      Row row = getCurrentRow();
      Cell cell = getCell(row, 0);
      cell.setCellValue(entry);
      yRow.put(entry, _currentRow);
      _currentRow++;
    }
    _currentRow++;

    for (Map.Entry<Pair<String, String>, String> entry : marketValueMap.entrySet()) {
      Cell valueCell = getCell(getRow(yRow.get(entry.getKey().getSecond())), xCol.get(entry.getKey().getFirst()));
      valueCell.setCellValue(entry.getValue());
    }

  }
}
